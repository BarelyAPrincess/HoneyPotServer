package io.amelia.monitor;

import io.amelia.events.EventDispatcher;
import io.amelia.events.application.RunlevelEvent;
import io.amelia.foundation.Kernel;
import io.amelia.foundation.MinimalApplication;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.Runlevel;
import io.amelia.lang.StartupException;
import io.amelia.lang.StartupInterruptException;
import io.amelia.networking.IPC;
import io.amelia.networking.NetworkLoader;
import io.amelia.networking.udp.UDPWorker;

public class EntryPoint
{
	public static void main( String... args ) throws Exception
	{
		/* Prepare the environment by downloading and applying the builtin libraries required */
		Kernel.prepare();

		/* Specify and get the ApplicationInterface for this environment. */
		Kernel.setApplicationInterface( MinimalApplication.class );
		MinimalApplication app = Kernel.getApplication();

		app.addArgument( "start", "Starts the daemon" );
		app.addArgument( "stop", "Stops the daemon" );
		app.addArgument( "status", "Prints the active daemon list" );

		final String instanceId = app.getEnv().getString( "instance-id" );

		/* Load up Network UDP Driver */
		final UDPWorker udp = NetworkLoader.UDP().get();

		EventDispatcher.listen( app, RunlevelEvent.class, ( event ) ->
		{
			if ( event.getRunLevel() == Runlevel.MAINLOOP )
			{
				try
				{
					udp.start();
				}
				catch ( ApplicationException e )
				{
					throw new StartupException( e );
				}

				if ( !udp.isStarted() )
					throw new StartupException( "The UDP service failed to start for unknown reasons." );
			}
			if ( event.getRunLevel() == Runlevel.DAEMON )
			{
				if ( app.hasArgument( "status" ) )
				{
					Kernel.L.info( "Waiting..." );
					IPC.status();
				}
				else if ( app.hasArgument( "stop" ) )
				{
					Kernel.L.info( "Stopping..." );
					IPC.stop( instanceId );
				}
				else if ( app.hasArgument( "start" ) )
				{
					Kernel.L.info( "Starting..." );
					IPC.start();
				}

			}
		} );

		try
		{
			app.parse( args );
		}
		catch ( StartupInterruptException e )
		{
			// Prevent exception from being printed to console
			return;
		}

		Kernel.start();
	}
}
