package io.amelia.networking.packets;

import io.amelia.lang.PacketValidationException;
import io.netty.buffer.ByteBuf;

public class PacketRequestStop extends PacketRequest<PacketRequestStop>
{
	public String instanceId = null;

	public PacketRequestStop( String instanceId )
	{
		this.instanceId = instanceId;
	}

	public PacketRequestStop()
	{

	}

	@Override
	public void validate() throws PacketValidationException
	{
		notEmpty( "instanceId" );
	}

	@Override
	protected void encode( ByteBuf out )
	{
		writeBlob( out, instanceId );
	}

	@Override
	protected void decode( ByteBuf in )
	{
		instanceId = readBlob( in );
	}
}
