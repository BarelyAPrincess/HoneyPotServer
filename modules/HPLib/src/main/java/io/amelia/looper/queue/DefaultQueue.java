package io.amelia.looper.queue;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import io.amelia.lang.ApplicationException;
import io.amelia.looper.AbstractLooper;
import io.amelia.support.DateAndTime;

/**
 * Low-level class holding the list of {@link AbstractEntry entries} and sometimes {@link Runnable tasks}.
 * <p>
 * You can retrieve the looper for the current thread with {@link io.amelia.looper.LooperFactory#obtain()}
 * You can retrieve the queue for the looper from {@link AbstractLooper#getQueue()}
 */
public class DefaultQueue extends AbstractQueue
{
	protected final NavigableSet<AbstractEntry> entries = new TreeSet<>();
	/**
	 * We use {@link WeakReference} to prevent a circular reference that negates the benefit of the GC.
	 * Sometimes this isn't an issue but some JVMs ain't smart enough to detect these types of bugs.
	 */
	private WeakReference<AbstractLooper<DefaultQueue>.LooperControl> looperControl;

	public DefaultQueue( AbstractLooper<DefaultQueue>.LooperControl looperControl )
	{
		this.looperControl = new WeakReference<>( looperControl );
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
			AbstractEntry activeEntry = getActiveEntry();
			/* If a barrier is actively blocking the queue, wake the Looper. */
			if ( activeEntry != null && activeEntry instanceof EntryBarrier )
			{
				clearState();
				if ( !isQuitting() )
					wake();
			}

			/* Iterate over the pending entries. */
			entries.removeIf( entry -> entry instanceof EntryBarrier );
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
			AbstractEntry activeEntry = getActiveEntry();
			/* The barrier is actively blocking the queue, so wake the Looper. */
			if ( activeEntry != null && activeEntry instanceof EntryBarrier && activeEntry.getId() == id )
			{
				clearState();
				if ( !isQuitting() )
					wake();
				return;
			}

			/* Iterate over the pending entries. */
			Iterator<AbstractEntry> queueIterator = entries.iterator();
			while ( queueIterator.hasNext() )
			{
				AbstractEntry entry = queueIterator.next();
				if ( entry instanceof EntryBarrier && entry.getId() == id )
				{
					queueIterator.remove();
					return;
				}
			}

			throw new IllegalStateException( "The specified barrier id does not exist or has already been removed." );
		}
	}

	@Override
	public long getEarliestEntry()
	{
		AbstractEntry first = entries.first();
		return first == null ? 0L : first.getWhen();
	}

	@Override
	public long getLatestEntry()
	{
		AbstractEntry last = entries.last();
		return last == null ? Long.MAX_VALUE : last.getWhen();
	}

	AbstractLooper<DefaultQueue> getLooper()
	{
		return looperControl.get().getLooper();
	}

	AbstractLooper<DefaultQueue>.LooperControl getLooperControl()
	{
		return looperControl.get();
	}

	@Override
	public boolean hasPendingEntries()
	{
		return entries.size() > 0;
	}

	@Override
	public boolean isQuitting()
	{
		return getLooper().isQuitting();
	}

	@Override
	protected AbstractEntry pollNext()
	{
		return entries.pollFirst();
	}

	private EntryBarrier postBarrier( long when )
	{
		return postBarrier( when, null );
	}

	private EntryBarrier postBarrier( long when, Predicate<AbstractLooper> predicate )
	{
		/* Enqueue a new barrier. We don't need to wake the queue because the purpose of a barrier is to stall it. */
		synchronized ( this )
		{
			EntryBarrier barrier = new EntryBarrier( this, predicate, when );
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
	 * @return The instance of the {@link EntryBarrier} which can be used to cancel the barrier.
	 */
	public EntryBarrier postBarrier()
	{
		return postBarrier( null );
	}

	public EntryBarrier postBarrier( Predicate<AbstractLooper> predicate )
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
	public EntryCheckpoint postCheckpoint( BiPredicate<AbstractLooper, Boolean> predicate )
	{
		return postEntry( new EntryCheckpoint( this, predicate ) );
	}

	protected void postEntry0( AbstractEntry entry )
	{
		entries.add( entry );
	}

	@Override
	protected Result processEntry( AbstractEntry activeEntry, long now )
	{
		if ( activeEntry instanceof EntryCheckpoint )
		{
			// We filter out the remaining entries looking for anything besides just more CheckpointEntry instances.
			// This allows for all remaining CheckpointEntry instances that may be in a row to receive the same answer.
			boolean hasMoreEntries = entries.stream().filter( e -> !( e instanceof EntryCheckpoint ) ).count() > 0;

			BiPredicate<AbstractLooper, Boolean> predicate = ( ( EntryCheckpoint ) activeEntry ).predicate;

			// Based on the information provided, CheckpointEntry will decide if it would like to be rescheduled for a later time.
			if ( predicate.test( getLooper(), hasMoreEntries ) )
				postCheckpoint( predicate );

			// Reset the entry and go again.
			return null;
		}
		else if ( activeEntry instanceof EntryBarrier )
		{
			// Executes the inner Predicate contained in the barrier, removing it on False.
			( ( EntryBarrier ) activeEntry ).run();
			return Result.STALLED;
		}
		else
		{
			if ( now < activeEntry.getWhen() )
				return Result.WAITING;
			else
				return Result.SUCCESS;
		}
	}

	@Override
	public void quit( boolean removePendingMessages )
	{
		// Currently, there is nothing to do when we quitSafely.
		if ( !removePendingMessages )
			return;

		if ( !getLooper().isLockedByCurrentThread() )
			throw ApplicationException.runtime( "Looper must be locked by this thread to quit the LooperQueue." );

		final long now = System.currentTimeMillis();
		synchronized ( entries )
		{
			entries.removeIf( entry -> {
				if ( entry.isSafe() && entry.getWhen() > now )
				{
					entry.recycle();
					return true;
				}
				else
					return false;
			} );
		}
	}

	@Override
	public void wake()
	{
		entries.notifyAll();
	}
}
