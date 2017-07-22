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

import io.amelia.lang.NetworkException;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class UDPInitializer extends ChannelInitializer<NioDatagramChannel>
{
	UDPCodec codec;

	public UDPInitializer() throws NetworkException
	{
		codec = new UDPCodec();
	}

	@Override
	protected void initChannel( NioDatagramChannel ch ) throws Exception
	{
		ChannelPipeline p = ch.pipeline();

		p.addLast( codec );
		p.addLast( new UDPHandler() );
	}
}
