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

import com.sun.istack.internal.NotNull;

import java.nio.charset.Charset;
import java.util.Map;

import javax.annotation.Nonnull;

import io.amelia.http.HttpHandler;
import io.amelia.http.HttpRequestWrapper;
import io.amelia.http.HttpResponseWrapper;
import io.amelia.http.session.Session;
import io.amelia.http.webroot.BaseWebroot;
import io.netty.buffer.ByteBuf;

public class RenderEvent extends NetworkEvent
{
	private final HttpHandler handler;
	private final Map<String, String> params;
	private Charset encoding;
	private ByteBuf source;

	public RenderEvent( @Nonnull HttpHandler handler, @Nonnull ByteBuf source, @Nonnull Charset encoding, @NotNull Map<String, String> params )
	{
		this.handler = handler;
		this.source = source;
		this.encoding = encoding;
		this.params = params;
	}

	public Charset getEncoding()
	{
		return encoding;
	}

	public Map<String, String> getParams()
	{
		return params;
	}

	public HttpRequestWrapper getRequest()
	{
		return handler.getRequest();
	}

	public String getRequestId()
	{
		return handler.getSession().getSessionId();
	}

	public HttpResponseWrapper getResponse()
	{
		return handler.getResponse();
	}

	public Session getSession()
	{
		return handler.getSession();
	}

	public ByteBuf getSource()
	{
		return source.copy();
	}

	public void setSource( ByteBuf source )
	{
		this.source = source;
	}

	public BaseWebroot getWebroot()
	{
		return handler.getRequest() == null ? null : handler.getRequest().getWebroot();
	}
}
