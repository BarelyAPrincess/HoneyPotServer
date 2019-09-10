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

import javax.annotation.Nonnull;

import io.amelia.support.HttpCookie;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;

public class HoneyCookie implements HttpCookie<HoneyCookie>
{
	@Nonnull
	public static HoneyCookie empty( String name )
	{
		if ( name == null || name.length() == 0 )
			throw new IllegalArgumentException( "Cookie name must not be blank!" );
		return new HoneyCookie( name, null );
	}

	@Nonnull
	public static HoneyCookie from( Cookie cookie )
	{
		HoneyCookie honeyCookie = new HoneyCookie( cookie.name(), cookie.value() );
		honeyCookie.setDomain( cookie.domain() );
		honeyCookie.setWrap( cookie.wrap() );
		honeyCookie.setSecure( cookie.isSecure() );
		honeyCookie.setPath( cookie.path() );
		honeyCookie.setValue( cookie.value() );
		honeyCookie.setMaxAge( cookie.maxAge() );
		honeyCookie.setHttpOnly( cookie.isHttpOnly() );
		return honeyCookie;
	}

	@Nonnull
	public static HoneyCookie from( javax.servlet.http.Cookie cookie )
	{
		HoneyCookie honeyCookie = new HoneyCookie( cookie.getName(), cookie.getValue() );
		honeyCookie.setComment( cookie.getComment() );
		honeyCookie.setDomain( cookie.getDomain() );
		honeyCookie.setSecure( cookie.getSecure() );
		honeyCookie.setPath( cookie.getPath() );
		honeyCookie.setValue( cookie.getValue() );
		honeyCookie.setMaxAge( cookie.getMaxAge() );
		return honeyCookie;
	}

	private String comment;
	private String domain;
	private boolean httpOnly;
	private long maxAge;
	private String name;
	private boolean needsUpdating;
	private Cookie nettyCookie = null;
	private String path;
	private boolean secure;
	private javax.servlet.http.Cookie servletCookie = null;
	private String value;
	private boolean wrap;

	private HoneyCookie( String name, String value )
	{
		this.name = name;
		this.value = value;

		needsUpdating = true;
	}

	@Override
	public String getComment()
	{
		return comment;
	}

	@Override
	public String getDomain()
	{
		return domain;
	}

	@Override
	public long getMaxAge()
	{
		return maxAge;
	}

	@Override
	public String getName()
	{
		return name;
	}

	public Cookie getNettyCookie()
	{
		if ( nettyCookie == null || needsUpdating )
		{
			nettyCookie = new DefaultCookie( getName(), getValue() );
			// Comments are not supported in Netty Cookies.
			nettyCookie.setDomain( getDomain() );
			nettyCookie.setMaxAge( getMaxAge() );
			nettyCookie.setPath( getPath() );
			nettyCookie.setValue( getValue() );
			nettyCookie.setWrap( getWrap() );
			nettyCookie.setHttpOnly( isHttpOnly() );
			nettyCookie.setSecure( isSecure() );
		}
		return nettyCookie;
	}

	@Override
	public String getPath()
	{
		return path;
	}

	public javax.servlet.http.Cookie getServletCookie()
	{
		if ( servletCookie == null || needsUpdating )
		{
			servletCookie = new javax.servlet.http.Cookie( getName(), getValue() );
			servletCookie.setComment( getComment() );
			servletCookie.setDomain( getDomain() );
			servletCookie.setSecure( isSecure() );
			servletCookie.setPath( getPath() );
			servletCookie.setValue( getValue() );
			servletCookie.setMaxAge( ( int ) getMaxAge() );
		}
		return servletCookie;
	}

	@Override
	public String getValue()
	{
		return value;
	}

	@Override
	public boolean getWrap()
	{
		return wrap;
	}

	@Override
	public boolean isHttpOnly()
	{
		return httpOnly;
	}

	@Override
	public boolean isSecure()
	{
		return secure;
	}

	@Override
	public boolean needsUpdating()
	{
		return needsUpdating;
	}

	@Override
	public HoneyCookie setComment( String comment )
	{
		this.comment = comment;
		needsUpdating = true;
		return this;
	}

	@Override
	public HoneyCookie setDomain( String domain )
	{
		this.domain = domain;
		needsUpdating = true;
		return this;
	}

	@Override
	public HoneyCookie setHttpOnly( boolean httpOnly )
	{
		this.httpOnly = httpOnly;
		needsUpdating = true;
		return this;
	}

	@Override
	public HoneyCookie setMaxAge( long maxAge )
	{
		this.maxAge = maxAge;
		needsUpdating = true;
		return this;
	}

	@Override
	public HoneyCookie setPath( String path )
	{
		this.path = path;
		needsUpdating = true;
		return this;
	}

	@Override
	public HoneyCookie setSecure( boolean secure )
	{
		this.secure = secure;
		needsUpdating = true;
		return this;
	}

	@Override
	public HoneyCookie setValue( String value )
	{
		this.value = value;
		needsUpdating = true;
		return this;
	}

	@Override
	public HoneyCookie setWrap( boolean wrap )
	{
		this.wrap = wrap;
		needsUpdating = true;
		return this;
	}

	@Override
	public HoneyCookie unsetNeedsUpdating()
	{
		needsUpdating = false;
		return this;
	}
}
