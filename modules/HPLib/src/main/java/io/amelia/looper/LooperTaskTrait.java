package io.amelia.looper;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import io.amelia.foundation.BlockingTask;
import io.amelia.lang.ApplicationException;
import io.amelia.looper.queue.DefaultQueue;
import io.amelia.looper.queue.EntryRunnable;
import io.amelia.support.Objs;

public interface LooperTaskTrait
{
	DefaultQueue getQueue();

	/**
	 * Causes the Runnable task to be added to the {@link DefaultQueue}.
	 * The runnable will be run on the thread to which this queue is attached.
	 *
	 * @param task The Runnable that will be executed.
	 *
	 * @return Returns an {@link TaskEntry} instance that references the enqueued runnable.
	 */
	default TaskEntry postTask( LooperTask task )
	{
		DefaultQueue queue = getQueue();
		return queue.postEntry( new TaskEntry( queue, task ) );
	}

	default TaskEntry postTaskAsync( LooperTask task )
	{
		DefaultQueue queue = getQueue();
		return queue.postEntry( new TaskEntry( queue, task, true ) );
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
	 * @see #postTaskAtAsync(LooperTask, long)
	 */
	default TaskEntry postTaskAt( LooperTask task, long when )
	{
		DefaultQueue queue = getQueue();
		return queue.postEntry( new TaskEntry( queue, task, when ) );
	}

	/**
	 * @see #postTaskAt(LooperTask, long)
	 */
	default TaskEntry postTaskAtAsync( LooperTask task, long when )
	{
		DefaultQueue queue = getQueue();
		return queue.postEntry( new TaskEntry( queue, task, when, true ) );
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
	 * @see #postTaskLaterAsync(LooperTask, long)
	 */
	default TaskEntry postTaskLater( LooperTask task, long delay )
	{
		DefaultQueue queue = getQueue();
		return queue.postEntry( new TaskEntry( queue, task, System.currentTimeMillis() + delay ) );
	}

	/**
	 * @see #postTaskLater(LooperTask, long)
	 */
	default TaskEntry postTaskLaterAsync( LooperTask task, long delay )
	{
		DefaultQueue queue = getQueue();
		return queue.postEntry( new TaskEntry( queue, task, System.currentTimeMillis() + delay, true ) );
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
	 * @see #postTaskAsync(LooperTask)
	 */
	default TaskEntry postTaskNext( LooperTask task )
	{
		DefaultQueue queue = getQueue();
		return queue.postEntry( new TaskEntry( queue, task, queue.getEarliestEntry() + 1L ) );
	}

	/**
	 * @see #postTaskNext(LooperTask)
	 */
	default TaskEntry postTaskNextAsync( LooperTask task )
	{
		DefaultQueue queue = getQueue();
		return queue.postEntry( new TaskEntry( queue, task, queue.getEarliestEntry() + 1L, true ) );
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
	default <E extends Exception> boolean postTaskUnsafe( @Nonnull LooperTask<E> task, @Nonnegative long timeout ) throws E
	{
		if ( isThreadJoined() )
		{
			task.execute();
			return true;
		}

		BlockingTask blockingRunnable = new BlockingTask( task );
		postTask( blockingRunnable );
		return blockingRunnable.postAndWait( timeout );
	}

	class TaskEntry extends EntryRunnable
	{
		private final LooperTask task;
		private final long when;

		TaskEntry( @Nonnull DefaultQueue queue, @Nonnull LooperTask task, long when )
		{
			this( queue, task, when, false );
		}

		TaskEntry( @Nonnull DefaultQueue queue, @Nonnull LooperTask task, long when, boolean async )
		{
			super( queue, async );

			Objs.notNull( task );

			this.task = task;
			if ( when < 0 ) // Now
				this.when = System.currentTimeMillis();
			else if ( when < queue.getEarliestEntry() ) // Confirm this task won't come before any active entries unless it's async.
				throw ApplicationException.runtime( "Task must be in the future." );
			else
				this.when = when;
		}

		TaskEntry( @Nonnull DefaultQueue queue, @Nonnull LooperTask task, boolean async )
		{
			this( queue, task, -1, async );
		}

		TaskEntry( @Nonnull DefaultQueue queue, @Nonnull LooperTask task )
		{
			this( queue, task, -1 );
		}

		@Override
		public long getWhen()
		{
			return when;
		}

		@Override
		public void recycle()
		{

		}

		/**
		 * Can this entry be removed without causing major bugs?
		 * We use this TaskEntry to unblock after {@link LooperTaskTrait#postTaskUnsafe(LooperTask, long)} is called.
		 *
		 * @return True if so.
		 */
		@Override
		public boolean removesSafely()
		{
			return !( task instanceof BlockingTask );
		}

		@Override
		protected void run0() throws Exception
		{
			task.execute();
		}
	}
}
