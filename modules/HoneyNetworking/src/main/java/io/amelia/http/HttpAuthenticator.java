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

import io.amelia.support.Encrypt;
import io.amelia.support.Strs;
import io.netty.handler.codec.http.HttpHeaderNames;

import static javax.servlet.http.HttpServletRequest.BASIC_AUTH;
import static javax.servlet.http.HttpServletRequest.CLIENT_CERT_AUTH;
import static javax.servlet.http.HttpServletRequest.DIGEST_AUTH;
import static javax.servlet.http.HttpServletRequest.FORM_AUTH;

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
		if ( !isTypeDigest() )
			throw new IllegalStateException( "Authorization is not type digest!" );

		return null;
	}

	public String getPassword()
	{
		if ( !isTypeBasic() )
			throw new IllegalStateException( "Authorization is not type basic!" );

		String auth = Encrypt.base64DecodeString( Strs.regexCapture( getAuthorization(), "Basic (.*)" ) );
		return auth.substring( auth.indexOf( ":" ) + 1 );
	}

	public String getType()
	{
		String auth = getAuthorization();
		if ( auth != null )
		{
			if ( auth.startsWith( "Basic" ) )
				return BASIC_AUTH;
			if ( auth.startsWith( "Digest" ) )
				return DIGEST_AUTH;
			// TODO What do these auth types start with?
			// return FORM_AUTH;
			// return CLIENT_CERT_AUTH;
		}
		return null;
	}

	public String getUsername()
	{
		if ( !isTypeBasic() )
			throw new IllegalStateException( "Authorization is not type basic!" );

		String auth = Encrypt.base64DecodeString( Strs.regexCapture( getAuthorization(), "Basic (.*)" ) );
		return auth.substring( 0, auth.indexOf( ":" ) );
	}

	public boolean isTypeBasic()
	{
		return getType().equals( BASIC_AUTH );
	}

	public boolean isTypeCert()
	{
		return getType().equals( CLIENT_CERT_AUTH );
	}

	public boolean isTypeDigest()
	{
		return getType().equals( DIGEST_AUTH );
	}

	public boolean isTypeForm()
	{
		return getType().equals( FORM_AUTH );
	}
}
