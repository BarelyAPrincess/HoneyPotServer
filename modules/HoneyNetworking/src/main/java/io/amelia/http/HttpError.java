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

import io.amelia.lang.NetworkException;
import io.netty.handler.codec.http.HttpResponseStatus;

public class HttpError extends NetworkException.Error
{
	private static final long serialVersionUID = 8116947267974772489L;

	int statusCode;
	String statusReason;

	public HttpError( HttpResponseStatus status )
	{
		this( status, null );
	}

	public HttpError( HttpResponseStatus status, String developerMessage )
	{
		super( developerMessage == null ? status.reasonPhrase().toString() : developerMessage );
		statusCode = status.code();
		statusReason = status.reasonPhrase().toString();
	}

	public HttpError( int statusCode )
	{
		this( statusCode, null );
	}

	public HttpError( int statusCode, String statusReason )
	{
		this( statusCode, statusReason, null );
	}

	public HttpError( int statusCode, String statusReason, String developerMessage )
	{
		super( developerMessage == null ? statusReason : developerMessage );

		this.statusCode = statusCode;
		this.statusReason = statusReason;
	}

	public HttpError( Throwable cause, String developerMessage )
	{
		super( developerMessage == null ? HttpResponseStatus.INTERNAL_SERVER_ERROR.reasonPhrase().toString() : developerMessage, cause );

		statusCode = 500;
		statusReason = HttpResponseStatus.INTERNAL_SERVER_ERROR.reasonPhrase().toString();
	}

	public int getHttpCode()
	{
		return statusCode < 100 ? 500 : statusCode;
	}

	public HttpResponseStatus getHttpResponseStatus()
	{
		return HttpResponseStatus.valueOf( statusCode );
	}

	public String getReason()
	{
		return statusReason == null ? HttpResponseStatus.valueOf( statusCode ).reasonPhrase().toString() : statusReason;
	}
}
