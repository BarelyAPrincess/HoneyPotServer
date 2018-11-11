/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http.events;

import io.amelia.http.HttpRequestWrapper;
import io.amelia.http.HttpResponseWrapper;

public class HttpExceptionEvent extends HttpEvent
{
	private final Throwable cause;
	private final HttpRequestWrapper request;
	private String errorHtml = "";
	private int httpCode = 500;
	private boolean isDevelopmentMode;

	public HttpExceptionEvent( HttpRequestWrapper request, Throwable cause, boolean isDevelopmentMode )
	{
		this.cause = cause;
		this.request = request;
		this.isDevelopmentMode = isDevelopmentMode;
	}

	public String getErrorHtml()
	{
		if ( errorHtml.isEmpty() )
			return null;

		return errorHtml;
	}

	public void setErrorHtml( String errorHtml )
	{
		this.errorHtml = errorHtml;
	}

	public int getHttpCode()
	{
		return httpCode;
	}

	public void setHttpCode( int httpCode )
	{
		this.httpCode = httpCode;
	}

	public HttpRequestWrapper getRequest()
	{
		return request;
	}

	public HttpResponseWrapper getResponse()
	{
		return request.getResponse();
	}

	public Throwable getThrowable()
	{
		return cause;
	}

	public boolean isDevelopmentMode()
	{
		return isDevelopmentMode;
	}
}
