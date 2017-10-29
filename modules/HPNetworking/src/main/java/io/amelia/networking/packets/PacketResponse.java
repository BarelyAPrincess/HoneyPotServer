package io.amelia.networking.packets;

public abstract class PacketResponse<T extends PacketRequest> extends RawPacket
{
	public PacketResponse( T requestPacket )
	{
		super( requestPacket.getPacketId() );
	}
}
