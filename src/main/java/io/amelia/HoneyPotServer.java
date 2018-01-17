package io.amelia;

import java.io.InputStream;

import io.amelia.foundation.DefaultApplication;
import io.amelia.foundation.Env;
import io.amelia.foundation.Foundation;
import io.amelia.foundation.Kernel;
import io.amelia.foundation.NetworkedApplication;
import io.amelia.foundation.PropDevMeta;
import io.amelia.foundation.parcel.ParcelCarrier;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.ParcelException;
import io.amelia.logcompat.DefaultLogFormatter;
import io.amelia.logcompat.LogBuilder;
import io.amelia.logcompat.Logger;
import io.amelia.networking.NetworkLoader;
import io.amelia.support.Encrypt;
import io.amelia.support.EnumColor;
import io.amelia.support.IO;
import io.amelia.support.Runlevel;
import io.amelia.support.Sys;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import static io.amelia.support.Runlevel.INITIALIZATION;
import static io.amelia.support.Runlevel.MAINLOOP;
import static io.amelia.support.Runlevel.SHUTDOWN;

public class HoneyPotServer extends DefaultApplication implements NetworkedApplication
{
	/**
	 * Provides shortcut CONST for keyed directory paths
	 */
	public static final String PATH_WEBROOT = "__webroot";
	public static final String PATH_DATABASE = "__database";
	public static final String PATH_SESSIONS = "__sessions";

	public HoneyPotServer() throws ApplicationException.Error
	{
		/* Register keyed directory paths with the ConfigRegistry */
		Kernel.setPath( PATH_WEBROOT, Kernel.PATH_APP, "webroot" );
		Kernel.setPath( PATH_DATABASE, Kernel.PATH_APP, "database" );
		Kernel.setPath( PATH_SESSIONS, Kernel.PATH_STORAGE, "sessions" );

		Kernel.setDevMeta( new PropDevMeta( HoneyPotServer.class, "build.properties" ) );

		addArgument( "console-fancy", "Specifies if control characters are written with console output to stylize it, e.g., fgcolor, bgcolor, bold, or inverted." );
		addStringArgument( "cluster-id", "Specifies the cluster unique identity" );
		addStringArgument( "instance-id", "Specifies the instance unique identity" );
	}

	@Override
	public void handleParcel( ParcelCarrier parcelCarrier ) throws ParcelException.Error
	{
		// TODO Nothing Yet!
	}

	@Override
	public void onRunlevelChange( Runlevel previousRunlevel, Runlevel currentRunlevel ) throws ApplicationException.Error
	{
		super.onRunlevelChange( previousRunlevel, currentRunlevel );

		if ( currentRunlevel == INITIALIZATION )
		{
			if ( Sys.isAdminUser() )
				Kernel.L.warning( "We detected that you are running this application with the system admin account. This is highly discouraged, it may compromise security or file permissions." );

			// Minimum 1GB memory recommended.
			if ( Runtime.getRuntime().maxMemory() / 1024L / 1024L < 1024L )
				LogBuilder.get().warning( "We detected less than the recommended 1024Mb of JVM ram, we recommended you dedicate more ram to guarantee a smoother experience. You can use the JVM options \"-Xmx1024M -Xms1024M\" to set the ram at 1GB." );

			try
			{
				Env env = getEnv();

				/* Check instance-id */
				if ( !env.isValueSet( "instance-id" ) )
					env.set( "instance-id", Encrypt.uuid(), true );

				LogBuilder.setConsoleFormatter( new DefaultLogFormatter( env.getBoolean( "console-fancy" ) ) );
			}
			catch ( Exception e )
			{
				throwStartupException( e );
			}

			// TODO Install or factory reset option?

			// TODO Extract Web UI archive to webroot.

			if ( Sys.isUnixLikeOS() )
			{
				SignalHandler signalHandler = new SignalHandler()
				{
					@Override
					public void handle( Signal signal )
					{
						Foundation.shutdown( "Received SIGTERM - Terminate" );
					}
				};

				Signal.handle( new Signal( "TERM" ), signalHandler );
				Signal.handle( new Signal( "INT" ), signalHandler );
			}
		}
		if ( currentRunlevel == MAINLOOP )
			getLooper().postTaskRepeatingLater( NetworkLoader::heartbeat, 50L, 50L );
		if ( currentRunlevel == SHUTDOWN )
		{
			LogBuilder.get().info( "Shutting Down Plugin Manager..." );
			// PluginManager.shutdown();

			LogBuilder.get().info( "Shutting Down Permission Manager..." );
			// PermissionDispatcher.shutdown();

			LogBuilder.get().info( "Shutting Down Account Manager..." );
			// AccountManager.shutdown();
		}
	}

	@Override
	public void sendToAll( ParcelCarrier parcel )
	{
		// TODO Distribute the parcel to all available receivers.
	}

	@Override
	public void showBanner( Logger logger )
	{
		InputStream is = getClass().getClassLoader().getResourceAsStream( "banner.txt" );
		if ( is != null )
			for ( String line : IO.readStreamToLines( is ) )
				LogBuilder.get().info( EnumColor.GOLD + line );

		super.showBanner( logger );
	}
}
