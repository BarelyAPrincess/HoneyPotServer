package io.amelia.foundation;

import io.amelia.lang.ApplicationException;
import io.amelia.lang.Runlevel;
import io.amelia.logcompat.LogBuilder;
import io.amelia.tasks.TaskDispatcher;

/**
 * Implements a basic application environment with modules Config, Tasks, Events.
 */
public abstract class DefaultApplication extends ApplicationInterface
{
	private static String stopReason = null;

	static
	{
		System.setProperty( "file.encoding", "utf-8" );
	}

	@Override
	public void onRunlevelChange( Runlevel previousRunlevel, Runlevel currentRunlevel ) throws ApplicationException
	{
		if ( currentRunlevel == Runlevel.SHUTDOWN )
		{
			LogBuilder.get().info( "Shutting Down Task Manager..." );
			TaskDispatcher.shutdown();

			LogBuilder.get().info( "Saving Configuration..." );
			ConfigRegistry.save();

			try
			{
				App.L.info( "Clearing Excess Cache..." );
				long keepHistory = ConfigRegistry.config.getLong( "advanced.execute.keepHistory" ).orElse( 30L );
				ConfigRegistry.clearCache( keepHistory );
			}
			catch ( IllegalArgumentException e )
			{
				App.L.warning( "Cache directory is invalid!" );
			}
		}
	}

	@Override
	protected void onTick( int currentTick, float averageTick ) throws ApplicationException
	{
		// CommandDispatch.handleCommands();
		TaskDispatcher.heartbeat( currentTick );
	}
}
