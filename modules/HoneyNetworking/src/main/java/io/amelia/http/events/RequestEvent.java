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

import javax.annotation.Nonnull;

import io.amelia.events.Cancellable;
import io.amelia.http.HttpRequestWrapper;

public class RequestEvent extends NetworkEvent implements Cancellable
{
	private final HttpRequestWrapper request;
	private String reason = null;
	private int statusNo = 200;

	public RequestEvent( @Nonnull HttpRequestWrapper request )
	{
		this.request = request;
	}

	public void clearError()
	{
		statusNo = 200;
		reason = null;
	}

	public HttpRequestWrapper getFramework()
	{
		return request;
	}

	public String getReason()
	{
		return reason;
	}

	public int getStatus()
	{
		return statusNo;
	}

	public void setStatus( int statusNo )
	{
		setStatus( statusNo, null );
	}

	/*
	 * public Long getServerLong( ServerVars serverVar )
	 * {
	 * try
	 * {
	 * return (Long) _server.get( serverVar );
	 * }
	 * catch ( Exception e )
	 * {
	 * return 0L;
	 * }
	 * }
	 * public Integer getServerInt( ServerVars serverVar )
	 * {
	 * try
	 * {
	 * return (Integer) _server.get( serverVar );
	 * }
	 * catch ( Exception e )
	 * {
	 * return 0;
	 * }
	 * }
	 * public String getServerString( ServerVars serverVar )
	 * {
	 * try
	 * {
	 * return (String) _server.get( serverVar );
	 * }
	 * catch ( Exception e )
	 * {
	 * return "";
	 * }
	 * }
	 */

	public void setStatus( int statusNo0, String reason0 )
	{
		statusNo = statusNo0;
		reason = reason0;
	}
}
