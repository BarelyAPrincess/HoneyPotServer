package io.amelia.networking.udp;

import com.google.common.collect.BiMap;
import io.amelia.lang.NetworkException;
import io.amelia.networking.NetworkLoader;
import io.amelia.networking.PacketPayload;
import io.amelia.networking.packets.Packet;
import io.amelia.support.Info;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

public class UDPPacketCodec extends ByteToMessageCodec<Object>
{
	@Override
	protected void encode( ChannelHandlerContext ctx, Object msg, ByteBuf out ) throws Exception
	{
		encode( ctx, ( Packet ) msg, out );
	}

	@Override
	protected void decode( ChannelHandlerContext ctx, ByteBuf in, List<Object> out ) throws Exception
	{
		int readable = in.readableBytes();

		if ( readable != 0 )
		{
			PacketPayload packetPayload = new PacketPayload( in );
			int var = packetPayload.readVarIntFromBuffer();
			Packet packet = Packet.generatePacket( channel().attr( UDPHandler.attrKeyReceivable ).get(), var );

			if ( packet == null )
				throw new NetworkException( "Bad packet id " + var );
			else
			{
				packet.readPacketData( packetPayload );

				if ( packetPayload.readableBytes() > 0 )
					throw new NetworkException( "Packet was larger than expected, found " + packetPayload.readableBytes() + " bytes extra whilst reading packet " + packet );
				else
				{
					out.add( packet );
					// field_152499_c.func_152469_a( var, ( long ) readable );

					if ( Info.isDevelopment() )
						NetworkLoader.L.debug( "IN: [%s:%s] %s[%s]", ctx.channel().attr( UDPHandler.attrKeyConnectionState ).get(), Integer.valueOf( var ), packet.getClass().getName(), packet.serialize() );
				}
			}
		}
	}

	protected void encode( ChannelHandlerContext ctx, Packet packet, ByteBuf out ) throws Exception
	{
		Integer var4 = ( Integer ) ( ( BiMap ) ctx.channel().attr( UDPHandler.attrKeySendable ).get() ).inverse().get( packet.getClass() );

		if ( Info.isDevelopment() )
			NetworkLoader.L.debug( "OUT: [{}:{}] {}[{}]", new Object[] {ctx.channel().attr( UDPWorker.attrKeyConnectionState ).get(), var4, packet.getClass().getName(), packet.serialize()} );

		if ( var4 == null )
			throw new NetworkException( "Can't serialize unregistered packet" );
		else
		{
			PacketPayload packetPayload = new PacketPayload( out );
			packetPayload.writeVarIntToBuffer( var4.intValue() );
			packet.writePacketData( packetPayload );
			// field_152500_c.func_152464_b( var4.intValue(), ( long ) packetPayload.readableBytes() );
		}
	}
}
