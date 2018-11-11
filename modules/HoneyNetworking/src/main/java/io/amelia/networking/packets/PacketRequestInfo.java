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
