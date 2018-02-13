/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
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

	default boolean isHeldByCurrentThread()
	{
		return false;
	}

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
		return postTask( task, false );
	}

	default TaskEntry postTask( LooperTask task, boolean async )
	{
		DefaultQueue queue = getQueue();
		return queue.postEntry( new TaskEntry( queue, task, async ) );
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
	 * @see #postTaskAt(LooperTask, long)
	 */
	default TaskEntry postTaskAt( LooperTask task, long when )
	{
		return postTaskAt( task, when, false );
	}

	/**
	 * @see #postTaskAt(LooperTask, long)
	 */
	default TaskEntry postTaskAt( LooperTask task, long when, boolean async )
	{
		DefaultQueue queue = getQueue();
		return queue.postEntry( new TaskEntry( queue, task, when, async ) );
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
	 * @see #postTaskLater(LooperTask, long)
	 */
	default TaskEntry postTaskLater( LooperTask task, long delay )
	{
		return postTaskLater( task, delay, false );
	}

	/**
	 * @see #postTaskLater(LooperTask, long)
	 */
	default TaskEntry postTaskLater( LooperTask task, long delay, boolean async )
	{
		DefaultQueue queue = getQueue();
		return queue.postEntry( new TaskEntry( queue, task, System.currentTimeMillis() + delay, async ) );
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
	 * @see #postTask(LooperTask)
	 */
	default TaskEntry postTaskNow( LooperTask task )
	{
		return postTaskNow( task, false );
	}

	/**
	 * @see #postTaskNow(LooperTask)
	 */
	default TaskEntry postTaskNow( LooperTask task, boolean async )
	{
		DefaultQueue queue = getQueue();
		return queue.postEntry( new TaskEntry( queue, task, queue.getEarliestEntry() + 1L, async ) );
	}

	default RepeatingTaskEntry postTaskRepeating( LooperTask task, long delay )
	{
		return postTaskRepeating( task, delay, false );
	}

	default RepeatingTaskEntry postTaskRepeating( LooperTask task, long delay, boolean async )
	{
		DefaultQueue queue = getQueue();
		return queue.postEntry( new RepeatingTaskEntry( queue, task, delay, async ) );
	}

	default RepeatingTaskEntry postTaskRepeatingAt( LooperTask task, long when, long delay )
	{
		return postTaskRepeatingAt( task, when, delay, false );
	}

	default RepeatingTaskEntry postTaskRepeatingAt( LooperTask task, long when, long delay, boolean async )
	{
		DefaultQueue queue = getQueue();
		return queue.postEntry( new RepeatingTaskEntry( queue, task, when, delay, async ) );
	}

	default RepeatingTaskEntry postTaskRepeatingLater( LooperTask task, long whenDelay, long delay )
	{
		return postTaskRepeatingLater( task, whenDelay, delay, false );
	}

	default RepeatingTaskEntry postTaskRepeatingLater( LooperTask task, long whenDelay, long delay, boolean async )
	{
		DefaultQueue queue = getQueue();
		return queue.postEntry( new RepeatingTaskEntry( queue, task, System.currentTimeMillis() + whenDelay, delay, async ) );
	}

	default RepeatingTaskEntry postTaskRepeatingNext( LooperTask task, long delay )
	{
		return postTaskRepeatingNext( task, delay, false );
	}

	default RepeatingTaskEntry postTaskRepeatingNext( LooperTask task, long delay, boolean async )
	{
		DefaultQueue queue = getQueue();
		return queue.postEntry( new RepeatingTaskEntry( queue, task, queue.getEarliestEntry() + 1L, delay, async ) );
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
		Objs.notNull( task );
		Objs.notNegative( timeout );

		if ( isHeldByCurrentThread() )
		{
			task.execute();
			return true;
		}

		BlockingTask blockingRunnable = new BlockingTask( task );
		postTask( blockingRunnable );
		return blockingRunnable.postAndWait( timeout );
	}

	class RepeatingTaskEntry extends TaskEntry
	{
		private long delay;

		public RepeatingTaskEntry( @Nonnull DefaultQueue queue, @Nonnull LooperTask task, long when, long delay )
		{
			super( queue, task, when );
			setDelay( delay );
		}

		public RepeatingTaskEntry( @Nonnull DefaultQueue queue, @Nonnull LooperTask task, long when, long delay, boolean async )
		{
			super( queue, task, when, async );
			setDelay( delay );
		}

		public RepeatingTaskEntry( @Nonnull DefaultQueue queue, @Nonnull LooperTask task, long delay, boolean async )
		{
			super( queue, task, async );
			setDelay( delay );
		}

		public RepeatingTaskEntry( @Nonnull DefaultQueue queue, @Nonnull LooperTask task, long delay )
		{
			super( queue, task );
			setDelay( delay );
		}

		public long getDelay()
		{
			return delay;
		}

		public void setDelay( @Nonnegative long delay )
		{
			if ( delay < 50 )
				throw new IllegalArgumentException( "RepeatingTask delay can't be less than 50 milliseconds. Anything less can cause looper lag issues." );
			this.delay = delay;
		}

		@Override
		protected void run0() throws ApplicationException.Error
		{
			super.run0();

			// Repeat entry unless the queue is quitting.
			if ( !queue.isQuitting() )
				queue.postEntry( new RepeatingTaskEntry( queue, task, when + delay, delay, isAsync() ) );
		}
	}

	class TaskEntry extends EntryRunnable
	{
		protected final LooperTask task;
		protected final long when;

		public TaskEntry( @Nonnull DefaultQueue queue, @Nonnull LooperTask task, long when )
		{
			this( queue, task, when, false );
		}

		public TaskEntry( @Nonnull DefaultQueue queue, @Nonnull LooperTask task, long when, boolean async )
		{
			super( queue, async );

			Objs.notNull( task );

			this.task = task;
			if ( when <= 0 ) // Now
				this.when = System.currentTimeMillis();
				// else if ( when < queue.getEarliestEntry() && !async ) // Confirm this task won't come before any active entries unless it's async.
				// throw ApplicationException.runtime( "Task must be in the future. {When: " + when + ", Earliest: " + queue.getEarliestEntry() + "}" );
			else
				this.when = when;
		}

		public TaskEntry( @Nonnull DefaultQueue queue, @Nonnull LooperTask task, boolean async )
		{
			this( queue, task, -1, async );
		}

		public TaskEntry( @Nonnull DefaultQueue queue, @Nonnull LooperTask task )
		{
			this( queue, task, -1 );
		}

		@Override
		public long getWhen()
		{
			return when;
		}

		/**
		 * Can this entry be removed without causing major bugs?
		 * We use this TaskEntry to unblock after {@link LooperTaskTrait#postTaskUnsafe(LooperTask, long)} is called.
		 *
		 * @return True if so.
		 */
		@Override
		public boolean isSafe()
		{
			return !( task instanceof BlockingTask );
		}

		@Override
		public void recycle()
		{

		}

		@Override
		protected void run0() throws ApplicationException.Error
		{
			try
			{
				task.execute();
			}
			catch ( ApplicationException.Error e )
			{
				throw e;
			}
			catch ( Exception e )
			{
				throw ApplicationException.error( e );
			}
		}
	}
}
