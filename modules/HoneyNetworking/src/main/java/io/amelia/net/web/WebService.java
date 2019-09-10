/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.net.web;

import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.nio.file.Paths;
import java.util.Arrays;

import io.amelia.data.TypeBase;
import io.amelia.foundation.ConfigRegistry;
import io.amelia.foundation.Kernel;
import io.amelia.lang.ExceptionContext;
import io.amelia.lang.ExceptionReport;
import io.amelia.lang.StartupException;
import io.amelia.looper.Delays;
import io.amelia.looper.LooperRouter;
import io.amelia.net.ssl.SslInitializer;
import io.amelia.net.NetworkService;
import io.amelia.net.Networking;
import io.amelia.support.NIO;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class WebService implements NetworkService
{
	private Channel httpChannel;
	private InetSocketAddress httpSocket;
	private Channel sslChannel;
	private InetSocketAddress sslSocket;

	@Override
	public String getId()
	{
		return "web";
	}

	@Override
	public void shutdown()
	{

	}

	public boolean isHttpRunning()
	{
		// TODO
		return true;
	}

	public boolean isHttpsRunning()
	{
		// TODO
		return true;
	}

	@Override
	public void start()
	{
		int httpPort = ConfigRegistry.config.getValue( ConfigKeys.UNSECURE_PORT );
		int sslPort = ConfigRegistry.config.getValue( ConfigKeys.SECURE_PORT );

		if ( httpPort <= 0 && sslPort <= 0 )
			throw new RuntimeException( "Invalid Port Numbers" );

		if ( NIO.isPrivilegedPort( httpPort ) || NIO.isPrivilegedPort( sslPort ) )
		{
			Networking.L.warning( "It would seem that you are trying to start the HTTP/HTTPS Service on a privileged port without root access." );
			Networking.L.warning( "We will attempt to still start the service but we can't guarantee it's success. http://www.w3.org/Daemon/User/Installation/PrivilegedPorts.html" );
		}

		httpSocket = new InetSocketAddress( httpPort );
		sslSocket = new InetSocketAddress( sslPort );

		try
		{
			if ( httpPort > 0 )
			{
				Networking.L.info( "Starting HTTP Service on port " + httpPort + "!" );

				ServerBootstrap b = new ServerBootstrap();
				b.group( Networking.IO_LOOP_GROUP ).channel( NioServerSocketChannel.class ).childHandler( new HttpInitializer() );

				httpChannel = b.bind( httpSocket ).sync().channel();

				// HTTP Server Thread
				Kernel.getExecutorParallel().execute( () -> {
					try
					{
						httpChannel.closeFuture().sync();
					}
					catch ( InterruptedException e )
					{
						e.printStackTrace();
					}

					Networking.L.info( "The HTTP Server has been shutdown!" );
				} );
			}

			if ( sslPort > 0 )
			{
				Networking.L.info( "Starting HTTPS Service on port " + sslPort + "!" );

				ServerBootstrap b = new ServerBootstrap();
				b.group( Networking.IO_LOOP_GROUP ).channel( NioServerSocketChannel.class ).childHandler( new SslInitializer() );

				sslChannel = b.bind( sslSocket ).sync().channel();

				// HTTPS Server Thread
				Kernel.getExecutorParallel().execute( () -> {
					try
					{
						sslChannel.closeFuture().sync();
					}
					catch ( InterruptedException e )
					{
						e.printStackTrace();
					}

					Networking.L.info( "The HTTPS Server has been shutdown!" );
				} );
			}
		}
		catch ( NullPointerException e )
		{
			throw new StartupException( "There was a problem starting the Web Server. Check logs and try again.", e );
		}
		catch ( Throwable e )
		{
			if ( e instanceof ExceptionContext )
				ExceptionReport.handleSingleException( e );
			else
				throw e instanceof StartupException ? ( StartupException ) e : new StartupException( e );
		}

		LooperRouter.getMainLooper().postTaskRepeatingLater( entry -> {
			for ( WeakReference<SocketChannel> ref : HttpInitializer.activeChannels )
				if ( ref.get() == null )
					HttpInitializer.activeChannels.remove( ref );
			for ( WeakReference<SocketChannel> ref : SslInitializer.activeChannels )
				if ( ref.get() == null )
					SslInitializer.activeChannels.remove( ref );
		}, Delays.SECOND_15, Delays.SECOND_15, true );
	}

	public static class ConfigKeys
	{
		public static final TypeBase ROOT_PATH = new TypeBase( "net" );

		public static final TypeBase HTTP_PATH = new TypeBase( ROOT_PATH, "http" );
		public static final TypeBase.TypeInteger UNSECURE_PORT = new TypeBase.TypeInteger( HTTP_PATH, "http.port", 8088 );

		public static final TypeBase HTTPS_PATH = new TypeBase( ROOT_PATH, "https" );
		public static final TypeBase.TypeInteger SECURE_PORT = new TypeBase.TypeInteger( HTTPS_PATH, "https.port", 8443 );

		public static final TypeBase SSL_PATH = new TypeBase( ROOT_PATH, "ssl" );
		public static final TypeBase.TypeStringList ENABLED_CIPHER_SUITES = new TypeBase.TypeStringList( SSL_PATH, "enabled-cipher-suites", Arrays.asList( "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256", "TLS_DHE_DSS_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA", "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA", "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256", "TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA", "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256", "TLS_DHE_DSS_WITH_AES_128_CBC_SHA", "TLS_DHE_DSS_WITH_AES_128_CBC_SHA256", "TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_128_CBC_SHA256", "TLS_RSA_WITH_AES_128_GCM_SHA256", "TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA", "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA", "TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA", "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA", "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256", "TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256", "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256", "TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256" ) );
		public static final TypeBase.TypePath SSL_SHARED_KEY = new TypeBase.TypePath( SSL_PATH, "sharedKey", Paths.get( "server.key" ) );
		public static final TypeBase.TypePath SSL_SHARED_CERT = new TypeBase.TypePath( SSL_PATH, "sharedCert", Paths.get( "server.crt" ) );
		public static final TypeBase.TypeString SSL_SHARED_SECRET = new TypeBase.TypeString( SSL_PATH, "sharedSecret", ( String ) null );
	}
}
