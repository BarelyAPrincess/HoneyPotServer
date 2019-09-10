/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.net.ssl;

import java.net.IDN;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.net.ssl.SSLEngine;

import io.amelia.foundation.ConfigRegistry;
import io.amelia.lang.NetworkException;
import io.amelia.net.Networking;
import io.amelia.net.web.WebService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.ssl.SniHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

/**
 * <p>
 * Enables <a href="https://tools.ietf.org/html/rfc3546#section-3.1">SNI (Server Name Indication)</a> extension for server side SSL. For clients support SNI, the server could have multiple host name bound on a single IP. The client will send host name in
 * the handshake data so server could decide which certificate to choose for the host name.
 *
 * Original code from io.netty.handler.ssl.SniHandler, modified for Chiori-chan's Web Server use case.
 * </p>
 */
public class SniNegotiator extends ByteToMessageDecoder
{
	/**
	 * Constants for SSL packets.
	 */
	final class SslConstants
	{
		/**
		 * change cipher spec
		 */
		public static final int SSL_CONTENT_TYPE_CHANGE_CIPHER_SPEC = 20;

		/**
		 * alert
		 */
		public static final int SSL_CONTENT_TYPE_ALERT = 21;

		/**
		 * handshake
		 */
		public static final int SSL_CONTENT_TYPE_HANDSHAKE = 22;

		/**
		 * application data
		 */
		public static final int SSL_CONTENT_TYPE_APPLICATION_DATA = 23;

		private SslConstants()
		{
		}
	}

	private static final InternalLogger logger = InternalLoggerFactory.getInstance( SniHandler.class );

	static List<String> enabledCipherSuites = new CopyOnWriteArrayList<>();

	static
	{
		try
		{
			List<String> contents = ConfigRegistry.config.getStringList( WebService.ConfigKeys.ENABLED_CIPHER_SUITES );

			for ( String line : contents )
			{
				// TODO Comments are likely already filtered by the configuration library.

				if ( line.startsWith( "#" ) || line.length() == 0 )
					continue;

				if ( !enabledCipherSuites.contains( line ) )
					enabledCipherSuites.add( line );
			}
		}
		catch ( Throwable e )
		{
			Networking.L.severe( "Failed to load \"net.enabled-cipher-suites\" configuration list.", e );
		}

		if ( enabledCipherSuites.size() == 0 )
			throw new NetworkException.Runtime( "There were no cipher suites enabled, please check your EnabledCipherSuites file and/or consider adding additional ciphers." );
	}

	public static List<String> enabledCipherSuites()
	{
		return enabledCipherSuites;
	}

	private boolean handshaker = false;
	private volatile String hostname;
	private volatile SslContext selectedContext;

	@Override
	protected void decode( ChannelHandlerContext ctx, ByteBuf in, List<Object> out ) throws Exception
	{
		if ( !handshaker && in.readableBytes() >= 5 )
		{
			String hostname = sniHostNameFromHandshakeInfo( in );
			if ( hostname != null )
				hostname = IDN.toASCII( hostname, IDN.ALLOW_UNASSIGNED ).toLowerCase( Locale.US );
			this.hostname = hostname;

			selectedContext = Networking.getSslRegistry().map( hostname );

			if ( handshaker )
			{
				SSLEngine engine = selectedContext.newEngine( ctx.alloc() );

				List<String> supportedCipherSuites = Arrays.asList( engine.getSupportedCipherSuites() );

				if ( !supportedCipherSuites.containsAll( enabledCipherSuites ) )
					for ( String cipher : enabledCipherSuites )
						if ( !supportedCipherSuites.contains( cipher ) )
						{
							Networking.L.severe( String.format( "The SSL/TLS cipher suite '%s' is not supported by SSL Provider %s", cipher, SslContext.defaultServerProvider().name() ) );
							enabledCipherSuites.remove( cipher );
						}

				engine.setUseClientMode( false );
				engine.setEnabledCipherSuites( enabledCipherSuites.toArray( new String[0] ) );

				ctx.pipeline().replace( this, ctx.name(), new SslExceptionHandler( engine ) );
			}
		}
	}

	/**
	 * @return the selected hostname
	 */
	public String hostname()
	{
		return hostname;
	}

	private String sniHostNameFromHandshakeInfo( ByteBuf in )
	{
		int readerIndex = in.readerIndex();
		try
		{
			int command = in.getUnsignedByte( readerIndex );

			// tls, but not handshake command
			switch ( command )
			{
				case SslConstants.SSL_CONTENT_TYPE_CHANGE_CIPHER_SPEC:
				case SslConstants.SSL_CONTENT_TYPE_ALERT:
				case SslConstants.SSL_CONTENT_TYPE_APPLICATION_DATA:
					return null;
				case SslConstants.SSL_CONTENT_TYPE_HANDSHAKE:
					break;
				default:
					//not tls or sslv3, do not try sni
					handshaker = true;
					return null;
			}

			int majorVersion = in.getUnsignedByte( readerIndex + 1 );

			// SSLv3 or TLS
			if ( majorVersion == 3 )
			{
				int packetLength = in.getUnsignedShort( readerIndex + 3 ) + 5;

				if ( in.readableBytes() >= packetLength )
				{
					// decode the ssl client hello packet
					// we have to skip some var-length fields
					int offset = readerIndex + 43;

					int sessionIdLength = in.getUnsignedByte( offset );
					offset += sessionIdLength + 1;

					int cipherSuitesLength = in.getUnsignedShort( offset );
					offset += cipherSuitesLength + 2;

					int compressionMethodLength = in.getUnsignedByte( offset );
					offset += compressionMethodLength + 1;

					int extensionsLength = in.getUnsignedShort( offset );
					offset += 2;
					int extensionsLimit = offset + extensionsLength;

					while ( offset < extensionsLimit )
					{
						int extensionType = in.getUnsignedShort( offset );
						offset += 2;

						int extensionLength = in.getUnsignedShort( offset );
						offset += 2;

						// SNI
						if ( extensionType == 0 )
						{
							handshaker = true;
							int serverNameType = in.getUnsignedByte( offset + 2 );
							if ( serverNameType == 0 )
							{
								int serverNameLength = in.getUnsignedShort( offset + 3 );
								return in.toString( offset + 5, serverNameLength, CharsetUtil.UTF_8 );
							}
							else
								// invalid enum value
								return null;
						}

						offset += extensionLength;
					}

					handshaker = true;
					return null;
				}
				else
					// client hello incomplete
					return null;
			}
			else
			{
				handshaker = true;
				return null;
			}
		}
		catch ( Throwable e )
		{
			// unexpected encoding, ignore sni and use default
			if ( logger.isDebugEnabled() )
				logger.debug( "Unexpected client hello packet: " + ByteBufUtil.hexDump( in ), e );
			handshaker = true;
			return null;
		}
	}

	/**
	 * @return the selected sslContext
	 */
	public SslContext sslContext()
	{
		return selectedContext;
	}
}
