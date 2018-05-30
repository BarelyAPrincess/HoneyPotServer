/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <theameliadewitt@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.monitor;

import io.amelia.events.Events;
import io.amelia.foundation.Foundation;
import io.amelia.foundation.MinimalApplication;
import io.amelia.foundation.events.RunlevelEvent;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.NetworkException;
import io.amelia.lang.StartupException;
import io.amelia.lang.StartupInterruptException;
import io.amelia.networking.NetworkLoader;
import io.amelia.networking.ipc.IPC;
import io.amelia.networking.udp.UDPWorker;
import io.amelia.support.Runlevel;

public class EntryPoint
{
	public static void main( String... args ) throws Exception
	{
		// Prepare the environment by downloading and applying the builtin libraries required
		Foundation.prepare();

		// Specify the ApplicationInterface for this environment.
		MinimalApplication app = new MinimalApplication();
		Foundation.setApplication( app );

		app.addArgument( "start", "Starts the daemon" );
		app.addArgument( "stop", "Stops the daemon" );
		app.addArgument( "status", "Prints the active daemon list" );

		final String instanceId = app.getEnv().getString( "instance-id" ).orElse( null );

		// Load up Network UDP Driver
		final UDPWorker udp = NetworkLoader.UDP();

		Events.listen( app, RunlevelEvent.class, ( event ) -> {
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
			if ( event.getRunLevel() == Runlevel.STARTED )
			{
				if ( app.hasArgument( "status" ) )
				{
					Foundation.L.info( "Waiting..." );
					try
					{
						IPC.status();
					}
					catch ( NetworkException.PacketValidation packetValidation )
					{
						packetValidation.printStackTrace();
					}
				}
				else if ( app.hasArgument( "stop" ) )
				{
					Foundation.L.info( "Stopping..." );
					IPC.stop( instanceId );
				}
				else if ( app.hasArgument( "start" ) )
				{
					Foundation.L.info( "Starting..." );
					try
					{
						IPC.start();
					}
					catch ( NetworkException.PacketValidation packetValidation )
					{
						packetValidation.printStackTrace();
					}
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

		Foundation.start();
	}
}
