package io.amelia.networking;

import io.amelia.foundation.Kernel;
import io.amelia.lang.PacketValidationException;
import io.amelia.networking.packets.PacketRequestInfo;
import io.amelia.networking.packets.PacketRequestStop;
import io.amelia.networking.udp.UDPWorker;

public class IPC
{
	public static void start()
	{
		udp().sendPacket( new PacketRequestInfo(), r ->
		{

		} );
	}

	public static void status()
	{
		udp().sendPacket( new PacketRequestInfo(), r ->
		{
			Kernel.L.info( "Found Instance: " + r.instanceId + " with IP " + r.ipAddress );
		} );
	}

	public static void stop( String instanceId )
	{
		try
		{
			udp().sendPacket( new PacketRequestStop( instanceId ), r ->
			{

			} );
		}
		catch ( PacketValidationException e )
		{

		}
	}

	private static UDPWorker udp()
	{
		return NetworkLoader.UDP().get();
	}

	private IPC()
	{
		// Static Class
	}
}
