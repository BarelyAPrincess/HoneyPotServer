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
import com.sun.istack.internal.NotNull;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.ReportingLevel;
import io.amelia.networking.NetworkLoader;
import io.amelia.networking.packets.RawPacket;
import io.amelia.support.Objs;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public class UDPHandler extends SimpleChannelInboundHandler<RawPacket>
{
	private final Queue<InboundHandlerTuplePacketListener> outboundPacketsQueue = Queues.newConcurrentLinkedQueue();
	private final Queue<RawPacket> receivedPacketsQueue = Queues.newConcurrentLinkedQueue();
	private Channel channel;
	private ClusterRole clusterRole;
	private boolean clusterRoleChanged = false;
	private InetSocketAddress inetSocketAddress;
	private UDPPacketHandler packetHandler;

	public UDPHandler( InetSocketAddress inetSocketAddress )
	{
		this.inetSocketAddress = inetSocketAddress;
	}

	public void closeChannel( String reason )// String reason )
	{
		if ( channel.isOpen() )
		{
			channel.close();
			// terminationReason = reason;
		}
	}

	private void dispatchPacket( final RawPacket packet, final GenericFutureListener[] listeners )
	{
		if ( !packet.checkUDPState( clusterRole ) )
			throw new ApplicationException.Runtime( ReportingLevel.E_ERROR, "Cluster role mismatch for packet " + packet.getClass().getSimpleName() + ", current role " + clusterRole.name() );

		Runnable func = () -> channel.writeAndFlush( packet ).addListeners( listeners ).addListener( ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE );

		if ( channel.eventLoop().inEventLoop() )
			func.run();
		else
			channel.eventLoop().execute( func );
	}

	@Override
	public void exceptionCaught( ChannelHandlerContext ctx, Throwable cause ) throws Exception
	{
		super.exceptionCaught( ctx, cause );
		closeChannel( cause instanceof TimeoutException ? "Stream Timeout" : "Stream Exception: " + cause.getMessage() );
	}

	@Override
	public void channelActive( ChannelHandlerContext ctx ) throws Exception
	{
		super.channelActive( ctx );

		channel = ctx.channel();
		setClusterRole( ClusterRole.MONITOR );
	}

	@Override
	public void channelInactive( ChannelHandlerContext ctx ) throws Exception
	{
		super.channelInactive( ctx );
		closeChannel( "End of Stream" );
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

	public InetSocketAddress getInetSocketAddress()
	{
		return inetSocketAddress;
	}

	public UDPPacketHandler getPacketHandler()
	{
		return packetHandler;
	}

	public void setPacketHandler( @NotNull UDPPacketHandler packetHandler )
	{
		Objs.notNull( packetHandler );
		NetworkLoader.L.fine( "Set packet handler of %s to %s", this, packetHandler );
		this.packetHandler = packetHandler;
	}

	@Override
	protected void messageReceived( ChannelHandlerContext ctx, RawPacket packet ) throws Exception
	{
		if ( channel.isOpen() )
		{
			if ( packet.hasPriority() )
				packet.processPacket( packetHandler );
			else
				receivedPacketsQueue.add( packet );
		}
	}

	public void processReceivedPacket()
	{
		flushOutboundQueue();
		if ( clusterRoleChanged )
		{
			packetHandler.onClusterRoleTransition( clusterRole );
			this.packetHandler = clusterRole.newPacketHandler();
		}

		if ( packetHandler != null )
		{
			for ( int i = 1000; !receivedPacketsQueue.isEmpty() && i >= 0; --i )
				receivedPacketsQueue.poll().processPacket( packetHandler );

			packetHandler.onNetworkTick();
		}

		channel.flush();
	}

	public void scheduleOutboundPacket( RawPacket packet, GenericFutureListener... listener )
	{
		if ( channel != null && channel.isOpen() )
		{
			flushOutboundQueue();
			dispatchPacket( packet, listener );
		}
		else
			outboundPacketsQueue.add( new InboundHandlerTuplePacketListener( packet, listener ) );
	}

	public void setClusterRole( ClusterRole clusterRole )
	{
		if ( this.clusterRole != clusterRole )
		{
			this.clusterRole = clusterRole;
			clusterRoleChanged = true;
			channel.config().setAutoRead( true );
		}
	}

	/**
	 * The HoneyPotServer clustering feature is very unique when compared to other clustering mechanisms.
	 * <p>
	 * First off the application makes use of a decentralized voting system where one member is always selected
	 * as the MASTER with the power to veto any action the cluster might take. The role of MASTER is very important
	 * to a healthy responsive cluster, so the role could be transferred at anytime to another member of the cluster.
	 * Reasons for a transfer of power would include low-latency and the instance going silent, no IAH packets.
	 * <p>
	 * Each cluster member consistency records the latency seen by the cluster members. Once the MASTER has gone
	 * silent or deemed lagging, each member will broadcast there latencies in a timeout/lagging acknowledgement
	 * packet, these packets are then received by their peers for analyzing. Each member will collect these latency
	 * reports including it's own and compile a rank based on the results, then broadcast these rankings. Once
	 * each member has gathered all the ranking reports, the new MASTER is selected based on a majority. The
	 * MEMBER who won the vote, will then send a MASTER acknowledgement packet.
	 * <p>
	 * When new members wishes to join, it will broadcast a cluster status packet, asking for each MEMBER of the cluster
	 * to acknowledge itself and other key details such as the software version number. If the new MEMBER is running an
	 * older version of the software it will notify the log and shutdown (unless auto-updates are enabled and the update
	 * is only a minor version. Major version will always shutdown and request they are manually updated with the --update
	 * argument.) If the new MEMBER is running a newer version, it will enter limbo (virtually creating a second cluster
	 * within a cluster) awaiting old members to leave the cluster and update. Once all old MEMBERS leave, a new MASTER
	 * vote takes place making the new virtual cluster the primary. This method ensures that when a cluster-wide software
	 * update takes place each member has a fair amount of time to shutdown, install, and reboot.
	 * Depending if the update was expected, the old MEMBERS will try to coordinate with the new MEMBERS for a peaceful
	 * takeover.
	 */
	public enum ClusterRole
	{
		/**
		 * Watching UDP traffic and sending queries to the cluster members.
		 */
		MONITOR( UDPPacketHandler::new ),
		/**
		 * Negotiating with the cluster, waiting to be promoted to a member of the cluster.
		 */
		NEGOTIATING( UDPPacketHandler::new ),
		/**
		 * Actively a voting member of the cluster.
		 */
		MEMBER( UDPPacketHandler::new ),
		/**
		 * Actively the master member of the cluster.
		 */
		MASTER( UDPPacketHandler::new );

		final Supplier<UDPPacketHandler> packetHandlerSupplier;

		ClusterRole( Supplier<UDPPacketHandler> packetHandlerSupplier )
		{
			this.packetHandlerSupplier = packetHandlerSupplier;
		}

		/**
		 * Produces a new UDP Packet Handler for use on this UDPHandler.
		 *
		 * @return new UDPPacketHandler instance;
		 */
		public UDPPacketHandler newPacketHandler()
		{
			return packetHandlerSupplier.get();
		}
	}

	static class InboundHandlerTuplePacketListener
	{
		private final GenericFutureListener[] listeners;
		private final RawPacket packet;

		public InboundHandlerTuplePacketListener( RawPacket packet, GenericFutureListener... listeners )
		{
			this.packet = packet;
			this.listeners = listeners;
		}
	}
}
