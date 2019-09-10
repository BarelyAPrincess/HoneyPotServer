/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.main;

import io.amelia.HoneyPotServer;
import io.amelia.events.Events;
import io.amelia.events.RunlevelEvent;
import io.amelia.foundation.Foundation;
import io.amelia.foundation.Kernel;
import io.amelia.foundation.Runlevel;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.ExceptionReport;
import io.amelia.foundation.FoundationCrashException;
import io.amelia.lang.StartupInterruptException;
import io.amelia.net.Networking;
import io.amelia.net.wip.NetworkLoader;
import io.amelia.net.wip.udp.UDPWorker;
import io.amelia.support.EnumColor;

public class EntryPoint
{
	public static void main( String... args ) throws Exception
	{
		try
		{
			Foundation.init();

			/* Specify the BaseApplication for this environment. */
			HoneyPotServer app = new HoneyPotServer();

			try
			{
				app.parse( args );
			}
			catch ( StartupInterruptException e )
			{
				// Prevent exception from being printed to console
				return;
			}

			Foundation.setApplication( app );

			/* Prepare the environment by downloading and applying builtin libraries required */
			Foundation.prepare();

			// Load up Network UDP Driver
			final UDPWorker udp = NetworkLoader.UDP();

			Events.getInstance().listen( app, RunlevelEvent.class, ( event ) -> {
				// Start the Networking
				if ( event.getRunLevel() == Runlevel.MAINLOOP )
				{
				/*try
				{
					udp.start();
				}
				catch ( ApplicationException.Error e )
				{
					throw new StartupException( e );
				}

				if ( !udp.isStarted() )
					throw new StartupException( "The UDP service failed to start for unknown reasons." );*/
				}

				// Make sure I'm the only process with my instanceId running
				if ( event.getRunLevel() == Runlevel.NETWORKING )
				{
					try
					{
						Networking.start();
					}
					catch ( ApplicationException.Error e )
					{
						ExceptionReport.handleSingleException( e );
					}
				}
			} );

			/* Tell the Kernel to start the startup sequence */
			Foundation.start();
		}
		catch ( FoundationCrashException muted )
		{
			// Muted since abort information is normally communicated before this point.
		}
		finally
		{
			Kernel.L.info( EnumColor.AQUA + "Application has finished and ran for a total of " + Kernel.uptimeDescribe() + "." );
		}
	}
}
