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

import io.netty.handler.ssl.SslContext;

public class SslCertificateDefaultEvent extends NetworkEvent
{
	private final String hostname;
	private SslContext context = null;

	public SslCertificateDefaultEvent( String hostname )
	{
		this.hostname = hostname;
	}

	public String getHostname()
	{
		return hostname;
	}

	public SslContext getSslContext()
	{
		return context;
	}

	public void setContext( SslContext context )
	{
		this.context = context;
	}
}
