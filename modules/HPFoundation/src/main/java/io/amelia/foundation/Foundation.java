package io.amelia.foundation;

import java.io.IOException;

import javax.annotation.Nonnull;

import io.amelia.events.EventDispatcher;
import io.amelia.events.application.RunlevelEvent;
import io.amelia.injection.Libraries;
import io.amelia.injection.MavenReference;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.Runlevel;
import io.amelia.lang.StartupException;
import io.amelia.logcompat.LogBuilder;
import io.amelia.logcompat.Logger;
import io.amelia.support.EnumColor;
import io.amelia.support.IO;
import io.amelia.support.Objs;
import io.amelia.support.Timing;

/**
 * Used for accessing majority of the HP Foundation API.<br />
 * The first call to of this class MUST be the thread that will initiate the main loop.
 * <p>
 * <p>
 * Your main() should look like this:
 * <code>
 * App.prepare();
 * ...
 * ImplementedApplication app = new ImplementedApplication();
 * App.setApplication( app );
 * ...
 * App.start();
 * </code>
 */
public final class Foundation
{
	public static final Logger L = LogBuilder.get();

	/**
	 * Set to the thread that first accessed this class.
	 * Thread MUST also be the thread used to join the Main Looper
	 */
	public static final Thread PRIMARY_THREAD = Thread.currentThread();

	private static ApplicationInterface app = null;
	private static Runlevel currentRunlevel = Runlevel.INITIALIZATION;
	private static String currentRunlevelReason = null;
	private static Runlevel previousRunlevel;
	private static Object runlevelTimingObject = new Object();

	public static <T extends ApplicationInterface> T getApplication()
	{
		if ( isRunlevel( Runlevel.DISPOSED ) )
			throw ApplicationException.runtime( "The application has been DISPOSED!" );
		if ( app == null )
			throw ApplicationException.runtime( "The application instance has never been set!" );
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
		if ( Foundation.app != null )
			throw ApplicationException.fatal( "The application instance has already been set!" );
		Kernel.setExceptionContext( app );
		Foundation.app = app;
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

	public static void setRunlevel( Runlevel level )
	{
		setRunlevel( level, null );
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
	 * Handles post runlevel change events. Should almost always be the very last method call when the runlevel changes.
	 */
	private static void onRunlevelChange() throws ApplicationException
	{
		EventDispatcher.callEventWithException( new RunlevelEvent( previousRunlevel, currentRunlevel ) );

		getApplication().onRunlevelChange( previousRunlevel, currentRunlevel );

		/* Indicates the application has begun the main loop */
		if ( currentRunlevel == Runlevel.MAINLOOP )
			setRunlevel( Runlevel.NETWORKING );

		/* Indicates the application has started all and any networking */
		if ( currentRunlevel == Runlevel.NETWORKING )
			setRunlevel( Runlevel.STARTED );

		/* Indicates the application is now started */
		if ( currentRunlevel == Runlevel.STARTED )
			Foundation.L.info( EnumColor.GOLD + "" + EnumColor.NEGATIVE + "Now Running! It took " + Timing.finish( runlevelTimingObject ) + "ms!" );

		if ( currentRunlevel == Runlevel.RELOAD || currentRunlevel == Runlevel.CRASHED || currentRunlevel == Runlevel.SHUTDOWN )
		{
			L.notice( currentRunlevelReason );

			app.shutdown();
		}

		if ( currentRunlevel == Runlevel.SHUTDOWN )
			Foundation.setRunlevel( Runlevel.DISPOSED );

		if ( currentRunlevel == Runlevel.DISPOSED )
		{
			app.dispose();
			app = null;

			Foundation.L.info( EnumColor.GOLD + "" + EnumColor.NEGATIVE + "Shutdown Completed! It took " + Timing.finish( runlevelTimingObject ) + "ms!" );

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

			Timing.start( runlevelTimingObject );

			onRunlevelChange();

			if ( level != Runlevel.DISPOSED && level != Runlevel.STARTED )
				L.info( "Application Runlevel has been changed to " + level.name() + "! It took " + Timing.finish( runlevelTimingObject ) + "ms!" );
		}
		catch ( ApplicationException e )
		{
			Kernel.handleExceptions( e );
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

		L.info( "Starting " + Kernel.getDevMeta().getProductName() + " version " + Kernel.getDevMeta().getVersionDescribe() );

		// Initiate startup procedures.
		setRunlevel( Runlevel.STARTUP );

		// This ensures the next set of runlevels are handled in sequence after the main looper is started.
		Looper mainLooper = getApplication().getMainLooper();

		// As soon as the main Looper gets a kick in it's pants, the first runlevel is initiated.
		mainLooper.getQueue().postTask( () -> setRunlevel0( Runlevel.MAINLOOP, null ) );

		// Join this thread to the main looper.
		mainLooper.joinLoop();
	}

	private Foundation()
	{
		// Static Access
	}
}
