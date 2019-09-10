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

import java.io.IOException;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

import io.amelia.net.Networking;
import io.amelia.support.Strs;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ssl.NotSslRecordException;
import io.netty.handler.ssl.SslHandler;

public class SslExceptionHandler extends SslHandler
{
	public SslExceptionHandler( SSLEngine engine )
	{
		super( engine );
	}

	public SslExceptionHandler( SSLEngine engine, boolean startTls )
	{
		super( engine, startTls );
	}

	@Override
	public void exceptionCaught( ChannelHandlerContext ctx, Throwable cause ) throws Exception
	{
		/*
		 * Common SSL exception such as those below are thrown thru this exceptionCaught() method first
		 *
		 * javax.net.ssl.SSLHandshakeException: Client requested protocol SSLv3 not enabled or not supported
		 * javax.net.ssl.SSLHandshakeException: no cipher suites in common
		 *
		 * Issues catching:
		 * io.netty.handler.ssl.NotSslRecordException: not an SSL/TLS record: 474554202f20485454502f312e310d0a486f73743...
		 */

		// io.netty.handler.ssl.NotSslRecordException;

		if ( cause instanceof NotSslRecordException )
			Networking.L.severe( "Not an SSL/TLS record" );
		if ( cause instanceof SSLException || !( cause instanceof IOException ) )
		{
			String protocol = Strs.regexCapture( cause.getMessage(), "Client requested protocol (.*) not enabled or not supported" );

			if ( protocol != null )
				Networking.L.severe( String.format( "Client tried to negotiate a SSL connection using protocol version %s, which is currently disabled or not supported", protocol ) );
			else if ( cause.getMessage().contains( "SSLv2Hello is disabled" ) )
				Networking.L.severe( "Client tried to negotiate a SSL connection using protocol version SSLv2Hello, which is currently disabled" );
			else if ( cause.getMessage().contains( "no cipher suites in common" ) )
				// Expand on this exception and see if it would be possible to discover which cipher suites the client was expecting
				Networking.L.severe( "Client tried to negotiate a SSL connection but the server and client had no cipher suites in common" );
			else
				Networking.L.severe( "Caught an unexpected yet severe exception while trying to negotiate the SSL connection", cause );
		}
		else
			super.exceptionCaught( ctx, cause );
	}
}
