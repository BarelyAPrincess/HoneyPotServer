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

import com.google.common.collect.Queues;
import io.amelia.networking.packets.Packet;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.SocketAddress;
import java.util.Queue;

public class UDPHandler extends SimpleChannelInboundHandler<Packet>
{
	private final Queue outboundPacketsQueue = Queues.newConcurrentLinkedQueue();
	private final Queue receivedPacketsQueue = Queues.newConcurrentLinkedQueue();
	private Channel channel;
	private SocketAddress socketAddress;

	private void dispatchPacket( final Packet packet, final GenericFutureListener[] listeners )
	{
		final ConnectionState state1 = ConnectionState.func( packet );
		final ConnectionState state2 = ( ConnectionState ) channel.attr( attrKeyConnectionState ).get();

		if ( state1 != state2 )
			channel.config().setAutoRead( false );

		Runnable func = () ->
		{
			if ( state1 != state2 )
				setConnectionState( state1 );

			channel.writeAndFlush( packet ).addListeners( listeners ).addListener( ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE );
		};

		if ( channel.eventLoop().inEventLoop() )
			func.run();
		else
			channel.eventLoop().execute( func );
	}

	@Override
	public void exceptionCaught( ChannelHandlerContext ctx, Throwable cause ) throws Exception
	{
		super.exceptionCaught( ctx, cause );

		/*
		* ChatComponentTranslation var3;

        if (p_exceptionCaught_2_ instanceof TimeoutException)
        {
            var3 = new ChatComponentTranslation("disconnect.timeout", new Object[0]);
        }
        else
        {
            var3 = new ChatComponentTranslation("disconnect.genericReason", new Object[] {"Internal Exception: " + p_exceptionCaught_2_});
        }

        this.closeChannel(var3);
		*/

		// TODO When there is a problem with the UDP protocol, shutdown or retry? Send reason!
	}

	@Override
	public void channelActive( ChannelHandlerContext ctx ) throws Exception
	{
		super.channelActive( ctx );
		channel = ctx.channel();
		socketAddress = channel.remoteAddress();
		setConnectionState( ConnectionState.HANDSHAKING );
	}

	@Override
	public void channelInactive( ChannelHandlerContext ctx ) throws Exception
	{
		super.channelInactive( ctx );
		closeChannel();
	}

	private void flushOutboundQueue()
	{
		if ( channel != null && channel.isOpen() )
			while ( !this.outboundPacketsQueue.isEmpty() )
			{
				InboundHandlerTuplePacketListener packetListener = ( InboundHandlerTuplePacketListener ) outboundPacketsQueue.poll();
				dispatchPacket( packetListener.packet, packetListener.listeners );
			}
	}

	@Override
	protected void messageReceived( ChannelHandlerContext ctx, Packet packet ) throws Exception
	{
		if ( channel.isOpen() )
		{
			if ( packet.hasPriority() )
				packet.processPacket( netHandler );
			else
				receivedPacketsQueue.add( packet );
		}
	}

	public void processReceivedPacket()
	{
		flushOutboundQueue();
		ConnectionState state = ( ConnectionState ) channel.attr( attrKeyConnectionState ).get();

		if ( connectionState != state )
		{
			if ( connectionState != null )
				netHandler.onConnectionStateTransition( connectionState, state );

			connectionState = state;
		}

		if ( netHandler != null )
		{
			for ( int i = 1000; !receivedPacketsQueue.isEmpty() && i >= 0; --i )
			{
				receivedPacketsQueue.poll().processPacket( netHandler );
			}

			netHandler.onNetworkTick();
		}

		channel.flush();
	}

	public SocketAddress getSocketAddress()
	{
		return socketAddress;
	}

	public void closeChannel()// String reason )
	{
		if ( channel.isOpen() )
		{
			channel.close();
			// terminationReason = reason;
		}
	}

	public void scheduleOutboundPacket( Packet packet, GenericFutureListener... listener )
	{
		if ( channel != null && channel.isOpen() )
		{
			flushOutboundQueue();
			dispatchPacket( packet, listener );
		}
		else
			outboundPacketsQueue.add( new InboundHandlerTuplePacketListener( packet, listener ) );
	}

	public void setConnectionState( ConnectionState connectionState )
	{
		this.connectionState = ( EnumConnectionState ) this.channel.attr( attrKeyConnectionState ).getAndSet( connectionState );
		this.channel.attr( attrKeyReceivable ).set( connectionState.func_150757_a( this.isClientSide ) );
		this.channel.attr( attrKeySendable ).set( connectionState.func_150754_b( this.isClientSide ) );
		this.channel.config().setAutoRead( true );
		logger.debug( "Enabled auto read" );
	}

	enum ConnectionState
	{
		HANDSHAKING
	}

	static class InboundHandlerTuplePacketListener
	{
		private final GenericFutureListener[] listeners;
		private final Packet packet;

		public InboundHandlerTuplePacketListener( Packet packet, GenericFutureListener... listeners )
		{
			this.packet = packet;
			this.listeners = listeners;
		}
	}
}
