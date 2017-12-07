package io.amelia;

import io.amelia.config.Env;
import io.amelia.foundation.DefaultApplication;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.Runlevel;
import io.amelia.logcompat.DefaultLogFormatter;
import io.amelia.logcompat.LogBuilder;
import io.amelia.networking.NetworkLoader;
import io.amelia.support.Encrypt;

import static io.amelia.lang.Runlevel.INITIALIZATION;
import static io.amelia.lang.Runlevel.SHUTDOWN;

public class HoneyPotServer extends DefaultApplication
{
	/**
	 * Provides shortcut CONST for keyed directory paths
	 */
	public static final String PATH_WEBROOT = "__webroot";
	public static final String PATH_DATABASE = "__database";
	public static final String PATH_SESSIONS = "__sessions";

	public HoneyPotServer()
	{
		/* Register keyed directory paths with the ConfigRegistry */
		App.setPath( PATH_WEBROOT, App.PATH_APP, "webroot" );
		App.setPath( PATH_DATABASE, App.PATH_APP, "database" );
		App.setPath( PATH_SESSIONS, App.PATH_STORAGE, "sessions" );

		addArgument( "console-fancy", "Specifies if control characters are written with console output to stylize it, e.g., fgcolor, bgcolor, bold, or inverted." );
		addStringArgument( "cluster-id", "Specifies the cluster unique identity" );
		addStringArgument( "instance-id", "Specifies the instance unique identity" );
	}

	@Override
	public void onRunlevelChange( Runlevel previousRunlevel, Runlevel currentRunlevel ) throws ApplicationException
	{
		super.onRunlevelChange( previousRunlevel, currentRunlevel );

		if ( currentRunlevel == INITIALIZATION )
		{
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
		}
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
	protected void onTick( int currentTick, float averageTick ) throws ApplicationException
	{
		super.onTick( currentTick, averageTick );

		NetworkLoader.heartbeat();
	}
}
