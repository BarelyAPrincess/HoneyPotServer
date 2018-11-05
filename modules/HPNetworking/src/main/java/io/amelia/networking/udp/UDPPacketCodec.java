/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.networking.udp;

import java.net.DatagramPacket;
import java.util.List;

import io.amelia.lang.NetworkException;
import io.amelia.networking.NetworkLoader;
import io.amelia.networking.packets.RawPacket;
import io.amelia.support.DateAndTime;
import io.amelia.support.NIO;
import io.amelia.support.Objs;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

public class UDPPacketCodec extends MessageToMessageCodec<DatagramPacket, RawPacket>
{
	@Override
	protected void decode( ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out ) throws Exception
	{
		ByteBuf buffer = Unpooled.wrappedBuffer( msg.getData() );

		Class<? extends RawPacket> packetClass = NetworkLoader.getPacketClass( NIO.readByteBufToString( buffer, 32 ) ).orElse( null );
		RawPacket packet = packetClass == null ? null : Objs.initClass( packetClass );

		if ( packet == null )
			throw new NetworkException.Error( "We seemed to have a problem generating a packet from the supplied packet identifier." );

		buffer.skipBytes( 32 );

		packet.setPayload( buffer );
		packet.decode();

		if ( buffer.readableBytes() > 0 )
			throw new NetworkException.Error( "Packet was larger than expected, found " + buffer.readableBytes() + " bytes extra whilst reading packet " + packet );

		out.add( packet );
	}

	@Override
	protected void encode( ChannelHandlerContext ctx, RawPacket msg, List<Object> out ) throws Exception
	{
		// Validate the packet has all the required information
		msg.validate();

		// Instruct the packet to serialize the packet into a ByteBuf payload
		msg.encode();

		ByteBuf payload = msg.getPayload();

		if ( payload == null )
			throw new NetworkException.Error( msg.getClass().getSimpleName() + " had no payload. Is this a bug?" );

		byte[] bytes = NIO.readByteBufToBytes( payload );
		out.add( new DatagramPacket( bytes, bytes.length, ( ( UDPHandler ) ctx.pipeline().get( "handler" ) ).getInetSocketAddress() ) );

		msg.sentTime = DateAndTime.epoch();
		msg.sent = true;
	}
}
