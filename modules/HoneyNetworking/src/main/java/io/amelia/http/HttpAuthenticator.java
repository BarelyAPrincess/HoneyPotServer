/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http;

import io.amelia.support.Encrypt;
import io.amelia.support.Strs;
import io.netty.handler.codec.http.HttpHeaderNames;

public class HttpAuthenticator
{
	private final HttpRequestWrapper request;
	private String cached = null;

	public HttpAuthenticator( HttpRequestWrapper request )
	{
		this.request = request;
	}

	public String getAuthorization()
	{
		if ( cached == null )
			cached = request.getHeader( HttpHeaderNames.AUTHORIZATION );
		return cached;
	}

	public String getDigest()
	{
		if ( !isDigest() )
			throw new IllegalStateException( "Authorization is invalid!" );

		return null;
	}

	public String getPassword()
	{
		if ( !isBasic() )
			throw new IllegalStateException( "Authorization is invalid!" );

		String auth = Encrypt.base64DecodeString( Strs.regexCapture( getAuthorization(), "Basic (.*)" ) );
		return auth.substring( auth.indexOf( ":" ) + 1 );
	}

	public String getType()
	{
		return isBasic() ? "Basic" : "Digest";
	}

	public String getUsername()
	{
		if ( !isBasic() )
			throw new IllegalStateException( "Authorization is invalid!" );

		String auth = Encrypt.base64DecodeString( Strs.regexCapture( getAuthorization(), "Basic (.*)" ) );
		return auth.substring( 0, auth.indexOf( ":" ) );
	}

	public boolean isBasic()
	{
		String var = getAuthorization();
		return var != null && var.startsWith( "Basic" );
	}

	public boolean isDigest()
	{
		String var = getAuthorization();
		return var != null && var.startsWith( "Digest" );
	}
}
