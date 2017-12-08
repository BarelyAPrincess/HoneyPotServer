package io.amelia.foundation;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.lang.ApplicationException;
import io.amelia.logcompat.LogBuilder;
import io.amelia.logcompat.Logger;
import io.amelia.support.Objs;

/**
 * The Looper is intended to be interfaced by the thread that intends to execute tasks or oversee the process.
 */
public class Looper
{
	private static final Logger LOG = LogBuilder.get( Looper.class );
	/**
	 * Used to globally increment internally unique numbers
	 */
	private static volatile AtomicLong UNIQUE = new AtomicLong( 0L );
	/**
	 * Stores the Loopers
	 */
	private static volatile NavigableSet<Looper> loopers = new TreeSet<>();

	public static long getGloballyUniqueId()
	{
		long id = UNIQUE.getAndIncrement();
		/*
		 * Just as a safety check in case the app is running for like a millennia.
		 * Should probably also check that we don't encounter in use numbers but that's probably even more rare.
		 */
		if ( Long.MAX_VALUE - id == 0 )
			UNIQUE.set( 0L );
		return id;
	}

	/**
	 * The Looper Queue
	 */
	final LooperQueue queue = new LooperQueue( this );
	/**
	 * Used to synchronize certain methods with the loop, so to avoid concurrent calls
	 */
	private final Object lock = new Object();
	/**
	 * List of threads that were spawned by this Looper.
	 * Used to obtain() this Looper from a async thread.
	 */
	private List<WeakReference<Thread>> aliasThreads = new ArrayList<>();
	/**
	 * States the average millis between iterations.
	 */
	private long averagePolledMillis = 0L;
	/**
	 * The Looper Flags
	 */
	private EnumSet<Flag> flags = EnumSet.noneOf( Flag.class );
	/**
	 * Idle Handlers that run when the queue is empty
	 */
	private Map<Long, Predicate<Looper>> idleHandlers = new HashMap<>();
	/**
	 * Indicates the Looper is overloaded.
	 */
	private boolean isOverloaded = false;
	/**
	 * Stores the amount of time that has past between iterations.
	 */
	private long lastPolledMillis = 0L;
	/**
	 * The Looper state
	 */
	private EnumSet<State> states = EnumSet.noneOf( State.class );
	/**
	 * Reference to the thread running this Looper.
	 * Remains null until {@link #joinLoop()} is called.
	 */
	private Thread thread = null;

	Looper( Flag... flags )
	{
		this.flags.addAll( Arrays.asList( flags ) );
	}

	void addChildThread( Thread thread )
	{
		synchronized ( aliasThreads )
		{
			for ( WeakReference<Thread> reference : aliasThreads )
				if ( reference.get() == null )
					aliasThreads.remove( reference );

			aliasThreads.add( new WeakReference<>( thread ) );
			thread.setName( getThread().getName() + "-" + aliasThreads.size() );
		}
	}

	public void addFlag( Flag flag )
	{
		if ( isRunning() )
			throw ApplicationException.ignorable( "You can't modify Looper flags while it's running." );
		if ( flag == Flag.SYSTEM )
			throw ApplicationException.runtime( "System Loopers are the domain of the application Kernel." );
		if ( flag == Flag.PLUGIN )
			throw ApplicationException.runtime( "Plugin Loopers are the domain of the PluginManager." );

		flags.add( flag );
	}

	void addState( State state )
	{
		states.add( state );
	}

	public long getAveragePolledMillis()
	{
		return averagePolledMillis;
	}

	public long getLastPolledMillis()
	{
		return lastPolledMillis;
	}

	public String getName()
	{
		return "Looper " + getThread().getName();
	}

	/**
	 * Get the TaskQueue associated with this Looper
	 */
	public LooperQueue getQueue()
	{
		return queue;
	}

	/**
	 * Gets the Thread running this Looper.
	 */
	public Thread getThread()
	{
		return thread;
	}

	public boolean hasFlag( Flag flag )
	{
		return flags.contains( flag );
	}

	/**
	 * Returns true if this Looper belongs to this thread.
	 */
	public boolean isCurrentThread()
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

	/**
	 * Returns true if this Looper has an average millis of over 100ms with each iteration.
	 */
	public boolean isOverloaded()
	{
		return isOverloaded;
	}

	/**
	 * Returns true if this Looper is currently working on quitting. No more tasks will be accepted by the Queue.
	 */
	public boolean isQuitting()
	{
		return states.contains( State.QUITTING );
	}

	/**
	 * Returns true if this Looper is currently running, i.e., a thread is actively calling the {@link #joinLoop()} method.
	 */
	public boolean isRunning()
	{
		return thread != null;
	}

	/**
	 * Joins the thread to this looper until it quits.
	 */
	public void joinLoop()
	{
		thread = Thread.currentThread();

		if ( !isCurrentThread() )
			throw ApplicationException.runtime( "LogisticsFactory#joinLoop() must be called from the thread that created it." );

		try
		{
			/* Synchronize the Looper so that outside access are only processed while the Looper is waiting between iterations. */
			synchronized ( lock )
			{
				/* Stores the last time the overload warning was displayed as to not flood the console. */
				long lastWarningMillis = 0L;
				/* Stores the last time the overload wait was called as to not delay the system all the more. */
				long lastOverloadMillis = 0;

				for ( ; ; )
				{
					/* Stores when the loop started. */
					long loopStartMillis = System.currentTimeMillis();

					/* Call the actual loop logic */
					LooperQueue.Result result = queue.next( loopStartMillis );

					if ( result.resultCode == LooperQueue.RESULT_OK )
					{
						result.entry.markFinalized();

						if ( result.entry.isAsync() )
							runAsync( result.entry.getRunnable() );
						else
							result.entry.getRunnable().run();

						result.entry.recycle();
					}

					/* Update the time taken during this iteration. */
					lastPolledMillis = System.currentTimeMillis() - loopStartMillis;

					/* Prevent negative numbers and warn */
					if ( lastPolledMillis < 0L )
					{
						Kernel.L.warning( "[" + getName() + "] Time ran backwards! Did the system time change?" );
						lastPolledMillis = 0L;
					}

					/* Update the average millis once we know the lastPolledMillis from this last iteration */
					averagePolledMillis = ( Math.min( lastPolledMillis, averagePolledMillis ) - Math.max( lastPolledMillis, averagePolledMillis ) ) / 2;

					/* Are we on average taking more than 100ms per iteration and has it been more than 5 seconds since last overload warning? */
					if ( averagePolledMillis > 100L )
					{
						if ( loopStartMillis - lastWarningMillis >= 15000L && ConfigRegistry.warnOnOverload() )
						{
							Kernel.L.warning( "[" + getName() + "] Can't keep up! Did the system time change, or is it overloaded?" );
							lastWarningMillis = loopStartMillis;
						}
						isOverloaded = true;
					}
					else
						isOverloaded = false;

					/* Delay was under the 50 millis cap, so we wait with timeout so the Looper can breath */
					if ( lastPolledMillis < 50L )
						wait( 50L - lastPolledMillis );

					/* Are we overloaded and it has been more than 1 second since the last time we forced a call on wait() */
					if ( isOverloaded && loopStartMillis - lastOverloadMillis > 1000L )
					{
						wait( 20L );
						lastOverloadMillis = loopStartMillis;
					}

					/* Process the quit message now that all pending messages have been handled. */
					if ( isQuitting() ) // TODO
						break;

					/* Otherwise the delay was longer, so we need to go immediately to the next iteration */
				}
			}
		}
		catch ( Throwable t )
		{
			Kernel.handleExceptions( t );
		}

		thread = null;
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
	public void quit()
	{
		queue.quit( false );
	}

	void quit( boolean removePendingMessages )
	{
		if ( type == LooperType.SYSTEM && !Kernel.isPrimaryThread() )
			throw new IllegalStateException( "SYSTEM queues are not allowed to quit." );
		if ( isQuitting )
			return;

		synchronized ( this )
		{
			isQuitting = true;

			final long now = Kernel.uptime();
			synchronized ( messages )
			{
				messages.removeIf( message -> {
					if ( removePendingMessages || message.getWhen() > now )
					{
						message.recycle();
						return true;
					}
					else
						return false;
				} );
			}

			// TODO Wake Queue
		}
	}

	void quitAndDestroy()
	{
		// TODO
		synchronized ( lock )
		{
			if ( thread != null )
				throw ApplicationException.ignorable( "Looper can't be destroyed while running." );

			addState( State.QUITTING );
			loopers.remove( this );
		}
	}

	/**
	 * Quits the looper safely.
	 * <p>
	 * Causes the {@link #joinLoop()} method to terminate as soon as all remaining messages
	 * in the queue that are already due to be delivered have been handled.
	 * However pending delayed messages with due times in the future will not be
	 * delivered before the loop terminates.
	 */
	public void quitSafely()
	{
		queue.quit( true );
	}

	void removeChildThread( Thread thread )
	{
		synchronized ( aliasThreads )
		{
			for ( WeakReference<Thread> threadReference : aliasThreads )
				if ( threadReference.get() == null || threadReference.get() == thread )
					aliasThreads.remove( threadReference );
		}
	}

	public void removeFlag( Flag flag )
	{
		if ( isRunning() )
			throw ApplicationException.ignorable( "You can't modify Looper flags while it's running." );
		if ( flag == Flag.SYSTEM )
			throw ApplicationException.runtime( "System Loopers are the domain of the application Kernel." );
		if ( flag == Flag.PLUGIN )
			throw ApplicationException.runtime( "Plugin Loopers are the domain of the PluginManager." );

		flags.remove( flag );
	}

	void removeState( State state )
	{
		states.remove( state );
	}

	/**
	 * Executes {@link Runnable} on a new thread, i.e., async.
	 * <p>
	 * We also add the new thread to the aliases, such that calls to
	 * the {@link Looper.Factory#obtain()} returns this {@link Looper}.
	 * <p>
	 * This also prevents a async task from creating a new Looper by accident.
	 */
	void runAsync( Runnable task )
	{
		Kernel.getExecutorParallel().execute( () -> {
			Thread thread = Thread.currentThread();
			addChildThread( thread );
			task.run();
			removeChildThread( thread );
		} );
	}

	@Override
	public String toString()
	{
		return "Looper (" + thread.getName() + ", tid " + thread.getId() + ") {" + Integer.toHexString( System.identityHashCode( this ) ) + "}";
	}

	void wake()
	{
		lock.notifyAll();
	}

	/**
	 * Specifies flags for each Looper
	 */
	public enum Flag
	{
		/**
		 * Forces the Looper the spawn each enqueued task on a new thread, regardless of if it's ASYNC or not.
		 */
		ASYNC,
		/**
		 * Indicates the {@link LooperQueue#next()} can and will block while the queue is empty.
		 * This flag is default on any non-system Looper as to save CPU time.
		 */
		BLOCKING,
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
	 * Indicates if certain Looper states are true
	 */
	enum State
	{
		/**
		 * Indicates the Looper is current trying to poll for new tasks
		 */
		POLLING,
		/**
		 * Indicates the Looper has gone to sleep until new tasks show
		 */
		STALLED,
		/**
		 * Indicates the Looper is waiting to quit.
		 */
		QUITTING
	}

	public static final class Factory
	{
		public static void destroy()
		{
			Looper looper = peek();
			if ( looper != null )
				looper.quitAndDestroy();
		}

		static Looper obtain( @Nonnull Supplier<Looper> supplier, @Nullable Predicate<Looper> predicate )
		{
			Objs.notNull( supplier );

			Looper looper = peek();
			if ( looper == null || ( predicate != null && predicate.test( looper ) ) )
			{
				looper = supplier.get();
				loopers.add( looper );
			}
			return looper;
		}

		public static Looper obtain()
		{
			return obtain( Looper::new, null );
		}

		static Stream<Looper> peek( Predicate<Looper> predicate )
		{
			return loopers.stream().filter( predicate );
		}

		static Looper peek()
		{
			return peek( Looper::isCurrentThread ).findFirst().orElse( null );
		}

		private Factory()
		{
			// Static Access
		}
	}
}
