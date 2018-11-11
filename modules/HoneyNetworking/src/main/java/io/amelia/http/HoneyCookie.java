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

import io.netty.handler.codec.http.cookie.DefaultCookie;

public class HoneyCookie extends DefaultCookie
{
	private boolean needsUpdating = false;

	/**
	 * Creates a new cookie with the specified name and value.
	 *
	 * @param name
	 * @param value
	 */
	public HoneyCookie( String name, String value )
	{
		super( name, value );
	}

	@Override
	public void setMaxAge( long maxAge )
	{
		super.setMaxAge( maxAge );
		needsUpdating = true;
	}

	@Override
	public void setWrap( boolean wrap )
	{
		super.setWrap( wrap );
		needsUpdating = true;
	}

	@Override
	public void setValue( String value )
	{
		super.setValue( value );
		needsUpdating = true;
	}

	@Override
	public void setSecure( boolean secure )
	{
		super.setSecure( secure );
		needsUpdating = true;
	}

	@Override
	public void setPath( String path )
	{
		super.setPath( path );
		needsUpdating = true;
	}

	@Override
	public void setDomain( String domain )
	{
		super.setDomain( domain );
		needsUpdating = true;
	}

	@Override
	public void setHttpOnly( boolean httpOnly )
	{
		super.setHttpOnly( httpOnly );
		needsUpdating = true;
	}

	public boolean needsUpdating()
	{
		return needsUpdating;
	}

	public void unsetNeedsUpdating()
	{
		needsUpdating = false;
	}
}
