package io.amelia.main;

import io.amelia.events.EventDispatcher;
import io.amelia.events.application.RunlevelEvent;
import io.amelia.foundation.ApplicationOptions;
import io.amelia.HoneyPotServer;
import io.amelia.foundation.Kernel;
import io.amelia.foundation.binding.BindingRegistry;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.Runlevel;
import io.amelia.lang.StartupException;
import io.amelia.lang.StartupInterruptException;
import io.amelia.networking.NetworkLoader;
import io.amelia.networking.udp.UDPWorker;

public class EntryPoint
{
	public static void main( String... args ) throws Exception
	{
		/* Prepare the environment by downloading and applying the builtin libraries required */
		Kernel.prepare();

		/* Specify and get the ApplicationInterface for this environment. */
		Kernel.setApplicationInterface( HoneyPotServer.class );
		HoneyPotServer app = Kernel.getApplication();

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

		/* Load up Network UDP Driver */
		final UDPWorker udp = NetworkLoader.UDP().get();

		EventDispatcher.listen( app, RunlevelEvent.class, ( event ) ->
		{
			/* Start the Networking */
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
			/* Make sure I'm the only process with my instanceId running */
			if ( event.getRunLevel() == Runlevel.NETWORKING )
			{

			}
		} );

		/* Tell the Kernel the start the startup sequence */
		Kernel.start();
	}
}
