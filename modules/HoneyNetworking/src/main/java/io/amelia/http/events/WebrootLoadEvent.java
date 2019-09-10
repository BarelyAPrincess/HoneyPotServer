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

import io.amelia.events.ApplicationEvent;
import io.amelia.events.Cancellable;
import io.amelia.http.webroot.Webroot;

public class WebrootLoadEvent extends ApplicationEvent implements Cancellable
{
	Webroot webroot;

	public WebrootLoadEvent( Webroot webroot )
	{
		this.webroot = webroot;
	}

	public Webroot getWebroot()
	{
		return webroot;
	}
}
