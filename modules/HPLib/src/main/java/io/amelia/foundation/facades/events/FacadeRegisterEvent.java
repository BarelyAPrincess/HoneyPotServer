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

import io.amelia.foundation.binding.AppBindings;
import io.amelia.foundation.facades.interfaces.FacadeService;

/**
 * This event is called when a service is registered.
 * <p>
 * Warning: The order in which register and unregister events are called should not be relied upon.
 */
public class FacadeRegisterEvent<T extends FacadeService> extends FacadeEvent<T>
{
	public FacadeRegisterEvent( AppBindings.RegisteredFacade<T> registeredFacade )
	{
		super( registeredFacade );
	}
}
