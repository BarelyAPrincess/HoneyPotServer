/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation;

import java.io.IOException;
import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.data.TypeBase;
import io.amelia.events.Events;
import io.amelia.foundation.bindings.FacadeRegistration;
import io.amelia.foundation.events.RunlevelEvent;
import io.amelia.injection.Libraries;
import io.amelia.injection.MavenReference;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.ExceptionReport;
import io.amelia.lang.StartupException;
import io.amelia.logcompat.LogBuilder;
import io.amelia.logcompat.Logger;
import io.amelia.looper.LooperRouter;
import io.amelia.looper.MainLooper;
import io.amelia.support.EnumColor;
import io.amelia.support.IO;
import io.amelia.support.Objs;
import io.amelia.support.Runlevel;
import io.amelia.support.Strs;
import io.amelia.support.Timing;

/**
 * Used for accessing majority of the HP Foundation API.<br />
 * The first call to this class MUST be the thread that will initiate the main loop.
 * <p>
 * <p>
 * Your main() should look like this:
 * <code>
 * Foundation.prepare();
 * ...
 * ImplementedApplication app = new ImplementedApplication();
 * Foundation.setApplication( app );
 * ...
 * Foundation.start();
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

	static
	{
		Kernel.setLogHandler( new ImplLogHandler()
		{
			@Override
			public void log( Level level, Class<?> source, String message, Object... args )
			{
				L.log( level, message, args );
			}

			@Override
			public void log( Level level, Class<?> source, Throwable cause )
			{
				Strs.split( Strs.getStackTrace( cause ), "\n" ).forEach( str -> L.log( level, str ) );
			}
		} );

		Kernel.setImplUtils( new ImplUtils()
		{
			@Override
			public boolean isPrimaryThread()
			{
				return Foundation.isPrimaryThread();
			}
		} );
	}

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
		if ( isRunlevel( Runlevel.DISPOSED ) )
			throw ApplicationException.error( "The application has been DISPOSED!" );
		if ( Foundation.app != null )
			throw ApplicationException.error( "The application instance has already been set!" );

		LooperRouter.setMainLooper( new FoundationLooper( app ) );
		Kernel.setExceptionRegistrar( app );

		Foundation.app = app;

		if ( !app.hasArgument( "no-banner" ) )
			app.showBanner( L );

		L.info( "Application Instance Identity: " + app.getId() );
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

	public static void setRunlevel( @Nonnull Runlevel level )
	{
		setRunlevel( level, null );
	}

	public static boolean isPrimaryThread()
	{
		// If app has yet to be set, then it's anyone's guess which thread is primary and were not willing to take that risk. :(
		return app == null || app.isPrimaryThread();
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

		app.onRunlevelChange( previousRunlevel, currentRunlevel );

		// Internal runlevel changes happen after this point. Generally progressing the application from each runlevel to the next.

		// TODO Register Foundation Resolver at STARTUP
		// if ( currentRunlevel == Runlevel.STARTUP )
		// Bindings.registerResolver( "io.amelia", new FoundationBindingResolver() );

		// Indicates the application has begun the main loop
		if ( currentRunlevel == Runlevel.MAINLOOP )
			if ( app instanceof NetworkedApplication )
				setRunlevelLater( Runlevel.NETWORKING );
			else
				setRunlevelLater( Runlevel.STARTED );

		// Indicates the application has started all and any networking
		if ( currentRunlevel == Runlevel.NETWORKING )
			setRunlevelLater( Runlevel.STARTED );

		// if ( currentRunlevel == Runlevel.CRASHED || currentRunlevel == Runlevel.RELOAD || currentRunlevel == Runlevel.SHUTDOWN )
		// L.notice( currentRunlevelReason );

		// TODO Implement the RELOAD runlevel!
		if ( currentRunlevel == Runlevel.RELOAD )
			throw ApplicationException.error( "Not Implemented. Sorry!" );

		if ( currentRunlevel == Runlevel.SHUTDOWN )
			app.quitSafely();

		if ( currentRunlevel == Runlevel.CRASHED )
			app.quitUnsafe();

		if ( currentRunlevel == Runlevel.DISPOSED )
		{
			// Runlevel DISPOSED is activated over the ApplicationLooper#joinLoop method returns.

			app.dispose();
			app = null;

			try
			{
				Thread.sleep( 100 );
			}
			catch ( InterruptedException e )
			{
				// Ignore
			}

			System.exit( 0 );
		}
	}

	/**
	 * Loads the built-in dependencies written by the gradle script.
	 * To best avoid {@link ClassNotFoundException}, this should be the very first call made by the main(String... args) method.
	 */
	public static void prepare() throws ApplicationException.Error
	{
		requireApplication();
		requirePrimaryThread();
		requireRunlevel( Runlevel.INITIALIZATION, "prepare() must be called at runlevel INITIALIZATION" );

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

		// Call to make sure the INITIALIZATION Runlevel is acknowledged by the application.
		onRunlevelChange();
	}

	private static void requireApplication() throws ApplicationException.Error
	{
		if ( app == null )
			throw new ApplicationException.Error( "Application is expected to have been initialized by this point." );
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
	public static void setRunlevel( @Nonnull Runlevel level, @Nullable String reason )
	{
		Objs.notNull( level );
		MainLooper mainLooper = LooperRouter.getMainLooper();
		// If we confirm that the current thread is the one used by the ApplicationLooper, we make the runlevel change immediate instead of posting it for later.
		if ( !mainLooper.isThreadJoined() && app.isPrimaryThread() || mainLooper.isHeldByCurrentThread() )
			setRunlevel0( level, reason );
		else
			setRunlevelLater( level, reason );
	}

	private synchronized static void setRunlevel0( Runlevel runlevel, String reason )
	{
		try
		{
			if ( LooperRouter.getMainLooper().isThreadJoined() && !LooperRouter.getMainLooper().isHeldByCurrentThread() )
				throw ApplicationException.error( "Runlevel can only be set from the application looper thread. Be more careful next time." );
			if ( currentRunlevel == runlevel )
				throw ApplicationException.error( "Runlevel is already set to " + runlevel.name() + ". This might be a severe race bug." );
			if ( !runlevel.checkRunlevelOrder( currentRunlevel ) )
				throw ApplicationException.error( "RunLevel " + runlevel.name() + " was set out of order. The is likely a severe race bug." );

			if ( Objs.isEmpty( reason ) )
			{
				String instanceId = getApplication().getEnv().getString( "instance-id" ).orElse( null );

				if ( runlevel == Runlevel.RELOAD )
					reason = String.format( "Server %s is restarting. Be back soon. :D", instanceId );
				else if ( runlevel == Runlevel.CRASHED )
					reason = String.format( "Server %s has crashed. Sorry about that. :(", instanceId );
				else if ( runlevel == Runlevel.SHUTDOWN )
					reason = String.format( "Server %s is shutting down. Good bye. :|", instanceId );
				else
					reason = "No reason was provided.";
			}

			Timing.start( runlevelTimingObject );

			previousRunlevel = currentRunlevel;
			currentRunlevel = runlevel;
			currentRunlevelReason = reason;

			if ( runlevel == Runlevel.RELOAD || runlevel == Runlevel.SHUTDOWN || runlevel == Runlevel.CRASHED )
				L.info( EnumColor.join( EnumColor.GOLD, EnumColor.NEGATIVE ) + "" + EnumColor.NEGATIVE + "Application is entering runlevel " + runlevel.name() + ", for reason: " + reason );

			onRunlevelChange();

			if ( currentRunlevel == Runlevel.DISPOSED )
				L.info( EnumColor.join( EnumColor.GOLD, EnumColor.NEGATIVE ) + "" + EnumColor.NEGATIVE + "Application has successfully shutdown! It took " + Timing.finish( runlevelTimingObject ) + "ms!" );
			else if ( currentRunlevel == Runlevel.STARTED )
				L.info( EnumColor.join( EnumColor.GOLD, EnumColor.NEGATIVE ) + "Application has successfully started! It took " + Timing.finish( runlevelTimingObject ) + "ms!" );
			else
				L.info( EnumColor.AQUA + "Application has entered runlevel " + runlevel.name() + ". It took " + Timing.finish( runlevelTimingObject ) + "ms!" );
		}
		catch ( ApplicationException.Error e )
		{
			ExceptionReport.handleSingleException( e );
		}
	}

	public static void setRunlevelLater( @Nonnull Runlevel level )
	{
		setRunlevelLater( level, null );
	}

	public static void setRunlevelLater( @Nonnull Runlevel level, @Nullable String reason )
	{
		Objs.notNull( level );
		LooperRouter.getMainLooper().postTask( () -> setRunlevel0( level, reason ) );
	}

	public static void shutdown( String reason )
	{
		setRunlevel( Runlevel.SHUTDOWN, reason );
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
		requireRunlevel( Runlevel.INITIALIZATION, "start() must be called at runlevel INITIALIZATION" );

		// Initiate startup procedures.
		setRunlevel( Runlevel.STARTUP );

		if ( !ConfigRegistry.config.getBoolean( Config.DISABLE_METRICS ) )
		{
			// TODO Implement!

			// Send Metrics

			final String instanceId = app.getEnv().getString( "instance-id" ).orElse( null );
		}

		// Join this thread to the main looper.
		LooperRouter.getMainLooper().joinLoop();

		// Sets the application to the disposed state once the joinLoop method returns exception free.
		setRunlevel( Runlevel.DISPOSED );
	}

	private Foundation()
	{
		// Static Access
	}

	public static class Config
	{
		/**
		 * Specifies built-in facades which can be registered here or by calling {@link io.amelia.foundation.bindings.FacadeRegistration#add(FacadeRegistration.Entry)} {@see Bindings#bindNamespace(String)}}.
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
		public static final TypeBase BINDINGS_FACADES = new TypeBase( "bindings.facades" );

		/**
		 * Specifies a config key for disabling a application metrics.
		 *
		 * <pre>
		 * app:
		 *   disableMetrics: false
		 * </pre>
		 */
		public static final TypeBase.TypeBoolean DISABLE_METRICS = new TypeBase.TypeBoolean( ConfigRegistry.Config.APPLICATION_BASE, "disableMetrics", false );

		private Config()
		{
			// Static Access
		}
	}
}
