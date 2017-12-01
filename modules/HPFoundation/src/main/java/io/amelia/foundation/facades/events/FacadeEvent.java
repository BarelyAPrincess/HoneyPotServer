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

import io.amelia.events.application.ApplicationEvent;
import io.amelia.foundation.binding.AppBindings;
import io.amelia.foundation.facades.interfaces.FacadeService;

/**
 * An event relating to a registered facades. This is called in a {@link AppBindings}
 */
public abstract class FacadeEvent<T extends FacadeService> extends ApplicationEvent
{
	private final AppBindings.RegisteredFacade<T> facade;

	public FacadeEvent( final AppBindings.RegisteredFacade<T> facade )
	{
		this.facade = facade;
	}

	public AppBindings.RegisteredFacade<T> getFacade()
	{
		return facade;
	}
}
