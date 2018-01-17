package io.amelia.foundation;

import java.io.IOException;

import javax.annotation.Nonnull;

import io.amelia.foundation.events.Events;
import io.amelia.foundation.events.builtin.RunlevelEvent;
import io.amelia.foundation.facades.FacadePriority;
import io.amelia.foundation.facades.FacadeService;
import io.amelia.injection.Libraries;
import io.amelia.injection.MavenReference;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.StartupException;
import io.amelia.logcompat.LogBuilder;
import io.amelia.logcompat.Logger;
import io.amelia.support.EnumColor;
import io.amelia.support.IO;
import io.amelia.support.Objs;
import io.amelia.support.Runlevel;
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
	public static void setApplication( @Nonnull ApplicationInterface app ) throws ApplicationException.Error
	{
		Objs.notNull( app );

		if ( isRunlevel( Runlevel.DISPOSED ) )
			throw ApplicationException.error( "The application has been DISPOSED!" );
		if ( Foundation.app != null )
			throw ApplicationException.error( "The application instance has already been set!" );
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

	public static ApplicationLooper getLooper()
	{
		return app.getLooper();
	}

	public static ApplicationRouter getRouter()
	{
		return app.getRouter();
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
		return app.isPrimaryThread();
	}

	public static boolean isRunlevel( Runlevel runlevel )
	{
		return currentRunlevel == runlevel;
	}

	/**
	 * Handles post runlevel change. Should almost always be the very last method call when the runlevel changes.
	 */
	private static void onRunlevelChange() throws ApplicationException.Error
	{
		Events.callEventWithException( new RunlevelEvent( previousRunlevel, currentRunlevel ) );

		getApplication().onRunlevelChange( previousRunlevel, currentRunlevel );

		// Internal runlevel changes happen after this point. Generally progressing the application from each runlevel to the next.

		// TODO Register Foundation Resolver at STARTUP
		// if ( currentRunlevel == Runlevel.STARTUP )
		// Bindings.registerResolver( "io.amelia", new FoundationBindingResolver() );


		// Indicates the application has begun the main loop
		if ( currentRunlevel == Runlevel.MAINLOOP )
			setRunlevel( Runlevel.NETWORKING );

		// Indicates the application has started all and any networking
		if ( currentRunlevel == Runlevel.NETWORKING )
			setRunlevel( Runlevel.STARTED );

		// Indicates the application is now started
		if ( currentRunlevel == Runlevel.STARTED )
			L.info( EnumColor.join( EnumColor.GOLD, EnumColor.NEGATIVE ) + "Now Running! It took " + Timing.finish( runlevelTimingObject ) + "ms!" );


		if ( currentRunlevel == Runlevel.CRASHED || currentRunlevel == Runlevel.RELOAD || currentRunlevel == Runlevel.SHUTDOWN )
			L.notice( currentRunlevelReason );

		if ( currentRunlevel == Runlevel.CRASHED )
			app.quitUnsafe();

		// TODO Implement the RELOAD runlevel!
		if ( currentRunlevel == Runlevel.RELOAD )
			throw ApplicationException.error( "Not Implemented! Sorry!" );

		if ( currentRunlevel == Runlevel.SHUTDOWN )
			app.quitSafely();


		if ( currentRunlevel == Runlevel.DISPOSED )
		{
			// Runlevel DISPOSED is activated over the ApplicationLooper#joinLoop method returns.

			app.dispose();
			app = null;

			L.info( EnumColor.GOLD + "" + EnumColor.NEGATIVE + "Shutdown Completed! It took " + Timing.finish( runlevelTimingObject ) + "ms!" );

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

	/**
	 * Systematically changes the application runlevel.
	 * If this method is called by the application main thread, the change is made immediate.
	 */
	public static void setRunlevel( Runlevel level, String reason )
	{
		ApplicationLooper mainLooper = getLooper();
		// If we confirm that the current thread is the one used by the ApplicationLooper, we make the runlevel change immediate instead of posting it for later.
		if ( mainLooper.isHeldByCurrentThread() )
			setRunlevel0( level, reason );
		else
			mainLooper.postTask( () -> setRunlevel0( level, reason ) );
	}

	private synchronized static void setRunlevel0( Runlevel level, String reason )
	{
		try
		{
			if ( !getLooper().isHeldByCurrentThread() )
				throw ApplicationException.error( "Runlevel can only be set from the application looper thread. Be more careful next time." );
			if ( currentRunlevel == level )
				throw ApplicationException.error( "Runlevel is already set to " + level.name() + ". This might be a severe race bug." );
			if ( !level.checkRunlevelOrder( currentRunlevel ) )
				throw ApplicationException.error( "RunLevel " + level.name() + " was set out of order. The is likely a severe race bug." );

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
		catch ( ApplicationException.Error e )
		{
			Kernel.handleExceptions( e );
		}
	}

	public static void shutdown() throws ApplicationException.Error
	{
		setRunlevel( Runlevel.SHUTDOWN );
	}

	/**
	 * Will process the application load based on the information provided by the ApplicationInterface.
	 * Takes Runlevel from INITIALIZATION to RUNNING.
	 * <p>
	 * start() will not return until the main looper quits.
	 */
	public static void start() throws ApplicationException.Error
	{
		requirePrimaryThread();
		requireRunlevel( Runlevel.INITIALIZATION, "Start() must be called at runlevel INITIALIZATION" );

		// Call to make sure the INITIALIZATION Runlevel is acknowledged by the application.
		onRunlevelChange();

		L.info( "Starting " + Kernel.getDevMeta().getProductName() + " version " + Kernel.getDevMeta().getVersionDescribe() );

		// Initiate startup procedures.
		setRunlevel( Runlevel.STARTUP );

		// This ensures the next set of runlevels are handled in sequence after the main looper is started.
		ApplicationLooper mainLooper = getApplication().getLooper();

		// As soon as the looper gets started by the following line, we set the first runlevel appropriately.
		mainLooper.postTask( () -> setRunlevel0( Runlevel.MAINLOOP, null ) );

		// Join this thread to the main looper.
		mainLooper.joinLoop();

		// Sets the application to the disposed state once the joinLoop method returns exception free.
		setRunlevel( Runlevel.DISPOSED );
	}

	private Foundation()
	{
		// Static Access
	}

	public static class ConfigKeys
	{
		/**
		 * Specifies built-in facades which can be registered here or by calling {@link io.amelia.foundation.binding.BoundNamespace#registerFacade(String, FacadeService, FacadePriority)}; {@see Bindings#bindNamespace(String)}}.
		 * Benefits of using configuration for facade registration is it adds the ability for end-users to disable select facades, however, this should be used if the facade is used by scripts.
		 *
		 * <pre>
		 * bindings:
		 *   facades:
		 *     permissions:
		 *       class: io.amelia.foundation.facades.PermissionsService
		 *       priority: NORMAL
		 *     events:
		 *       class: io.amelia.foundation.facades.EventService
		 *       priority: NORMAL
		 * </pre>
		 */
		public static final String BINDINGS_FACADES = "bindings.facades";

		private ConfigKeys()
		{
			// Static Access
		}
	}
}
