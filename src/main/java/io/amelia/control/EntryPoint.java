package io.amelia.control;

import io.amelia.foundation.Application;
import io.amelia.foundation.Kernel;
import io.amelia.lang.StartupException;
import io.amelia.networking.NetworkLoader;
import io.amelia.networking.packets.PacketRequestInfo;
import io.amelia.networking.udp.UDPWorker;

public class EntryPoint
{
	public static void main( String... args ) throws Exception
	{
		/* Prepare the environment by downloading and applying the builtin libraries required */
		Kernel.prepare();

		Kernel.setApplicationInterface( Application.class );

		Application app = Kernel.application();
		UDPWorker udp = NetworkLoader.UDP().get();

		app.onArg( "start", "Starts the daemon", () ->
		{
			if ( !udp.isStarted() )
				throw new StartupException( "The UDP service failed to start for unknown reasons." );

			Kernel.L.info( "Starting..." );
			udp.sendPacket( new PacketRequestInfo(), r ->
			{

			} );
		} );

		app.onArg( "stop", "Stops the daemon", () ->
		{
			if ( !udp.isStarted() )
				throw new StartupException( "The UDP service failed to start for unknown reasons." );

			Kernel.L.info( "Stopping..." );
			udp.sendPacket( new PacketRequestStop(), r ->
			{

			} );
		} );

		app.onArg( "status", "Prints the active daemon list", () ->
		{
			if ( !udp.isStarted() )
				throw new StartupException( "The UDP service failed to start for unknown reasons." );

			Kernel.L.info( "Waiting..." );
			udp.sendPacket( new PacketRequestInfo(), r ->
			{
				L.info( "Found Instance: " + r.instanceId + " with IP " + r.ipAddress );
			} );
		} );

		app.onEvent( ApplicationInitEvent.class, ( event ) ->
		{
			udp.start();
		} );

		app.start( args );
	}
}
