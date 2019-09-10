/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http.events;

import io.amelia.http.HttpRequestWrapper;
import io.amelia.http.HttpResponseWrapper;
import io.amelia.lang.HttpCode;
import io.netty.handler.codec.http.HttpResponseStatus;

public class HttpErrorEvent extends HttpEvent
{
	private HttpCode httpCode;
	private final HttpRequestWrapper request;
	private String errorHtml = "";
	private boolean isDevelopmentMode;
	private String statusReason;

	public HttpErrorEvent( HttpRequestWrapper request, HttpCode httpCode, String statusReason, boolean isDevelopmentMode )
	{
		this.httpCode = httpCode;
		this.statusReason = statusReason;
		this.request = request;
		this.isDevelopmentMode = isDevelopmentMode;
	}

	public String getErrorHtml()
	{
		if ( errorHtml.isEmpty() )
			return null;

		return errorHtml;
	}

	public HttpCode getHttpCode()
	{
		return httpCode;
	}

	public String getHttpReason()
	{
		return statusReason;
	}

	public HttpRequestWrapper getRequest()
	{
		return request;
	}

	public HttpResponseWrapper getResponse()
	{
		return request.getResponse();
	}

	public boolean isDevelopmentMode()
	{
		return isDevelopmentMode;
	}

	public void setErrorHtml( String errorHtml )
	{
		this.errorHtml = errorHtml;
	}

	public void setHttpCode( HttpCode httpCode )
	{
		this.httpCode = httpCode;
	}

	public void setHttpCode( HttpResponseStatus httpResponseStatus )
	{
		this.httpCode = HttpCode.getHttpCode( httpResponseStatus ).orElseThrow( IllegalArgumentException::new );
	}

	public void setHttpCode( int statusCode )
	{
		this.httpCode = HttpCode.getHttpCode( statusCode ).orElseThrow( IllegalArgumentException::new );
	}

	public void setHttpReason( String statusReason )
	{
		this.statusReason = statusReason;
	}
}
