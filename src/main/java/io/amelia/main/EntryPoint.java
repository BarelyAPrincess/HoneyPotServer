package io.amelia.main;

import io.amelia.HoneyPotServer;
import io.amelia.foundation.Foundation;
import io.amelia.foundation.events.Events;
import io.amelia.foundation.events.application.RunlevelEvent;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.StartupException;
import io.amelia.lang.StartupInterruptException;
import io.amelia.networking.NetworkLoader;
import io.amelia.networking.udp.UDPWorker;
import io.amelia.support.Runlevel;

public class EntryPoint
{
	public static void main( String... args ) throws Exception
	{
		/* Prepare the environment by downloading and applying the builtin libraries required */
		Foundation.prepare();

		/* Specify the ApplicationInterface for this environment. */
		HoneyPotServer app = new HoneyPotServer();
		Foundation.setApplication( app );

		try
		{
			app.parse( args );
		}
		catch ( StartupInterruptException e )
		{
			// Prevent exception from being printed to console
			return;
		}

		final String instanceId = app.getEnv().getString( "instance-id" );

		// Load up Network UDP Driver
		final UDPWorker udp = NetworkLoader.UDP().get();

		Events.listen( app, RunlevelEvent.class, ( event ) -> {
			// Start the Networking
			if ( event.getRunLevel() == Runlevel.MAINLOOP )
			{
				try
				{
					udp.start();
				}
				catch ( ApplicationException.Error e )
				{
					throw new StartupException( e );
				}

				if ( !udp.isStarted() )
					throw new StartupException( "The UDP service failed to start for unknown reasons." );
			}
			// Make sure I'm the only process with my instanceId running
			if ( event.getRunLevel() == Runlevel.NETWORKING )
			{

			}
		} );

		/* Tell the Kernel the start the startup sequence */
		Foundation.start();
	}
}
