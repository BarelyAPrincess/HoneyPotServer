/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation.service.events;

import io.amelia.events.application.ApplicationEvent;
import io.amelia.foundation.service.ServiceDispatcher;
import io.amelia.foundation.service.ServiceProvider;

/**
 * An event relating to a registered service. This is called in a {@link ServiceDispatcher}
 */
public abstract class ServiceEvent<T> extends ApplicationEvent
{
	private final ServiceDispatcher.RegisteredService<T, ? extends ServiceProvider> provider;

	public ServiceEvent( final ServiceDispatcher.RegisteredService<T, ? extends ServiceProvider> provider )
	{
		this.provider = provider;
	}

	public ServiceDispatcher.RegisteredService<T, ? extends ServiceProvider> getProvider()
	{
		return provider;
	}
}
