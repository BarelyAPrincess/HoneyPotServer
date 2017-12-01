package io.amelia.networking.udp;

import io.amelia.lang.NetworkException;
import io.amelia.networking.NetworkLoader;
import io.amelia.networking.packets.RawPacket;
import io.amelia.support.DateAndTime;
import io.amelia.support.IO;
import io.amelia.support.Objs;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.net.DatagramPacket;
import java.util.List;

public class UDPPacketCodec extends MessageToMessageCodec<DatagramPacket, RawPacket>
{
	@Override
	protected void encode( ChannelHandlerContext ctx, RawPacket msg, List<Object> out ) throws Exception
	{
		// Validate the packet has all the required information
		msg.validate();

		// Instruct the packet to serialize the packet into a ByteBuf payload
		msg.encode();

		ByteBuf payload = msg.getPayload();

		if ( payload == null )
			throw new NetworkException( msg.getClass().getSimpleName() + " had no payload. Is this a bug?" );

		byte[] bytes = IO.readByteBufferToBytes( payload.nioBuffer() );
		out.add( new DatagramPacket( bytes, bytes.length, ( ( UDPHandler ) ctx.pipeline().get( "handler" ) ).getInetSocketAddress() ) );

		msg.sentTime = DateAndTime.epoch();
		msg.sent = true;
	}

	@Override
	protected void decode( ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out ) throws Exception
	{
		ByteBuf buffer = Unpooled.wrappedBuffer( msg.getData() );

		Class<? extends RawPacket> packetClass = NetworkLoader.getPacketClass( IO.readByteBufferToString( buffer.nioBuffer(), 32 ) ).orElse( null );
		RawPacket packet = packetClass == null ? null : Objs.initClass( packetClass );

		if ( packet == null )
			throw new NetworkException( "We seemed to have a problem generating a packet from the supplied packet identifier." );

		buffer.skipBytes( 32 );

		packet.setPayload( buffer );
		packet.decode();

		if ( buffer.readableBytes() > 0 )
			throw new NetworkException( "Packet was larger than expected, found " + buffer.readableBytes() + " bytes extra whilst reading packet " + packet );

		out.add( packet );
	}
}
