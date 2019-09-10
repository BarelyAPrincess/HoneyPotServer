/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

public class HttpInitializer extends ChannelInitializer<SocketChannel>
{
	public static final List<WeakReference<SocketChannel>> activeChannels = new CopyOnWriteArrayList<>();

	@Override
	protected void initChannel( SocketChannel ch )
	{
		ChannelPipeline p = ch.pipeline();

		p.addLast( "decoder", new HttpRequestDecoder() );
		p.addLast( "aggregator", new HttpObjectAggregator( 104857600 ) ); // One Hundred Megabytes
		p.addLast( "encoder", new HttpResponseEncoder() );
		p.addLast( "deflater", new HttpContentCompressor() );
		p.addLast( "handler", new HttpHandler( false ) );

		activeChannels.add( new WeakReference<>( ch ) );
	}
}
