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

import io.amelia.foundation.facades.FacadeService;
import io.amelia.foundation.facades.Facades;

/**
 * This event is called when a facade is unregistered.
 * <p>
 * Warning: The order in which register and unregister events are called should not be relied upon.
 */
public class FacadeUnregisterEvent<T extends FacadeService> extends FacadeEvent<T>
{
	public FacadeUnregisterEvent( Facades.RegisteredFacade<T> serviceProvider )
	{
		super( serviceProvider );
	}
}
