/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.networking.packets;

import io.amelia.lang.NetworkException;
import io.amelia.networking.udp.UDPPacketHandler;
import io.amelia.support.NIO;
import io.netty.buffer.ByteBuf;

public class PacketRequestStop extends PacketRequest<PacketRequestStop, Object>
{
	public String instanceId = null;

	public PacketRequestStop( String instanceId )
	{
		this();
		this.instanceId = instanceId;
	}

	public PacketRequestStop()
	{
		super( () -> null );
	}

	@Override
	protected void decode( ByteBuf in )
	{
		instanceId = NIO.decodeStringFromByteBuf( in );
	}

	@Override
	protected void encode( ByteBuf out )
	{
		NIO.encodeStringToByteBuf( out, instanceId );
	}

	@Override
	public void processPacket( UDPPacketHandler packetHandler )
	{

	}

	@Override
	public void validate() throws NetworkException.PacketValidation
	{
		notEmpty( "instanceId" );
	}
}
