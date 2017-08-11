package io.amelia.networking.packets;

import io.amelia.lang.PacketValidationException;
import io.netty.buffer.ByteBuf;

public class PacketRequestInfo extends PacketRequest<PacketRequestInfo>
{
	public String instanceId;
	public String ipAddress;

	@Override
	public void validate() throws PacketValidationException
	{

	}

	@Override
	protected void encode( ByteBuf out )
	{

	}

	@Override
	protected void decode( ByteBuf in )
	{

	}
}
