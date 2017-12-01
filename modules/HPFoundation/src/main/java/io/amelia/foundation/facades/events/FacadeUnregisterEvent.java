/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation.facades.events;

import io.amelia.foundation.service.ServiceDispatcher;
import io.amelia.foundation.service.ServiceProvider;

/**
 * This event is called when a service is unregistered.
 * <p>
 * Warning: The order in which register and unregister events are called should not be relied upon.
 */
public class FacadeUnregisterEvent<T> extends FacadeEvent<T>
{
	public FacadeUnregisterEvent( ServiceDispatcher.RegisteredService<T, ServiceProvider> serviceProvider )
	{
		super( serviceProvider );
	}
}
