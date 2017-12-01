package io.amelia.foundation;

import com.sun.istack.internal.NotNull;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.amelia.events.EventDispatcher;
import io.amelia.events.application.RunlevelEvent;
import io.amelia.foundation.injection.Libraries;
import io.amelia.foundation.injection.MavenReference;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.EnumColor;
import io.amelia.lang.ExceptionReport;
import io.amelia.lang.IException;
import io.amelia.lang.Runlevel;
import io.amelia.lang.StartupException;
import io.amelia.lang.UncaughtException;
import io.amelia.logcompat.LogBuilder;
import io.amelia.logcompat.Logger;
import io.amelia.support.IO;
import io.amelia.support.Info;
import io.amelia.support.Lists;
import io.amelia.support.Objs;
import io.amelia.support.Strs;
import io.amelia.support.Timing;

/**
 * The application Kernel used for accessing majority of the API.
 * The first call to of this class MUST be the thread intended to run the main loop.
 * <p>
 * <p>
 * Your main() should look like this:
 * <code>
 * Kernel.prepare();
 * <p>
 * ImplementedApplication app = new ImplementedApplication();
 * Kernel.setApplication( app );
 * ...
 * </code>
 */
public final class Kernel
{
	public static final Logger L = LogBuilder.get();
	/**
	 * Set to the thread that first accessed the Kernel.
	 */
	public static final Thread PRIMARY_THREAD = Thread.currentThread();
	/**
	 * An {@link Executor} that can be used to execute tasks in parallel.
	 */
	static final Executor EXECUTOR_PARALLEL;
	/**
	 * An {@link Executor} that executes tasks one at a time in serial
	 * order.  This serialization is global to a particular process.
	 */
	static final Executor EXECUTOR_SERIAL;
	static final int KEEP_ALIVE_SECONDS = 30;
	static final ThreadFactory threadFactory = new ThreadFactory()
	{
		private final AtomicInteger mCount = new AtomicInteger( 1 );

		@Override
		public Thread newThread( Runnable r )
		{
			Thread newThread = new Thread( r, "HPS Thread #" + String.format( "%d04", mCount.getAndIncrement() ) );
			newThread.setUncaughtExceptionHandler( ( thread, exp ) -> Kernel.handleExceptions( new UncaughtException( "Uncaught exception thrown on thread " + thread.getName(), exp ) ) );

			return newThread;
		}
	};
	private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
	static final int THREAD_ROOL_SIZE_MAXIMUM = CPU_COUNT * 2 + 1;
	// We want at least 2 threads and at most 4 threads in the core pool,
	// preferring to have 1 less than the CPU count to avoid saturating
	// the CPU with background work
	static final int THREAD_POOL_SIZE_CORE = Math.max( 4, Math.min( CPU_COUNT - 1, 1 ) );
	public static long startTime = System.currentTimeMillis();
	private static ApplicationInterface app = null;
	private static Class<? extends ApplicationInterface> applicationInterface = null;
	private static Runlevel currentRunlevel = Runlevel.INITIALIZATION;
	private static String currentRunlevelReason = null;
	private static Object currentRunlevelTimingObject = new Object();
	private static Runlevel previousRunlevel;

	static
	{
		ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor( Kernel.THREAD_POOL_SIZE_CORE, Kernel.THREAD_ROOL_SIZE_MAXIMUM, Kernel.KEEP_ALIVE_SECONDS, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), threadFactory );
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

	public static <T extends ApplicationInterface> T getApplication()
	{
		if ( app == null )
			throw ApplicationException.runtime( "The application has been DISPOSED!" );
		return ( T ) app;
	}

	/**
	 * Sets an instance of ApplicationInterface for use by the Kernel
	 *
	 * @param app The ApplicationInterface instance
	 */
	public static void setApplication( @Nonnull ApplicationInterface app ) throws ApplicationException
	{
		Objs.notNull( app );

		if ( isRunlevel( Runlevel.DISPOSED ) )
			throw ApplicationException.fatal( "The application has been DISPOSED!" );
		if ( Kernel.app != null )
			throw ApplicationException.fatal( "The application instance has already been set!" );
		Kernel.app = app;
	}

	public static String getCurrentRunlevelReason()
	{
		return currentRunlevelReason;
	}

	public static Executor getExecutorParallel()
	{
		return EXECUTOR_PARALLEL;
	}

	public static Executor getExecutorSerial()
	{
		return EXECUTOR_SERIAL;
	}

	public static Runlevel getLastRunlevel()
	{
		return previousRunlevel;
	}

	public static Runlevel getRunlevel()
	{
		return currentRunlevel;
	}

	public static void setRunlevel( Runlevel level )
	{
		setRunlevel( level, null );
	}

	public static void handleExceptions( @NotNull Throwable throwable )
	{
		handleExceptions( Lists.newArrayList( throwable ) );
	}

	public static void handleExceptions( @NotNull List<? extends Throwable> throwables )
	{
		handleExceptions( throwables, true );
	}

	public static void handleExceptions( @NotNull Throwable throwable, boolean crashOnError )
	{
		handleExceptions( Lists.newArrayList( throwable ), crashOnError );
	}

	public static void handleExceptions( @NotNull List<? extends Throwable> throwables, boolean crashOnError )
	{
		ExceptionReport report = new ExceptionReport();
		boolean hasErrored = false;

		for ( Throwable t : throwables )
		{
			t.printStackTrace();
			if ( report.handleException( t, getApplication() ) )
				hasErrored = true;
		}

		/* Non-Ignorable Exceptions */

		Supplier<Stream<IException>> errorStream = report::getNotIgnorableExceptions;

		LogBuilder.get().severe( "We Encountered " + errorStream.get().count() + " Non-Ignorable Exception(s):" );

		errorStream.get().forEach( e -> {
			if ( e instanceof Throwable )
				LogBuilder.get().severe( ( Throwable ) e );
			else
				LogBuilder.get().severe( e.getClass() + ": " + e.getMessage() );
		} );

		/* Ignorable Exceptions */

		Supplier<Stream<IException>> debugStream = report::getIgnorableExceptions;

		if ( debugStream.get().count() > 0 )
		{
			LogBuilder.get().severe( "We Encountered " + debugStream.get().count() + " Ignorable Exception(s):" );

			debugStream.get().forEach( e -> {
				if ( e instanceof Throwable )
					LogBuilder.get().warning( ( Throwable ) e );
				else
					LogBuilder.get().warning( e.getClass() + ": " + e.getMessage() );
			} );
		}

		// Pass crash information for examination
		if ( hasErrored && crashOnError )
			setRunlevel( Runlevel.CRASHED, "The Application has reached an errored state!" );
	}

	public static boolean isPrimaryThread()
	{
		return PRIMARY_THREAD == Thread.currentThread();
	}

	private static boolean isRunlevel( Runlevel runlevel )
	{
		return currentRunlevel == runlevel;
	}

	/**
	 * Handles post runlevel change events. Should almost always be the very last method called when the runlevel changes.
	 */
	private static void onRunlevelChange() throws ApplicationException
	{
		EventDispatcher.callEventWithException( new RunlevelEvent( previousRunlevel, currentRunlevel ) );

		getApplication().onRunlevelChange( previousRunlevel, currentRunlevel );

		/* Indicates the application has begun the main loop */
		if ( currentRunlevel == Runlevel.MAINLOOP )
		{
			setRunlevel( Runlevel.NETWORKING );
		}

		/* Indicates the application has started all and any networking */
		if ( currentRunlevel == Runlevel.NETWORKING )
		{
			setRunlevel( Runlevel.STARTED );
		}

		/* Indicates the application is now started */
		if ( currentRunlevel == Runlevel.STARTED )
		{
			Kernel.L.info( EnumColor.GOLD + "" + EnumColor.NEGATIVE + "Now Running! It took " + Timing.finish( currentRunlevelTimingObject ) + "ms!" );
		}

		if ( currentRunlevel == Runlevel.RELOAD || currentRunlevel == Runlevel.CRASHED || currentRunlevel == Runlevel.SHUTDOWN )
		{
			L.notice( currentRunlevelReason );

			app.shutdown();
		}

		if ( currentRunlevel == Runlevel.SHUTDOWN )
			Kernel.setRunlevel( Runlevel.DISPOSED );

		if ( currentRunlevel == Runlevel.DISPOSED )
		{
			app.dispose();
			app = null;

			Kernel.L.info( EnumColor.GOLD + "" + EnumColor.NEGATIVE + "Shutdown Completed! It took " + Timing.finish( currentRunlevelTimingObject ) + "ms!" );

			System.exit( 0 );
		}
	}

	/**
	 * Loads the built-in dependencies written by the gradle script.
	 * To best avoid {@link ClassNotFoundException}, this should be the very first call made by the main(String... args) method.
	 */
	public static void prepare()
	{
		L.info( "Loading deployment libraries from " + Libraries.LIBRARY_DIR );
		try
		{
			/* Load Deployment Libraries */
			L.info( "Loading deployment libraries defined in dependencies.txt." );
			for ( String depend : IO.resourceToString( "dependencies.txt" ).split( "\n" ) )
				Libraries.loadLibrary( new MavenReference( "builtin", depend ) );
		}
		catch ( IOException e )
		{
			throw new StartupException( "Failed to read the built-in dependencies file.", e );
		}
		L.info( "Finished downloading deployment libraries." );
	}

	public static void requirePrimaryThread()
	{
		requirePrimaryThread( null );
	}

	public static void requirePrimaryThread( String errorMessage )
	{
		if ( !isPrimaryThread() )
			throw new StartupException( errorMessage == null ? "Method MUST be called from the primary thread that initialed started the Kernel." : errorMessage );
	}

	public static void requireRunlevel( Runlevel runlevel )
	{
		requireRunlevel( runlevel, null );
	}

	public static void requireRunlevel( Runlevel runlevel, String errorMessage )
	{
		if ( !isRunlevel( runlevel ) )
			throw new StartupException( errorMessage == null ? "Method MUST be called at runlevel " + runlevel.name() : errorMessage );
	}

	public static void setRunlevel( Runlevel level, String reason )
	{
		Looper mainLooper = getApplication().getMainLooper();
		if ( mainLooper.isCurrentThread() )
			setRunlevel0( level, reason );
		else
			mainLooper.getQueue().postTask( () -> setRunlevel0( level, reason ) );
	}

	private synchronized static void setRunlevel0( Runlevel level, String reason )
	{
		try
		{
			requirePrimaryThread( "Runlevel can only be set from the Primary Thread." );
			if ( currentRunlevel == level )
				throw ApplicationException.fatal( "Runlevel is already set to " + level.name() + "." );
			if ( !level.checkRunlevelOrder( currentRunlevel ) )
				throw ApplicationException.fatal( "RunLevel " + level.name() + " was called out of order." );

			if ( Objs.isEmpty( reason ) )
			{
				String instanceId = getApplication().getEnv().getString( "instance-id" );

				if ( level == Runlevel.RELOAD )
					reason = String.format( "Server %s is restarting. Be back soon. :D", instanceId );
				else if ( level == Runlevel.CRASHED )
					reason = String.format( "Server %s has crashed. Sorry about that. :(", instanceId );
				else if ( level == Runlevel.SHUTDOWN )
					reason = String.format( "Server %s is shutting down. Good bye. :|", instanceId );
				else
					reason = "No reason was provided.";
			}

			currentRunlevelReason = reason;
			previousRunlevel = currentRunlevel = level;

			Timing.start( currentRunlevelTimingObject );

			onRunlevelChange();

			if ( level != Runlevel.DISPOSED && level != Runlevel.STARTED )
				L.info( "Application Runlevel has been changed to " + level.name() + "! It took " + Timing.finish( currentRunlevelTimingObject ) + "ms!" );
		}
		catch ( ApplicationException e )
		{
			handleExceptions( e );
		}
	}

	public static void shutdown() throws ApplicationException
	{
		setRunlevel( Runlevel.SHUTDOWN );
	}

	/**
	 * Will process the application load based on the information provided by the ApplicationInterface.
	 * Takes Runlevel from INITIALIZATION to RUNNING.
	 * <p>
	 * start() will not return until the main looper quits.
	 */
	public static void start() throws ApplicationException
	{
		requirePrimaryThread();
		requireRunlevel( Runlevel.INITIALIZATION, "Start() must be called at runlevel INITIALIZATION" );

		// Call to make sure the INITIALIZATION Runlevel is acknowledged by the application.
		onRunlevelChange();

		L.info( "Starting " + Info.getProduct() + " (" + Info.getVersion() + ")" );

		/*
		 * STARTUP
		 * Indicates the application has begun startup procedures
		 */
		setRunlevel( Runlevel.STARTUP );

		/* This ensures the next set of runlevels are handled in sequence after the main looper is started */

		Looper mainLoop = getApplication().getMainLooper();

		/* As soon as the main Looper gets a kick in it's pants, the first runlevel is initiated. */
		mainLoop.getQueue().postTask( () -> setRunlevel0( Runlevel.MAINLOOP, null ) );

		/* Join this thread to the main looper. */
		mainLoop.joinLoop();
	}

	public static long uptime()
	{
		return System.currentTimeMillis() - Kernel.startTime;
	}

	public static String uptimeDescribe()
	{
		return Strs.formatDuration( System.currentTimeMillis() - Kernel.startTime );
	}

	private Kernel()
	{
		// Static Access
	}
}
