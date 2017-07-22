/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.networking.udp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class UDPHandler extends SimpleChannelInboundHandler<ByteBuf>
{
	@Override
	protected void messageReceived( ChannelHandlerContext ctx, ByteBuf msg ) throws Exception
	{
		UDPService.getLogger().debug( "Received: " + msg.readLong() );
	}
}
