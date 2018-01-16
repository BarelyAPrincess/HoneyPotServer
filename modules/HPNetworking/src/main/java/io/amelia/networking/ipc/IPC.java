package io.amelia.networking.ipc;

import io.amelia.foundation.Kernel;
import io.amelia.foundation.parcel.ParcelCarrier;
import io.amelia.lang.PacketValidationException;
import io.amelia.networking.NetworkLoader;
import io.amelia.networking.packets.PacketRequestInfo;
import io.amelia.networking.packets.PacketRequestStop;
import io.amelia.networking.udp.UDPWorker;
import io.amelia.support.data.Parcel;

public class IPC
{
	/**
	 * temp?
	 */
	public static void processIncomingParcel( Parcel src )
	{
		ParcelCarrier parcelCarrier = Parcel.Factory.deserialize( src, ParcelCarrier.class );

		parcelCarrier.sendToTarget();
	}

	public static void start()
	{
		udp().sendPacket( new PacketRequestInfo(), r -> {

		} );
	}

	public static void status()
	{
		udp().sendPacket( new PacketRequestInfo(), r -> {
			Kernel.L.info( "Found Instance: " + r.instanceId + " with IP " + r.ipAddress );
		} );
	}

	public static void stop( String instanceId )
	{
		try
		{
			udp().sendPacket( new PacketRequestStop( instanceId ), r -> {

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
