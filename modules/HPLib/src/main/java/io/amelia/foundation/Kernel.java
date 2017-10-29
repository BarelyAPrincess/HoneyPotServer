package io.amelia.foundation;

import com.sun.istack.internal.NotNull;
import io.amelia.events.EventDispatcher;
import io.amelia.events.application.RunlevelEvent;
import io.amelia.foundation.injection.Libraries;
import io.amelia.foundation.injection.MavenReference;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.EnumColor;
import io.amelia.lang.ExceptionReport;
import io.amelia.lang.IException;
import io.amelia.lang.ReportingLevel;
import io.amelia.lang.Runlevel;
import io.amelia.lang.StartupException;
import io.amelia.logcompat.LogBuilder;
import io.amelia.logcompat.Logger;
import io.amelia.support.Info;
import io.amelia.support.LibIO;
import io.amelia.support.LibTime;
import io.amelia.support.Lists;
import io.amelia.support.Objs;
import io.amelia.support.Timings;
import io.amelia.synchronize.LooperFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class Kernel
{
	public static final Logger L = LogBuilder.get();
	public static long startTime = System.currentTimeMillis();
	private static ApplicationInterface app = null;
	private static Class<? extends ApplicationInterface> applicationInterface = null;
	private static Runlevel currentRunlevel = Runlevel.INITIALIZATION;
	private static String currentRunlevelReason = null;
	private static Object currentRunlevelTimingObject = new Object();
	private static DefaultMainLoop mainLoop;
	private static Runlevel previousRunlevel;
	private static Thread primaryThread;
	private static ExecutorService threadPool = Executors.newCachedThreadPool();

	@SuppressWarnings( "unchecked" )
	public static <T extends ApplicationInterface> T getApplication()
	{
		if ( isRunlevel( Runlevel.DISPOSED ) )
			throw ApplicationException.ignorable( "The application has been DISPOSED!" );
		if ( applicationInterface == null )
			throw new StartupException( "The Application Interface was never set." );
		if ( app == null )
			app = Objs.initClass( applicationInterface );

		try
		{
			return ( T ) app;
		}
		catch ( Exception ex )
		{
			throw ApplicationException.ignorable( "Can't initialize the new instance of " + applicationInterface.getName(), ex );
		}
	}

	public static String getCurrentRunlevelReason()
	{
		return currentRunlevelReason;
	}

	public static Runlevel getLastRunlevel()
	{
		return previousRunlevel;
	}

	public static Runlevel getRunlevel()
	{
		return currentRunlevel;
	}

	public static void setRunlevel( Runlevel level ) throws ApplicationException
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

		errorStream.get().forEach( e ->
		{
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

			debugStream.get().forEach( e ->
			{
				if ( e instanceof Throwable )
					LogBuilder.get().warning( ( Throwable ) e );
				else
					LogBuilder.get().warning( e.getClass() + ": " + e.getMessage() );
			} );
		}

		if ( hasErrored && crashOnError )
			try
			{
				// Pass crash information for examination
				setRunlevel( Runlevel.CRASHED, "The Application has reached an errored state!" );
			}
			catch ( ApplicationException e )
			{
				// Ignore
			}
	}

	public static boolean isPrimaryThread()
	{
		if ( primaryThread == null )
			throw new IllegalStateException( "prepare() was never called!" );
		return primaryThread == Thread.currentThread();
	}

	private static boolean isRunlevel( Runlevel runlevel )
	{
		return currentRunlevel == runlevel;
	}

	private static void onRunlevelChange() throws ApplicationException
	{
		EventDispatcher.callEventWithException( new RunlevelEvent( previousRunlevel, currentRunlevel ) );

		if ( currentRunlevel == Runlevel.DAEMON )
		{
			mainLoop = new DefaultMainLoop( getApplication() );
			mainLoop.executeOnExecutor( threadPool );

			Kernel.L.info( EnumColor.GOLD + "" + EnumColor.NEGATIVE + "Now Running! It took " + Timings.finish( currentRunlevelTimingObject ) + "ms!" );
		}

		if ( currentRunlevel == Runlevel.RELOAD || currentRunlevel == Runlevel.CRASHED || currentRunlevel == Runlevel.SHUTDOWN )
		{
			L.notice( currentRunlevelReason );

			mainLoop.cancel( false );
			threadPool.shutdown();
		}

		if ( currentRunlevel == Runlevel.DISPOSED )
		{
			threadPool = null;
			mainLoop = null;
			app = null;

			Kernel.L.info( EnumColor.GOLD + "" + EnumColor.NEGATIVE + "Shutdown Completed! It took " + Timings.finish( currentRunlevelTimingObject ) + "ms!" );

			System.exit( 0 );
		}

		getApplication().onRunlevelChange( previousRunlevel, currentRunlevel );

		if ( currentRunlevel == Runlevel.SHUTDOWN )
			Kernel.setRunlevel( Runlevel.DISPOSED );
	}

	/**
	 * Loads the built-in dependencies written by the gradle script
	 */
	public static void prepare()
	{
		primaryThread = Thread.currentThread();
		LooperFactory.prepare();

		LogBuilder.get().info( "Loading deployment libraries from " + Libraries.LIBRARY_DIR );
		try
		{
			/* Load Deployment Libraries */
			LogBuilder.get().info( "Loading deployment libraries defined in dependencies.txt." );
			for ( String depend : LibIO.resourceToString( "dependencies.txt" ).split( "\n" ) )
				Libraries.loadLibrary( new MavenReference( "builtin", depend ) );
		}
		catch ( IOException e )
		{
			throw new StartupException( "Failed to read the built-in dependencies file.", e );
		}
		LogBuilder.get().info( "Finished downloading deployment libraries." );
	}

	public static void registerRunnable( Runnable runnable )
	{
		threadPool.execute( runnable );
	}

	/**
	 * Sets the ApplicationInterface used
	 *
	 * @param applicationInterface The ApplicationInterface Implementation
	 */
	public static void setApplicationInterface( Class<? extends ApplicationInterface> applicationInterface )
	{
		Kernel.applicationInterface = applicationInterface;
	}

	public static void setRunlevel( Runlevel level, String reason ) throws ApplicationException
	{
		if ( !isPrimaryThread() )
			throw new ApplicationException.Error( ReportingLevel.E_ERROR, "Runlevel can only be set from the Primary Thread." );
		if ( currentRunlevel == level )
			throw new ApplicationException.Error( ReportingLevel.E_ERROR, "Runlevel is already set to " + level.name() + "." );
		if ( !level.checkRunlevelOrder( currentRunlevel ) )
			throw new ApplicationException.Error( ReportingLevel.E_ERROR, "RunLevel " + level.name() + " was called out of order." );

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

		Timings.start( currentRunlevelTimingObject );

		onRunlevelChange();

		if ( level != Runlevel.DISPOSED && level != Runlevel.DAEMON )
			L.info( "Application Runlevel has been changed to " + level.name() + "! It took " + Timings.finish( currentRunlevelTimingObject ) + "ms!" );
	}

	public static void shutdown() throws ApplicationException
	{
		setRunlevel( Runlevel.SHUTDOWN );
	}

	/**
	 * Will process the application load based on the information provided by the ApplicationInterface.
	 * Takes Runlevel from INITIALIZATION to RUNNING.
	 */
	public static void start() throws ApplicationException
	{
		if ( !isPrimaryThread() )
			throw new StartupException( "Start() must be called from the primary thread." );
		if ( !isRunlevel( Runlevel.INITIALIZATION ) )
			throw new StartupException( "Start() must be called at INITIALIZATION" );

		// Call to make sure the INITIALIZATION Runlevel is well received
		onRunlevelChange();

		L.info( "Starting " + Info.getProduct() + " (" + Info.getVersion() + ")" );

		/*
		 * INITIALIZED
		 * Indicates the application has initialized all modules
		 */
		setRunlevel( Runlevel.STARTUP );

		/*
		 * STARTUP
		 * Indicates the application has begun startup procedures
		 */
		setRunlevel( Runlevel.MAINLOOP );

		/*
		 * POSTSTARTUP
		 * Indicates the application has started all and any networking
		 */
		setRunlevel( Runlevel.NETWORKING );

		/*
		 * RUNNING
		 * Indicates the application is now ready to handle the main application loop
		 */
		setRunlevel( Runlevel.DAEMON );

		/* From here, the application can SHUTDOWN, RELOAD, or CRASH */
	}

	public static long uptime()
	{
		return System.currentTimeMillis() - Kernel.startTime;
	}

	public static String uptimeDescribe()
	{
		return LibTime.formatDurection( System.currentTimeMillis() - Kernel.startTime );
	}

	private Kernel()
	{

	}
}
