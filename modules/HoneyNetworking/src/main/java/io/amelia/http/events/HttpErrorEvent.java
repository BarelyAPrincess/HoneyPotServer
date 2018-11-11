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

public class HttpErrorEvent extends HttpEvent
{
	private final HttpRequestWrapper request;
	private String errorHtml = "";
	private boolean isDevelopmentMode;
	private int statusCode;
	private String statusReason;

	public HttpErrorEvent( HttpRequestWrapper request, int statusCode, String statusReason, boolean isDevelopmentMode )
	{
		this.statusCode = statusCode;
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

	public void setErrorHtml( String errorHtml )
	{
		this.errorHtml = errorHtml;
	}

	public int getHttpCode()
	{
		return statusCode;
	}

	public void setHttpCode( int statusCode )
	{
		this.statusCode = statusCode;
	}

	public String getHttpReason()
	{
		return statusReason;
	}

	public void setHttpReason( String statusReason )
	{
		this.statusReason = statusReason;
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
}
