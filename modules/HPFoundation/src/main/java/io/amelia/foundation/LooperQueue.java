package io.amelia.foundation;

import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import io.amelia.lang.ApplicationException;
import io.amelia.support.DateAndTime;
import io.amelia.support.Objs;

public class LooperQueue
{
	private final NavigableSet<AbstractEntry> entries = new TreeSet<>();
	private final Looper looper;
	ActiveState activeState = new ActiveState();
	private boolean isPolling = false;

	public LooperQueue( Looper looper )
	{
		this.looper = looper;
	}

	<T extends AbstractEntry> T addQueueTask( T queueTask )
	{
		if ( looper.isQuitting() )
			throw new IllegalStateException( "The LooperQueue is quitting!" );

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
			Iterator<AbstractEntry> queueIterator = entries.iterator();
			while ( queueIterator.hasNext() )
			{
				AbstractEntry entry = queueIterator.next();
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
			if ( activeState.entry != null && activeState.entry instanceof BarrierEntry )
			{
				activeState.entry = null;
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
	 * @param id The synchronization barrier id that was returned by {@link #postBarrier}.
	 *
	 * @throws IllegalStateException if the barrier was not found.
	 */
	public void cancelBarrier( long id )
	{
		// Remove a sync barrier id from the queue.
		// If the queue is no longer stalled by a barrier then wake it.
		synchronized ( entries )
		{
			/* The barrier is actively blocking the queue, so wake the Looper. */
			if ( activeState.entry != null && activeState.entry instanceof BarrierEntry && activeState.entry.getId() == id )
			{
				activeState.entry = null;
				if ( !looper.isQuitting() )
					looper.wake();
				return;
			}

			/* Iterate over the pending entries. */
			Iterator<AbstractEntry> queueIterator = entries.iterator();
			while ( queueIterator.hasNext() )
			{
				AbstractEntry entry = queueIterator.next();
				if ( entry instanceof BarrierEntry && entry.getId() == id )
				{
					queueIterator.remove();
					return;
				}
			}

			throw new IllegalStateException( "The specified barrier id does not exist or has already been removed." );
		}
	}

	void clearState()
	{
		activeState.result = Result.NONE;
	}

	boolean enqueueMessage( @Nonnull ParcelCarrier msg, long when )
	{
		Objs.notNull( msg );
		Objs.notNegative( when );

		if ( msg.isInUse() )
			throw new IllegalStateException( "Message is already in use." );

		synchronized ( entries )
		{
			if ( looper.isQuitting() )
			{
				Looper.L.warning( "Looper is quiting." );
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

	public AbstractEntry getActiveEntry()
	{
		return activeState == null ? null : activeState.entry;
	}

	public long getEarliest()
	{
		AbstractEntry first = entries.first();
		return first == null ? 0L : first.getWhen();
	}

	public Result getLastResult()
	{
		return activeState.result;
	}

	public long getLatest()
	{
		AbstractEntry last = entries.last();
		return last == null ? Long.MAX_VALUE : last.getWhen();
	}

	public boolean hasEntries()
	{
		return entries.size() > 0;
	}

	public boolean isEmpty()
	{
		return activeState.result == Result.EMPTY;
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
		return activeState.result == Result.EMPTY || activeState.result == Result.WAITING;
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
		return !looper.isQuitting() && isPolling;
	}

	public boolean isStalled()
	{
		return activeState.result == Result.STALLED;
	}

	public boolean isWaiting()
	{
		return activeState.result == Result.WAITING;
	}

	ActiveState next( long now )
	{
		isPolling = false;
		try
		{
			for ( ; ; )
			{
				synchronized ( entries )
				{
					// If the current entry is null, poll for the next upcoming entry.
					if ( activeState.entry == null )
						activeState.entry = entries.pollFirst();

					// If it is still null assume the queue is effectively empty.
					if ( activeState.entry == null )
						activeState.result = Result.EMPTY;
					else if ( activeState.entry instanceof CheckpointEntry )
					{
						// We filter out the remaining entries looking for anything besides just more CheckpointEntry instances.
						// This allows for all remaining CheckpointEntry instances that may be in a row to receive the same answer.
						boolean hasMoreEntries = entries.stream().filter( e -> !( e instanceof CheckpointEntry ) ).count() > 0;

						BiPredicate<Looper, Boolean> predicate = ( ( CheckpointEntry ) activeState.entry ).predicate;

						// Based on the information provided, CheckpointEntry will decide if it would like to be rescheduled for a later time.
						if ( predicate.test( looper, hasMoreEntries ) )
							postCheckpoint( predicate );

						// Reset the entry and go again.
						activeState.entry = null;
						continue;
					}
					else if ( activeState.entry instanceof BarrierEntry )
					{
						// Executes the inner Predicate contained in the barrier, removing it on False.
						( ( BarrierEntry ) activeState.entry ).run();
						activeState.result = Result.STALLED;
					}
					else if ( activeState.entry instanceof ParcelEntry )
					{
						if ( now < activeState.entry.getWhen() )
							activeState.result = Result.WAITING;
						else
							activeState.result = Result.SUCCESS;
					}
					else
						throw new IllegalStateException( "BUG? Unimplemented QueueEntry subclass " + activeState.entry.getClass().getSimpleName() );

					// Return the activeState.
					return activeState;
				}
			}
		}
		finally
		{
			isPolling = true;
		}
	}

	private BarrierEntry postBarrier( long when )
	{
		return postBarrier( when, null );
	}

	private BarrierEntry postBarrier( long when, Predicate<Looper> predicate )
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
	 * Asynchronous messages (see {@link AbstractEntry#isAsync} are exempt from the barrier
	 * and continue to be processed as usual.
	 * <p>
	 * This call must be always matched by a call to {@link #cancelBarrier} with
	 * the same token to ensure that the message queue resumes normal operation.
	 * Otherwise the application will probably hang!
	 *
	 * @return The instance of the {@link BarrierEntry} which can be used to cancel the barrier.
	 */
	public BarrierEntry postBarrier()
	{
		return postBarrier( null );
	}

	public BarrierEntry postBarrier( Predicate<Looper> predicate )
	{
		return postBarrier( DateAndTime.epoch(), predicate );
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
	 *
	 * @return The instance of QueueTask for easy tracking.
	 */
	public CheckpointEntry postCheckpoint( BiPredicate<Looper, Boolean> predicate )
	{
		return addQueueTask( new CheckpointEntry( predicate ) );
	}

	/**
	 * Causes the Runnable task to be added to the {@link LooperQueue}.
	 * The runnable will be run on the thread to which this queue is attached.
	 *
	 * @param task The Runnable that will be executed.
	 *
	 * @return Returns an {@link TaskEntry} instance that references the enqueued runnable.
	 */
	public TaskEntry postTask( Runnable task )
	{
		return addQueueTask( new TaskEntry( task ) );
	}

	public TaskEntry postTaskAsync( Runnable task )
	{
		return addQueueTask( new TaskEntry( task, true ) );
	}

	/**
	 * Causes the Runnable task to be added to the queue, to be run
	 * at a specific time given by <var>System.currentTimeMillis()</var>.
	 *
	 * @param task The Runnable that will be executed.
	 * @param when The absolute time at which the callback should run.
	 *
	 * @return Returns an {@link TaskEntry} instance that references the enqueued runnable.
	 *
	 * @see #postTaskAtAsync(Runnable, long)
	 */
	public TaskEntry postTaskAt( Runnable task, long when )
	{
		return addQueueTask( new TaskEntry( task, when ) );
	}

	/**
	 * @see #postTaskAt(Runnable, long)
	 */
	public TaskEntry postTaskAtAsync( Runnable task, long when )
	{
		return addQueueTask( new TaskEntry( task, when, true ) );
	}

	/**
	 * Causes the Runnable r to be added to the queue, to be run
	 * after the specified amount of time elapses.
	 * The runnable will be run on the thread to which this handler is attached.
	 *
	 * @param task  The Runnable that will be executed.
	 * @param delay The amount of time to the current time.
	 *
	 * @return Returns an {@link TaskEntry} instance that references the enqueued runnable.
	 *
	 * @see #postTaskLaterAsync(Runnable, long)
	 */
	public TaskEntry postTaskLater( Runnable task, long delay )
	{
		return addQueueTask( new TaskEntry( task, System.currentTimeMillis() + delay ) );
	}

	/**
	 * @see #postTaskLater(Runnable, long)
	 */
	public TaskEntry postTaskLaterAsync( Runnable task, long delay )
	{
		return addQueueTask( new TaskEntry( task, System.currentTimeMillis() + delay, true ) );
	}

	/**
	 * Posts an object to this queue that implements Runnable.
	 * Causes the Runnable task to executed on the next iteration through the
	 * queue. The runnable will be run on the thread to which this handler is attached.
	 *
	 * This method is only for use in very special circumstances -- it
	 * can easily starve the message queue, cause ordering problems, or have
	 * other unexpected side-effects.
	 *
	 * @param task The Runnable that will be executed.
	 *
	 * @return Returns an {@link TaskEntry} instance that references the enqueued runnable.
	 *
	 * @see #postTaskAsync(Runnable)
	 */
	public TaskEntry postTaskNext( Runnable task )
	{
		return addQueueTask( new TaskEntry( task, getEarliest() + 1L ) );
	}

	/**
	 * @see #postTaskNext(Runnable)
	 */
	public TaskEntry postTaskNextAsync( Runnable task )
	{
		return addQueueTask( new TaskEntry( task, getEarliest() + 1L, true ) );
	}

	/**
	 * Runs the specified task synchronously.
	 * <p>
	 * If the current thread is the same as the handler thread, then the runnable
	 * runs immediately without being enqueued.  Otherwise, posts the runnable
	 * to the handler and waits for it to complete before returning.
	 * <p>
	 * This method is dangerous!  Improper use can result in deadlocks.
	 * Never call this method while any locks are held or use it in a
	 * possibly re-entrant manner.
	 * <p>
	 * This method is occasionally useful in situations where a background thread
	 * must synchronously await completion of a task that must run on the
	 * handler's thread.  However, this problem is often a symptom of bad design.
	 * Consider improving the design (if possible) before resorting to this method.
	 * <p>
	 * One example of where you might want to use this method is when you just
	 * set up a Handler thread and need to perform some initialization steps on
	 * it before continuing execution.
	 * <p>
	 * If timeout occurs then this method returns <code>false</code> but the runnable
	 * will remain posted on the handler and may already be in progress or
	 * complete at a later time.
	 *
	 * @param task    The Runnable that will be executed synchronously.
	 * @param timeout The timeout in milliseconds, or 0 to wait indefinitely (makes this dangerous method, even more dangerous).
	 *
	 * @return Returns true if the Runnable was successfully executed.
	 * Returns false on failure, usually because the
	 * looper processing the message queue is exiting.
	 */
	public boolean postTaskUnsafe( @Nonnull Runnable task, @Nonnegative long timeout )
	{
		if ( looper.isThreadJoined() )
		{
			task.run();
			return true;
		}

		BlockingRunnable blockingRunnable = new BlockingRunnable( task );
		looper.getQueue().postTask( blockingRunnable );
		return blockingRunnable.postAndWait( timeout );
	}

	void quit( boolean removePendingMessages )
	{
		// Currently, there is nothing to do when we quitSafely.
		if ( !removePendingMessages )
			return;

		if ( !looper.isHeldByCurrentThread() )
			throw ApplicationException.runtime( "Looper must be locked by this thread to quit the LooperQueue." );

		final long now = System.currentTimeMillis();
		synchronized ( entries )
		{
			entries.removeIf( entry -> {
				if ( entry.removesSafely() && entry.getWhen() > now )
				{
					entry.recycle();
					return true;
				}
				else
					return false;
			} );
		}
	}

	public enum Result
	{
		/**
		 * Indicates the queue is new or something has changed since the last time next() was called.
		 */
		NONE,
		/**
		 * Indicates an empty queue.
		 */
		EMPTY,
		/**
		 * Indicates the queue has encountered a stall barrier, we are waiting for it removal.
		 */
		STALLED,
		/**
		 * Indicates the next enqueued task has been returned by next().
		 */
		SUCCESS,
		/**
		 * Indicates the next enqueued task is still in the future, so we wait.
		 */
		WAITING
	}

	public abstract class AbstractEntry
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

		AbstractEntry()
		{
			this.async = false;
		}

		AbstractEntry( boolean async )
		{
			this.async = async;
		}

		public void cancel()
		{
			synchronized ( entries )
			{
				if ( activeState.entry == this )
				{
					activeState.result = Result.NONE;
					activeState.entry = null;
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
				for ( AbstractEntry queueTask : entries )
					if ( queueTask == this )
						return pos;
					else
						pos++;

				return -1;
			}
		}

		/**
		 * Used for sorting, indicates when the entry is scheduled for processing.
		 */
		public abstract long getWhen();

		public boolean isActive()
		{
			return activeState.entry == this;
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

		/**
		 * Determines that the entry can be removed from the queue with any bugs to the Application.
		 *
		 * @return True if removal is permitted and this task doesn't have to run.
		 */
		public abstract boolean removesSafely();
	}

	class ActiveState
	{
		AbstractEntry entry = null;
		Result result = Result.NONE;
	}

	public class BarrierEntry extends RunnableEntry
	{
		private long id = Looper.getGloballyUniqueId();
		private Predicate<Looper> predicate;
		private long when;

		BarrierEntry( @Nonnegative long when, @Nonnull Predicate<Looper> predicate )
		{
			this.when = when;
			this.predicate = predicate;
		}

		public long getId()
		{
			return id;
		}

		@Override
		public long getWhen()
		{
			return when;
		}

		@Override
		public void recycle()
		{
			// Do Nothing
		}

		@Override
		public boolean removesSafely()
		{
			return true;
		}

		@Override
		void run0()
		{
			if ( !predicate.test( looper ) )
				cancel();
		}
	}

	public class CheckpointEntry extends AbstractEntry
	{
		BiPredicate<Looper, Boolean> predicate;
		long when;

		CheckpointEntry( @Nonnull BiPredicate<Looper, Boolean> predicate )
		{
			this.predicate = predicate;
			when = getLatest() + 1L;
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

		@Override
		public boolean removesSafely()
		{
			return true;
		}
	}

	public class ParcelEntry extends RunnableEntry
	{
		ParcelCarrier message;
		long when;

		ParcelEntry( @Nonnull ParcelCarrier message, @Nonnegative long when )
		{
			this.message = message;
			this.when = when;
		}

		@Override
		public long getWhen()
		{
			return when;
		}

		@Override
		void recycle()
		{
			message.recycle();
		}

		@Override
		public boolean removesSafely()
		{
			return true;
		}

		@Override
		void run0()
		{
			// TODO
		}
	}

	public abstract class RunnableEntry extends AbstractEntry implements Runnable
	{
		RunnableEntry()
		{
			super();
		}

		RunnableEntry( boolean async )
		{
			super( async );
		}

		@Override
		public void run()
		{
			if ( isAsync() || looper.hasFlag( Looper.Flag.ASYNC ) )
				looper.runAsync( this::run0 );
			else
				run0();
		}

		abstract void run0();
	}

	public class TaskEntry extends RunnableEntry
	{
		private final Runnable task;
		private final long when;

		TaskEntry( @Nonnull Runnable task, long when )
		{
			this( task, when, false );
		}

		TaskEntry( @Nonnull Runnable task, long when, boolean async )
		{
			super( async );

			Objs.notNull( task );

			this.task = task;
			if ( when < 0 ) // Now
				this.when = System.currentTimeMillis();
			else if ( when < getEarliest() ) // Confirm this task won't come before any active entries unless it's async.
				throw ApplicationException.runtime( "Task must be in the future." );
			else
				this.when = when;
		}

		TaskEntry( @Nonnull Runnable task, boolean async )
		{
			this( task, -1, async );
		}

		TaskEntry( @Nonnull Runnable task )
		{
			this( task, -1 );
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

		@Override
		public boolean removesSafely()
		{
			// ParcelHandler uses the TaskEntry to unblock after runUnsafe is called.
			return !( task instanceof BlockingRunnable );
		}

		@Override
		void run0()
		{
			task.run();
		}
	}
}
