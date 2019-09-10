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

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.amelia.foundation.ConfigRegistry;
import io.amelia.foundation.Foundation;
import io.amelia.foundation.Kernel;
import io.amelia.lang.NetworkException;
import io.amelia.lang.StartupException;
import io.amelia.net.wip.NetworkLoader;
import io.amelia.support.NIO;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class HttpWorker
{
	private List<Channel> listeningChannels = new ArrayList<>();
	private ProtocolGroup secure = null;
	private ProtocolGroup unsecure = null;

	public HttpWorker()
	{
		try
		{
			int httpPort = ConfigRegistry.config.getInteger( NetworkLoader.ConfigKeys.HTTP_PORT ).orElse( 8080 );

			{
				Optional<Integer> argOptional = Foundation.getApplication().getIntegerArgument( "http-port" );
				if ( argOptional.isPresent() )
					httpPort = argOptional.get();
			}

			if ( httpPort > 0 && httpPort < 65536 )
			{
				unsecure = new ProtocolGroup( "Unsecure HTTP" );
				unsecure.addListeningPort( httpPort );
			}

			int httpsPort = ConfigRegistry.config.getInteger( NetworkLoader.ConfigKeys.HTTPS_PORT ).orElse( 8443 );

			{
				Optional<Integer> argOptional = Foundation.getApplication().getIntegerArgument( "https-port" );
				if ( argOptional.isPresent() )
					httpsPort = argOptional.get();
			}

			if ( httpsPort > 0 && httpsPort < 65536 )
			{
				secure = new ProtocolGroup( "Secure HTTPS" );
				secure.addListeningPort( httpsPort );
			}
		}
		catch ( StartupException e )
		{
			throw e;
		}
		catch ( Throwable t )
		{
			throw new StartupException( t );
		}
	}

	public boolean isRunning()
	{
		return listeningChannels.size() > 0;
	}

	public void shutdown()
	{
		for ( Channel channel : listeningChannels )
			channel.closeFuture();
		// NIO.closeQuietly( channel );
	}

	public class ProtocolGroup
	{
		List<InetSocketAddress> listeningSockets = new ArrayList<>();
		String protocolName;
		ServerBootstrap serverBootstrap;

		public ProtocolGroup( String protocolName )
		{
			this.protocolName = protocolName;

			serverBootstrap = new ServerBootstrap();
			serverBootstrap.group( NetworkLoader.IO_LOOP_GROUP ).channel( NioServerSocketChannel.class ).childHandler( new HttpInitializer() );
		}

		public void addListeningPort( int port ) throws NetworkException.Error
		{
			if ( port < 1 || port > 35535 )
				throw new NetworkException.Error( "Port number for protocol " + protocolName + " must be within the range of 1-65535." );

			if ( NIO.isPrivilegedPort( port ) )
			{
				NetworkLoader.L.warning( "It would seem that you are trying to start " + protocolName + " on a privileged port without root access." );
				NetworkLoader.L.warning( "We will attempt to still start but we can't guarantee it's success. http://www.w3.org/Daemon/User/Installation/PrivilegedPorts.html" );
			}

			listeningSockets.add( new InetSocketAddress( port ) );

			Channel channel;
			try
			{
				channel = serverBootstrap.bind( new InetSocketAddress( port ) ).sync().channel();
			}
			catch ( InterruptedException e )
			{
				throw new NetworkException.Error( e );
			}

			Kernel.getExecutorParallel().execute( () -> {
				try
				{
					channel.closeFuture().sync();
				}
				catch ( InterruptedException e )
				{
					e.printStackTrace();
				}

				NetworkLoader.L.info( "The " + protocolName + " protocol has been shutdown!" );
			} );

			listeningChannels.add( channel );
		}
	}
}
