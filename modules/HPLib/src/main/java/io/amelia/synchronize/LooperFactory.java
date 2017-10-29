package io.amelia.synchronize;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.amelia.android.Handler;
import io.amelia.android.Looper;
import io.amelia.foundation.Kernel;
import io.amelia.lang.SynchronizeException;
import io.amelia.support.Timings;

public class LooperFactory
{
	private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
	/**
	 * An {@link Executor} that can be used to execute tasks in parallel.
	 */
	private static final Executor EXECUTOR_PARALLEL;
	/**
	 * An {@link Executor} that executes tasks one at a time in serial
	 * order.  This serialization is global to a particular process.
	 */
	private static final Executor EXECUTOR_SERIAL;
	private static final int KEEP_ALIVE_SECONDS = 30;
	private static final ThreadLocal<Looper> LOOPERS = new ThreadLocal<>();
	// We want at least 2 threads and at most 4 threads in the core pool,
	// preferring to have 1 less than the CPU count to avoid saturating
	// the CPU with background work
	private static final int THREAD_POOL_SIZE_CORE = Math.max( 4, Math.min( CPU_COUNT - 1, 1 ) );
	private static final int THREAD_ROOL_SIZE_MAXIMUM = CPU_COUNT * 2 + 1;
	// User threads are not permitted to run a Looper as they are the equivalent to an Android UI thread.
	private static final ThreadLocal<Boolean> USER_THREADS = new ThreadLocal<>();
	private static final List<LooperQueue> looperQueuePool = new ArrayList<>();
	private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<>( 128 );
	private static final ThreadFactory sThreadFactory = new ThreadFactory()
	{
		private final AtomicInteger mCount = new AtomicInteger( 1 );

		@Override
		public Thread newThread( Runnable r )
		{
			return new Thread( r, "Looper #" + String.format( "%d04", mCount.getAndIncrement() ) );
		}
	};

	// Main Looper runs on the main thread, i.e., the thread that started the Kernel
	private static Looper mainLooper;

	static
	{
		ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor( THREAD_POOL_SIZE_CORE, THREAD_ROOL_SIZE_MAXIMUM, KEEP_ALIVE_SECONDS, TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory );
		threadPoolExecutor.allowCoreThreadTimeOut( true );
		EXECUTOR_PARALLEL = threadPoolExecutor;

		EXECUTOR_SERIAL = new Executor()
		{
			final ArrayDeque<Runnable> mTasks = new ArrayDeque<>();
			Runnable mActive;

			public synchronized void execute( final Runnable r )
			{
				mTasks.offer( () -> {
					try
					{
						r.run();
					}
					finally
					{
						scheduleNext();
					}
				} );
				if ( mActive == null )
				{
					scheduleNext();
				}
			}

			protected synchronized void scheduleNext()
			{
				if ( ( mActive = mTasks.poll() ) != null )
				{
					EXECUTOR_PARALLEL.execute( mActive );
				}
			}
		};
	}

	public static Executor getExecutorParallel()
	{
		return EXECUTOR_PARALLEL;
	}

	public static Executor getExecutorSerial()
	{
		return EXECUTOR_SERIAL;
	}

	public static Looper getMainLooper()
	{
		return mainLooper;
	}

	public static boolean isUserThread()
	{
		return USER_THREADS.get();
	}

	public static void setUserThread( boolean isUserThread )
	{
		USER_THREADS.set( isUserThread );
	}

	/**
	 * Gets the Looper associated with the currently running thread.
	 *
	 * @return
	 */
	public static Looper myLooper() throws SynchronizeException
	{
		if ( Kernel.isPrimaryThread() )
		{
			if ( mainLooper == null )
				throw new SynchronizeException( "The main Looper was not prepared." );
			return mainLooper;
		}

		if ( LOOPERS.get() == null )
			throw new SynchronizeException( "The Looper for thread " + Thread.currentThread().getName() + " was not prepared." );

		return LOOPERS.get();
	}

	public static void prepare()
	{
		synchronized ( Looper.class )
		{
			if ( Kernel.isPrimaryThread() )
			{
				if ( mainLooper != null )
					throw new IllegalStateException( "The main Looper has already been prepared." );
				mainLooper = new Looper( false );
			}
			else
			{
				if ( LOOPERS.get() != null )
					throw new IllegalStateException( "The Looper for thread " + Thread.currentThread().getName() + " has already been prepared." );
				LOOPERS.set( new Looper( true ) );
			}
		}
	}

	public static void setLooperPool( int cnt )
	{
		// set looperQueuePool size
	}

	private LooperFactory()
	{
		// Static
	}

	private static class LooperQueue implements Runnable
	{
		private final ArrayList<IdleHandler> mIdleHandlers = new ArrayList<>();
		volatile NavigableMap<Long, Message> queuedMessages = new TreeMap<>();
		private Thread currentThread;
		private boolean isQuitting = false;
		// Indicates whether next() is blocked waiting in pollOnce() with a non-zero timeout.
		private boolean mBlocked;
		private IdleHandler[] mPendingIdleHandlers;
		private boolean quitAllowed;

		LooperQueue( boolean quitAllowed )
		{
			this.quitAllowed = quitAllowed;
		}

		/**
		 * Thread Safe
		 */
		public void awakeQueue()
		{
			if ( isPolling() )
				throw new IllegalStateException( "The LooperQueue is already polling." );
			EXECUTOR_PARALLEL.execute( this );
		}

		boolean enqueueMessage( Message msg, long when )
		{
			if ( msg.target == null )
				throw new IllegalArgumentException( "Message must have a target." );
			if ( msg.isInUse() )
				throw new IllegalStateException( msg + " This message is already in use." );

			synchronized ( this )
			{
				if ( isQuitting )
				{
					IllegalStateException e = new IllegalStateException( msg.target + " sending message to a Handler on a dead thread" );
					Kernel.L.warning( e.getMessage(), e );
					msg.recycle();
					return false;
				}

				msg.markInUse();
				msg.when = when;

				queuedMessages.put( when, msg );
				boolean needWake;

				Message peak = queuedMessages.firstEntry().getValue();
				// If they are the same then we know this msg was the next to be handled.
				if ( msg == peak )
					needWake = mBlocked;
				else
					needWake = mBlocked && peak.target == null && msg.isAsynchronous();

				if ( needWake )
					awakeQueue();
			}
			return true;
		}

		/**
		 * Gets the Thread associated with this Looper.
		 * Thread Safe
		 *
		 * @return The looper's thread.
		 */
		public Thread getThread()
		{
			return currentThread;
		}

		/**
		 * Returns true if the current thread is this looper's thread.
		 */
		public boolean isCurrentThread()
		{
			return Thread.currentThread() == currentThread;
		}

		/**
		 * @return
		 */
		public boolean isQuitting()
		{
			return isQuitting;
		}

		Message next()
		{
			int pendingIdleHandlerCount = -1; // -1 only during first iteration
			int nextPollTimeoutMillis = 0;
			for ( ; ; )
			{
				// if ( nextPollTimeoutMillis != 0 )
				// Binder.flushPendingCommands();

				synchronized ( this )
				{
					final long now = Timings.epoch();

					Map.Entry<Long, Message> next = queuedMessages.firstEntry();
					Message msg = null;

					// Barrier Stall
					if ( next != null )
						if ( next.getValue().target == null )
							msg = queuedMessages.values().stream().filter( Message::isAsynchronous ).sorted().findFirst().orElse( null );
						else
							msg = next.getValue();

					// Try to retrieve the next message.  Return if found.
					if ( msg != null )
					{
						if ( now < msg.when )
						{
							// Next msg is not ready.  Set a timeout to wake up when it is ready.
							nextPollTimeoutMillis = ( int ) Math.min( msg.when - now, Integer.MAX_VALUE );
						}
						else
						{
							// Got a message.
							mBlocked = false;
							msg.next = null;
							Kernel.L.debug( "Returning message: " + msg );
							msg.markInUse();
							return msg;
						}
					}
					else
					{
						// No more messages.
						nextPollTimeoutMillis = -1;
					}

					// Process the quit message now that all pending messages have been handled.
					if ( isQuitting )
					{
						// dispose();
						return null;
					}

					// If first time idle, then get the number of idlers to run.
					// Idle handles only run if the queue is empty or if the first message
					// in the queue (possibly a barrier) is due to be handled in the future.
					if ( pendingIdleHandlerCount < 0 && ( queuedMessages.isEmpty() || now < queuedMessages.firstEntry().getValue().when ) )
						pendingIdleHandlerCount = mIdleHandlers.size();

					if ( pendingIdleHandlerCount <= 0 )
					{
						// No idle handlers to run.  Loop and wait some more.
						mBlocked = true;
						continue;
					}

					if ( mPendingIdleHandlers == null )
						mPendingIdleHandlers = new IdleHandler[Math.max( pendingIdleHandlerCount, 4 )];

					mPendingIdleHandlers = mIdleHandlers.toArray( mPendingIdleHandlers );
				}

				// Run the idle handlers.
				// We only ever reach this code block during the first iteration.
				for ( int i = 0; i < pendingIdleHandlerCount; i++ )
				{
					final IdleHandler idler = mPendingIdleHandlers[i];
					mPendingIdleHandlers[i] = null; // release the reference to the handler

					boolean keep = false;
					try
					{
						keep = idler.queueIdle();
					}
					catch ( Throwable t )
					{
						Kernel.L.severe( "IdleHandler throw", t );
					}

					if ( !keep )
					{
						synchronized ( this )
						{
							mIdleHandlers.remove( idler );
						}
					}
				}

				// Reset the idle handler count to 0 so we do not run them again.
				pendingIdleHandlerCount = 0;

				// While calling an idle handler, a new message could have been delivered
				// so go back and look again for a pending message without waiting.
				nextPollTimeoutMillis = 0;
			}
		}

		/**
		 * Quits the looper queue.
		 * <p>
		 * Causes the {@link #loop} method to terminate without processing any
		 * more messages in the message queue.
		 * <p>
		 * Any attempt to post messages to the queue after the looper is asked to quit will fail.
		 * For example, the {@link Handler#sendMessage(Message)} method will return false.
		 * <p>
		 * Using this method may be unsafe because some messages may not be delivered
		 * before the looper terminates.  Consider using {@link #quitSafely} instead to ensure
		 * that all pending work is completed in an orderly manner.
		 *
		 * @see #quitSafely
		 */
		public void quit()
		{

		}

		/**
		 * Quits the looper queue safely.
		 * <p>
		 * Causes the {@link #loop} method to terminate as soon as all remaining messages
		 * in the message queue that are already due to be delivered have been handled.
		 * However pending delayed messages with due times in the future will not be
		 * delivered before the loop terminates.
		 * <p>
		 * Any attempt to post messages to the queue after the looper is asked to quit will fail.
		 * For example, the {@link Handler#sendMessage(Message)} method will return false.
		 */
		public void quitSafely()
		{

		}

		@Override
		public void run()
		{
			currentThread = Thread.currentThread();

			next();
		}

		@Override
		public String toString()
		{
			return "LooperQueue (" + currentThread.getName() + ", tid " + currentThread.getId() + ") {" + Integer.toHexString( System.identityHashCode( this ) ) + "}";
		}

		/**
		 * Callback interface for discovering when a thread is going to block
		 * waiting for more messages.
		 */
		public interface IdleHandler
		{
			/**
			 * Called when the message queue has run out of messages and will now
			 * wait for more.  Return true to keep your idle handler active, false
			 * to have it removed.  This may be called if there are still messages
			 * pending in the queue, but they are all scheduled to be dispatched
			 * after the current time.
			 */
			boolean queueIdle();
		}
	}

	public static class LooperTick
	{
		final ArrayDeque<Runnable> mTasks = new ArrayDeque<>();
		Runnable mActive;

		public synchronized void execute( final Runnable r )
		{
			mTasks.offer( () -> {
				try
				{
					r.run();
				}
				finally
				{
					scheduleNext();
				}
			} );
			if ( mActive == null )
				scheduleNext();
		}

		protected synchronized void scheduleNext()
		{
			if ( ( mActive = mTasks.poll() ) != null )
				EXECUTOR_PARALLEL.execute( mActive );
		}
	}
}
