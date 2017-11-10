package io.amelia.foundation;

import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import io.amelia.lang.ApplicationException;
import io.amelia.support.Objs;
import io.amelia.support.Timings;
import io.amelia.synchronize.AsyncQueueMessage;

public class LooperQueue
{
	/**
	 * Indicates an empty queue.
	 */
	static final int RESULT_EMPTY = 0x1;
	/**
	 * Indicates the queue is new or something has changed since the last time next() was called.
	 */
	static final int RESULT_NONE = 0x0;
	/**
	 * Indicates the next enqueued task has been returned by next().
	 */
	static final int RESULT_OK = 0x3;
	/**
	 * Indicates the queue has encountered a stall barrier, we are waiting for it removal.
	 */
	static final int RESULT_STALLED = 0x2;
	/**
	 * Indicates the next enqueued task is still in the future, so we wait.
	 */
	static final int RESULT_WAITING = 0x4;
	private final NavigableSet<Entry> entries = new TreeSet<>();
	private final Looper looper;
	Result result = new Result();
	private boolean polling = false;

	public LooperQueue( Looper looper )
	{
		this.looper = looper;
	}

	<T extends Entry> T addQueueTask( T queueTask )
	{
		synchronized ( entries )
		{
			entries.add( queueTask );
			return queueTask;
		}
	}

	public void cancel( long id )
	{
		synchronized ( entries )
		{
			Iterator<Entry> queueIterator = entries.iterator();
			while ( queueIterator.hasNext() )
			{
				Entry entry = queueIterator.next();
				if ( entry.getId() == id )
				{
					queueIterator.remove();
					return;
				}
			}

			throw new IllegalStateException( "The specified id does not exist or has already been removed." );
		}
	}

	public void cancelAllBarriers()
	{
		synchronized ( entries )
		{
			/* If a barrier is actively blocking the queue, wake the Looper. */
			if ( result.entry != null && result.entry instanceof BarrierEntry )
			{
				result.entry = null;
				if ( !looper.isQuitting() )
					looper.wake();
			}

			/* Iterate over the pending entries. */
			entries.removeIf( entry -> entry instanceof BarrierEntry );
		}
	}

	/**
	 * Removes a synchronization barrier.
	 *
	 * @param id The synchronization barrier id that was returned by {@link #postTaskBarrier}.
	 * @throws IllegalStateException if the barrier was not found.
	 */
	public void cancelBarrier( long id )
	{
		// Remove a sync barrier id from the queue.
		// If the queue is no longer stalled by a barrier then wake it.
		synchronized ( entries )
		{
			/* The barrier is actively blocking the queue, so wake the Looper. */
			if ( result.entry != null && result.entry instanceof BarrierEntry && result.entry.getId() == id )
			{
				result.entry = null;
				if ( !looper.isQuitting() )
					looper.wake();
				return;
			}

			/* Iterate over the pending entries. */
			Iterator<Entry> queueIterator = entries.iterator();
			while ( queueIterator.hasNext() )
			{
				Entry entry = queueIterator.next();
				if ( entry instanceof BarrierEntry && entry.getId() == id )
				{
					queueIterator.remove();
					return;
				}
			}

			throw new IllegalStateException( "The specified barrier id does not exist or has already been removed." );
		}
	}

	boolean enqueueMessage( @Nonnull InternalMessage msg, long when )
	{
		Objs.notNull( msg );
		Objs.notNegative( when );

		if ( msg.isInUse() )
			throw new IllegalStateException( "Message is already in use." );

		synchronized ( this )
		{
			if ( isQuitting )
			{
				LOG.warning( "PostalQueue is quiting." );
				msg.recycle();
				return false;
			}

			msg.markInUse( true );

			messages.add( msg );
			boolean needWake;

			if ( activeQueueMessage == null || when == 0 || when < activeQueueMessage.getWhen() )
			{
				// New head, wake up the event queue if blocked.
				activeQueueMessage = msg;
				needWake = isNextBlocked;
			}
			else
			{
				// Inserted within the middle of the queue.  Usually we don't have to wake
				// up the event queue unless there is a barrier at the head of the queue
				// and the message is the earliest asynchronous message in the queue.
				needWake = isNextBlocked && queueState == Looper.State.STALLED && msg instanceof AsyncQueueMessage;
			}

			if ( needWake )
				notify();
		}
		return true;
	}

	public Entry getActiveEntry()
	{
		return result.entry;
	}

	public long getEarliest()
	{
		Entry first = entries.first();
		return first == null ? 0L : first.getWhen();
	}

	public Result getLastResult()
	{
		return result;
	}

	public long getLatest()
	{
		Entry last = entries.last();
		return last == null ? Long.MAX_VALUE : last.getWhen();
	}

	public int getResultCode()
	{
		return result.resultCode;
	}

	public boolean hasEntries()
	{
		return entries.size() > 0;
	}

	public boolean isEmpty()
	{
		return result.resultCode == RESULT_EMPTY;
	}

	/**
	 * Returns true if the looper has no pending entries which are due to be processed.
	 * <p>
	 * This method is safe to call from any thread.
	 *
	 * @return True if the looper is idle.
	 */
	public boolean isIdle()
	{
		return result.resultCode == RESULT_EMPTY || result.resultCode == RESULT_WAITING;
	}

	/**
	 * Returns whether this looper queue is currently polling for more work to do.
	 * This is a good signal that the loop is still alive rather than being stuck
	 * handling a callback.
	 *
	 * @return True if the looper is currently polling for entries.
	 */
	public boolean isPolling()
	{
		return !looper.isQuitting() && polling;
	}

	public boolean isStalled()
	{
		return result.resultCode == RESULT_STALLED;
	}

	public boolean isWaiting()
	{
		return result.resultCode == RESULT_WAITING;
	}

	Result next( long now )
	{
		polling = true;
		for ( ; ; )
		{
			synchronized ( entries )
			{
				/* If the current entry is null, poll for the first entry. */
				if ( result.entry == null )
					result.entry = entries.pollFirst();
				/* If it is still null, assume we have no more entries to get. */
				if ( result.entry == null )
					result.resultCode = RESULT_EMPTY;
				else if ( result.entry instanceof CheckpointEntry )
				{
					/*
					 * We filter out the remaining entries looking for anything besides just more CheckpointEntry instances.
					 * This allows for all remaining CheckpointEntry instance that may be in a row to receive the same answer.
					 */
					boolean hasMoreEntries = entries.stream().filter( e -> !( e instanceof CheckpointEntry ) ).count() > 0;
					/* Based on the information provided, CheckpointEntry will decide if it would like to be rescheduled. */
					( ( CheckpointEntry ) result.entry ).run( hasMoreEntries );

					/* Clear out the entry and go again. */
					result.entry = null;
					continue;
				}
				else if ( result.entry instanceof BarrierEntry )
				{
					/* Executes the inner Predicate contained in the barrier, removing it on False. */
					result.entry.getRunnable().run();
					result.resultCode = RESULT_STALLED;
				}
				else if ( result.entry instanceof TaskEntry )
				{
					if ( now < result.entry.getWhen() )
						result.resultCode = RESULT_WAITING;
					else
						result.resultCode = RESULT_OK;
				}
				else
				{
					polling = false;
					throw new IllegalStateException( "BUG? Unimplemented QueueEntry subclass " + result.entry.getClass().getSimpleName() );
				}

				polling = false;
				/* Return the result */
				return result;
			}
		}
	}

	/**
	 * Adds a checkpoint to this queue.
	 * <p>
	 * The checkpoint is removed automatically for you by returning false from the
	 * {@link BiPredicate#test(Object, Object)} when it is invoked, or explicitly
	 * removing it with {@link #cancel}.
	 *
	 * @param predicate The Predicate that is tested if the checkpoint should be
	 *                  removed from the queue. Predicate will be provided an
	 *                  instance of Looper and if there are more enqueued tasks
	 *                  that follow the checkpoint.
	 * @return The instance of QueueTask for easy tracking.
	 */
	public Entry postCheckpoint( BiPredicate<Looper, Boolean> predicate )
	{
		return addQueueTask( new CheckpointEntry( predicate ) );
	}

	public Entry postTask( Runnable task )
	{
		return addQueueTask( new RunnableQueueTask( task ) );
	}

	public Entry postTaskAsync( Runnable task )
	{
		return addQueueTask( new RunnableQueueTask( task, true ) );
	}

	public Entry postTaskAt( Runnable task, long when )
	{
		return addQueueTask( new RunnableQueueTask( task, when ) );
	}

	public Entry postTaskAtAsync( Runnable task, long when )
	{
		return addQueueTask( new RunnableQueueTask( task, when, true ) );
	}

	private Entry postTaskBarrier( long when )
	{
		return postTaskBarrier( when, null );
	}

	private Entry postTaskBarrier( long when, Predicate<Looper> predicate )
	{
		/* Enqueue a new barrier. We don't need to wake the queue because the purpose of a barrier is to stall it. */
		synchronized ( this )
		{
			BarrierEntry barrier = new BarrierEntry( when, predicate );
			entries.add( barrier );
			return barrier;
		}
	}

	/**
	 * Adds a barrier to this queue.
	 * <p>
	 * Message processing occurs as usual until the message queue encounters the
	 * barrier that has been posted.  When the barrier is encountered,
	 * later synchronous messages in the queue are stalled (prevented from being executed)
	 * until the barrier is released by calling {@link #cancelBarrier} and specifying
	 * the token that identifies the synchronization barrier.
	 * <p>
	 * This method is used to immediately postpone execution of all subsequently posted
	 * synchronous messages until a condition is met that releases the barrier.
	 * Asynchronous messages (see {@link Entry#isAsync} are exempt from the barrier
	 * and continue to be processed as usual.
	 * <p>
	 * This call must be always matched by a call to {@link #cancelBarrier} with
	 * the same token to ensure that the message queue resumes normal operation.
	 * Otherwise the application will probably hang!
	 *
	 * @return The instance of QueueTask which can be used to cancel the barrier.
	 */
	public Entry postTaskBarrier()
	{
		return postTaskBarrier( null );
	}

	public Entry postTaskBarrier( Predicate<Looper> predicate )
	{
		return postTaskBarrier( Timings.epoch(), predicate );
	}

	public Entry postTaskLater( Runnable task, long delay )
	{
		return addQueueTask( new RunnableQueueTask( task, System.currentTimeMillis() + delay ) );
	}

	public Entry postTaskLaterAsync( Runnable task, long delay )
	{
		return addQueueTask( new RunnableQueueTask( task, System.currentTimeMillis() + delay, true ) );
	}

	private class BarrierEntry extends Entry
	{
		private long id = Looper.getGloballyUniqueId();
		private Predicate<Looper> predicate;
		private long when;

		public BarrierEntry( long when, @Nonnull Predicate<Looper> predicate )
		{
			this.when = when;
			this.predicate = predicate;
		}

		public long getId()
		{
			return id;
		}

		@Override
		Runnable getRunnable()
		{
			return () -> {
				if ( !predicate.test( looper ) )
					cancel();
			};
		}

		@Override
		public long getWhen()
		{
			return when;
		}

		@Override
		public void markInUse( boolean isInUse )
		{
			// Ignored
		}

		@Override
		public void recycle()
		{
			// Do Nothing
		}
	}

	private class CheckpointEntry extends Entry
	{
		BiPredicate<Looper, Boolean> predicate;
		long when;

		CheckpointEntry( BiPredicate<Looper, Boolean> predicate )
		{
			this.predicate = predicate;
			when = getLatest() + 1L;
		}

		@Override
		Runnable getRunnable()
		{
			return null;
		}

		@Override
		public long getWhen()
		{
			return when;
		}

		@Override
		void recycle()
		{
			// Still Does Nothing
		}

		public void run( boolean hasMoreEntries )
		{
			if ( predicate.test( looper, hasMoreEntries ) )
				postCheckpoint( predicate );
		}
	}

	public abstract class Entry
	{
		private final boolean async;

		private final long id = Looper.getGloballyUniqueId();
		/**
		 * Indicates when the entry has been processed by the queue
		 * <p>
		 * This boolean is set when the message is enqueued and remains set while it
		 * is delivered and afterwards when it is recycled. The flag is only cleared
		 * once {@link #recycle()} is called and it's contents are zeroed.
		 * <p>
		 * It is an error to attempt to enqueue or recycle a message that is already finalized.
		 */
		private boolean finalized;

		Entry()
		{
			this.async = false;
		}

		Entry( boolean async )
		{
			this.async = async;
		}

		public void cancel()
		{
			synchronized ( entries )
			{
				if ( result.entry == this )
				{
					result.resultCode = RESULT_NONE;
					result.entry = null;
					looper.wake();
				}
				else
					entries.remove( this );
			}
		}

		public long getId()
		{
			return id;
		}

		public int getPositionInQueue()
		{
			synchronized ( entries )
			{
				int pos = 0;
				for ( Entry queueTask : entries )
					if ( queueTask == this )
						return pos;
					else
						pos++;

				return -1;
			}
		}

		abstract Runnable getRunnable();

		/**
		 * Used for sorting, indicates when the entry is scheduled for processing.
		 */
		public abstract long getWhen();

		public boolean isActive()
		{
			return result.entry == this;
		}

		public boolean isAsync()
		{
			return async;
		}

		public boolean isEnqueued()
		{
			synchronized ( entries )
			{
				return entries.contains( this );
			}
		}

		public boolean isFinalized()
		{
			return finalized;
		}

		void markFinalized()
		{
			finalized = true;
		}

		void recycle()
		{
			finalized = false;
		}
	}

	class Result
	{
		Entry entry = null;
		int resultCode = RESULT_NONE;
	}

	private class RunnableQueueTask extends Entry
	{
		private final Runnable task;
		private final long when;

		RunnableQueueTask( Runnable task, long when )
		{
			this( task, when, false );
		}

		RunnableQueueTask( Runnable task, long when, boolean async )
		{
			super( async );

			this.task = task;
			if ( when < 0 ) // Now
				this.when = System.currentTimeMillis();
			else if ( when < getEarliest() ) // Confirm this task won't come before any active entries unless it's async.
				throw ApplicationException.runtime( "Task must be in the future." );
			else
				this.when = when;
		}

		RunnableQueueTask( Runnable task, boolean async )
		{
			this( task, -1, async );
		}

		RunnableQueueTask( Runnable task )
		{
			this( task, -1 );
		}

		@Override
		Runnable getRunnable()
		{
			return task;
		}

		@Override
		public long getWhen()
		{
			return when;
		}

		@Override
		void recycle()
		{

		}
	}

	private class TaskEntry extends Entry
	{
		InternalMessage message;
		long when;

		TaskEntry( InternalMessage message, long when )
		{
			this.message = message;
			this.when = when;
		}

		@Override
		Runnable getRunnable()
		{
			return msg;
		}

		@Override
		public long getWhen()
		{
			return when;
		}

		@Override
		void recycle()
		{
			msg.recycle;
		}
	}
}
