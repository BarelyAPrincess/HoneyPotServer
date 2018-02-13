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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import io.amelia.foundation.ConfigRegistry;
import io.amelia.foundation.Kernel;
import io.amelia.looper.queue.AbstractEntry;
import io.amelia.looper.queue.AbstractQueue;

/**
 * The Looper is intended to be interfaced by the thread that intends to execute tasks or oversee the process.
 */
public abstract class AbstractLooper<Q extends AbstractQueue>
{
	/**
	 * Used to increment an unique global number.
	 */
	private static volatile AtomicLong UNIQUE = new AtomicLong( 0L );

	/**
	 * Returns a globally unique id number, generally used for queue entry id numbers.
	 *
	 * @return Global unique id number, only limited by {@link Long#MAX_VALUE}.
	 */
	public static long getGloballyUniqueId()
	{
		return UNIQUE.getAndIncrement();
	}

	/**
	 * List of threads that were spawned by this Looper.
	 * Used to obtain() this Looper from a async thread.
	 */
	private final List<WeakReference<Thread>> aliasThreads = new ArrayList<>();
	/**
	 * The Looper Flags
	 */
	private final EnumSet<Flag> flags = EnumSet.noneOf( Flag.class );
	/**
	 * Used to synchronize certain methods with the loop, so to avoid concurrent and/or race issues
	 */
	private final ReentrantLock lock = new ReentrantLock();
	private final Condition blockingCondition = lock.newCondition();
	/**
	 * States the average millis between iterations.
	 */
	private long averagePolledMillis = 0L;
	/**
	 * Retains a handler used for when exceptions are thrown by the looper.
	 * If the handler is null, we forward them to the Kernel and quit the looper.
	 */
	private Consumer<Exception> exceptionHandler;
	/**
	 * Indicates the Looper is overloaded.
	 */
	private boolean isOverloaded = false;
	/**
	 * Indicates the Looper is preparing to quit.
	 */
	private boolean isQuitting = false;
	/**
	 * Stores the amount of time that has past between iterations.
	 */
	private long lastPolledMillis = 0L;
	/**
	 * The Looper Queue
	 */
	private Q queue;
	/**
	 * Reference to the thread running this Looper.
	 * Remains null until {@link #joinLoop()} is called.
	 */
	private Thread thread = null;

	public AbstractLooper( Flag... flags )
	{
		this.flags.addAll( Arrays.asList( flags ) );
	}

	public long getAveragePolledMillis()
	{
		return averagePolledMillis;
	}

	/**
	 * Gets the Thread running this Looper.
	 */
	public Thread getJoinedThread()
	{
		return thread;
	}

	public long getLastPolledMillis()
	{
		return lastPolledMillis;
	}

	protected LooperControl getLooperControl()
	{
		return new LooperControl();
	}

	public String getName()
	{
		return "Looper " + getJoinedThread().getName();
	}

	/**
	 * Get the {@link AbstractQueue} associated with this {@link AbstractLooper}
	 */
	public Q getQueue()
	{
		return queue;
	}

	protected void setQueue( Q queue )
	{
		this.queue = queue;
	}

	public boolean hasFlag( Flag flag )
	{
		return flags.contains( flag );
	}

	public boolean isAsync()
	{
		return false;
	}

	public abstract boolean isDisposed();

	/**
	 * Returns true if this Looper belongs to this the current thread.
	 * Will also check child threads and return true as well.
	 *
	 * @return True if the current thread is the same as the one used in {@link #joinLoop()}
	 */
	public boolean isHeldByCurrentThread()
	{
		if ( thread == null )
			return false;
		Thread currentThread = Thread.currentThread();
		if ( thread == currentThread )
			return true;
		synchronized ( aliasThreads )
		{
			for ( WeakReference<Thread> threadReference : aliasThreads )
				if ( threadReference.get() == null )
					aliasThreads.remove( threadReference );
				else if ( threadReference.get() == currentThread )
					return true;
		}
		return false;
	}

	public boolean isLockedByCurrentThread()
	{
		return lock.isHeldByCurrentThread();
	}

	/**
	 * Returns true if this Looper has an average millis of over 100ms with each iteration.
	 */
	public boolean isOverloaded()
	{
		return isOverloaded;
	}

	public abstract boolean isPermitted( AbstractEntry entry );

	/**
	 * Returns true if this Looper is currently working on quitting.
	 * No more tasks will be accepted.
	 */
	public boolean isQuitting()
	{
		return isQuitting;
	}

	/**
	 * Determines if the looper is currently active with a joined thread.
	 *
	 * @return True is so, false otherwise.
	 *
	 * @see #joinLoop()
	 */
	public boolean isThreadJoined()
	{
		return thread != null;
	}

	/**
	 * Joins the thread to this looper until it quits.
	 */
	public void joinLoop()
	{
		thread = Thread.currentThread();

		// Attempt to acquire the lock on the Looper, as to force outside calls to only process while Looper is asleep.
		lock.lock();
		try
		{
			signalPreJoinLoop();

			// Stores the last time the overload warning was displayed as to not flood the console.
			long lastWarningMillis = 0L;

			// Stores the last time the overload wait was called as to not delay the system all the more.
			long lastOverloadMillis = 0L;

			for ( ; ; )
			{
				// Stores when the loop started.
				final long loopStartMillis = System.currentTimeMillis();

				tick( loopStartMillis );

				// Update the time taken during this iteration.
				lastPolledMillis = System.currentTimeMillis() - loopStartMillis;

				// Prevent negative numbers and warn
				if ( lastPolledMillis < 0L )
				{
					Kernel.L.warning( "[" + getName() + "] Time ran backwards! Did the system time change?" );
					lastPolledMillis = 0L;
				}

				// Update the average millis once we know the lastPolledMillis from this last iteration
				averagePolledMillis = ( Math.min( lastPolledMillis, averagePolledMillis ) - Math.max( lastPolledMillis, averagePolledMillis ) ) / 2;

				// Are we on average taking more than 100ms per iteration and has it been more than 5 seconds since last overload warning?
				if ( averagePolledMillis > 100L )
				{
					if ( loopStartMillis - lastWarningMillis >= 15000L && ConfigRegistry.config.isTrue( ConfigRegistry.ConfigKeys.WARN_ON_OVERLOAD ) )
					{
						Kernel.L.warning( "[" + getName() + "] Can't keep up! Did the system time change, or is it overloaded?" );
						lastWarningMillis = loopStartMillis;
					}
					isOverloaded = true;
				}
				else
					isOverloaded = false;

				// Cycle time was under the 50 millis minimum, so we wait the remainder of time. This also gives the Looper a chance to process awaiting calls.
				if ( lastPolledMillis < 50L )
					blockingCondition.await( 50 - lastPolledMillis, TimeUnit.MILLISECONDS );

				// If we are overloaded and the last time we processed calls was over 1 second ago, a force the Looper to momentarily sleep for 20 millis.
				// Technically, this only stands to make an overloaded application, even more overloaded, but it's a necessary evil.
				if ( isOverloaded && loopStartMillis - lastOverloadMillis > 1000L )
				{
					blockingCondition.await( 20, TimeUnit.MILLISECONDS );
					lastOverloadMillis = loopStartMillis;
				}

				// Process the quit message now that all pending messages have been handled.
				if ( isQuitting() )
				{
					quitFinal();
					break;
				}

				// Otherwise we go immediately to the next iteration.
			}
		}
		catch ( Throwable t )
		{
			Kernel.handleExceptions( t );
		}
		finally
		{
			signalPostJoinLoop();
			queue.clearState();
			lock.unlock();
			thread = null;
		}
	}

	protected void quit( boolean removePendingMessages )
	{
		// If we're already quitting or have been disposed, return immediately.
		if ( isQuitting() || isDisposed() )
			return;

		lock.lock();
		try
		{
			isQuitting = true;

			queue.quit( removePendingMessages );

			if ( isThreadJoined() && queue.isBlocking() )
				queue.wake();
			if ( !isThreadJoined() )
				quitFinal();
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Quits the looper.
	 * <p>
	 * Causes the {@link #joinLoop()} method to terminate without processing any more messages in the queue.
	 * <p>
	 * Using this method may be unsafe because some messages may not be delivered
	 * before the looper terminates.  Consider using {@link #quitSafely} instead to ensure
	 * that all pending work is completed in an orderly manner.
	 *
	 * @see #quitSafely
	 */
	public final void quitUnsafe()
	{
		quit( true );
	}

	/**
	 * Does some final tasks before the Looper is permanently disposed of.
	 */
	protected abstract void quitFinal();

	/**
	 * Quits the looper safely.
	 * <p>
	 * Causes the {@link #joinLoop()} method to terminate as soon as all remaining messages
	 * in the queue that are already due to be delivered have been handled.
	 * However pending delayed messages with due times in the future will not be
	 * delivered before the loop terminates.
	 */
	public final void quitSafely()
	{
		quit( false );
	}

	public void setExceptionHandler( Consumer<Exception> exceptionHandler )
	{
		this.exceptionHandler = exceptionHandler;
	}

	protected void signalInfallibleStartup()
	{

	}

	protected void signalPostJoinLoop()
	{

	}

	protected void signalPreJoinLoop()
	{

	}

	protected abstract void tick( long loopStartMillis );

	@Override
	public String toString()
	{
		return "Looper (" + thread.getName() + ", threadId " + thread.getId() + ") {" + Integer.toHexString( System.identityHashCode( this ) ) + "}";
	}

	/**
	 * Looper Property Flags
	 */
	public enum Flag
	{
		/**
		 * Indicates the Looper is used for internal system tasks only, which includes but not limited to,
		 * the Main Loop, User Logins, Permissions, Log Subsystem, Networking, and more.
		 * System Loopers can not be terminated (or auto-quit) and will only shutdown when the entire application does.
		 */
		SYSTEM,
		/**
		 * Indicates the Looper is exclusive to a plugin loaded by the application and handles things such as data mining and analysis.
		 */
		PLUGIN,
		/**
		 * Indicates the Looper will auto-quit once the queue is empty.
		 */
		AUTO_QUIT
	}

	/**
	 * LooperControl exposes sensitive and/or volatile methods and fields outside the package.
	 */
	public class LooperControl
	{
		private LooperControl()
		{
			// Can only be instigated by implementing subclasses of AbstractLooper.
		}

		public void addChildThread( Thread thread )
		{
			synchronized ( aliasThreads )
			{
				for ( WeakReference<Thread> reference : aliasThreads )
					if ( reference.get() == null )
						aliasThreads.remove( reference );

				aliasThreads.add( new WeakReference<>( thread ) );
				thread.setName( getJoinedThread().getName() + "-" + aliasThreads.size() );
			}
		}

		public AbstractLooper<Q> getLooper()
		{
			return AbstractLooper.this;
		}

		public void handleException( Exception exception )
		{
			if ( exceptionHandler == null )
			{
				Kernel.handleExceptions( exception );
				quitUnsafe();
			}
			else
				exceptionHandler.accept( exception );
		}

		public void removeChildThread( Thread thread )
		{
			synchronized ( aliasThreads )
			{
				for ( WeakReference<Thread> threadReference : aliasThreads )
					if ( threadReference.get() == null || threadReference.get() == thread )
						aliasThreads.remove( threadReference );
			}
		}

		/**
		 * Executes {@link Runnable} on a new thread, i.e., async.
		 * <p>
		 * We also add the new thread to the aliases, such that calls to
		 * the {@link LooperFactory#obtain()} returns this {@link AbstractLooper}.
		 * <p>
		 * This also prevents a async task from creating a new Looper by accident.
		 */
		public void runAsync( Runnable task )
		{
			Kernel.getExecutorParallel().execute( () -> {
				Thread thread = Thread.currentThread();
				addChildThread( thread );
				task.run();
				removeChildThread( thread );
			} );
		}

		public void signalInfallibleStartup()
		{
			AbstractLooper.this.signalInfallibleStartup();
		}
	}
}
