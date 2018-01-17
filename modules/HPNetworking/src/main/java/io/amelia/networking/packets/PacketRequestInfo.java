package io.amelia.networking.packets;

import java.util.function.Supplier;

import io.amelia.lang.NetworkException;
import io.amelia.networking.udp.UDPPacketHandler;
import io.netty.buffer.ByteBuf;

public class PacketRequestInfo extends PacketRequest<PacketRequestInfo, Object>
{
	public String instanceId;
	public String ipAddress;

	public PacketRequestInfo( Supplier responsePacketSupplier )
	{
		super( responsePacketSupplier );
	}

	@Override
	public void validate() throws NetworkException.PacketValidation
	{

	}

	@Override
	protected void encode( ByteBuf out )
	{

	}

	@Override
	public void processPacket( UDPPacketHandler packetHandler )
	{

	}

	@Override
	protected void decode( ByteBuf in )
	{

	}
}
