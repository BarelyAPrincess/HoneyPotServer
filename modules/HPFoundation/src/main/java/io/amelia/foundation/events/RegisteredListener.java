/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation.events;

import io.amelia.foundation.RegistrarBase;

import java.util.function.Consumer;

public final class RegisteredListener<E extends AbstractEvent>
{
	private final Consumer<E> listener;
	private final EventPriority priority;
	private final RegistrarBase registrar;

	public RegisteredListener( final RegistrarBase registrar, final EventPriority priority, final Consumer<E> listener )
	{
		this.registrar = registrar;
		this.priority = priority;
		this.listener = listener;
	}

	/**
	 * Calls the event executor
	 *
	 * @param event The event
	 * @throws EventException If an event handler throws an exception.
	 */
	public void callEvent( final E event ) throws EventException
	{
		if ( priority != EventPriority.MONITOR )
		{
			if ( event instanceof Cancellable && ( ( Cancellable ) event ).isCancelled() )
				return;
			if ( !event.onEventConditional( this ) )
				return;
		}

		listener.accept( event );
	}

	/**
	 * Gets the priority for this registration
	 *
	 * @return Registered Priority
	 */
	public EventPriority getPriority()
	{
		return priority;
	}

	/**
	 * Gets the plugin for this registration
	 *
	 * @return Registered Plugin
	 */
	public RegistrarBase getRegistrar()
	{
		return registrar;
	}
}
