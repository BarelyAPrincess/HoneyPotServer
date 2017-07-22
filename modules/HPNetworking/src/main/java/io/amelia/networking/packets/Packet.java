package io.amelia.networking.packets;

import io.amelia.lang.PacketValidationException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public abstract class Packet<T>
{
	public String packetId;
	public ByteBuf payload;
	public long sentTime;
	public boolean sent;

	public void encode()
	{
		payload = Unpooled.buffer();
		encode( payload );


	}

	public String getPacketId()
	{
		return packetId;
	}

	public long getSentTime()
	{
		return sentTime;
	}

	public abstract void validate() throws PacketValidationException;

	protected abstract void encode( ByteBuf out );

	protected abstract void decode( ByteBuf in );
}
