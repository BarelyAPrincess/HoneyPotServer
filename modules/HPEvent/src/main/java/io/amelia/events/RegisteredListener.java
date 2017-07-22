/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.events;

import io.amelia.foundation.RegistrarBase;

public class RegisteredListener
{
	private final RegistrarBase context;
	private final EventExecutor executor;
	private final boolean ignoreCancelled;
	private final Listener listener;
	private final EventPriority priority;

	public RegisteredListener( final Listener listener, final EventExecutor executor, final EventPriority priority, final RegistrarBase context, final boolean ignoreCancelled )
	{
		this.listener = listener;
		this.priority = priority;
		this.context = context;
		this.executor = executor;
		this.ignoreCancelled = ignoreCancelled;
	}

	/**
	 * Calls the event executor
	 *
	 * @param event The event
	 * @throws EventException If an event handler throws an exception.
	 */
	public void callEvent( final AbstractEvent event ) throws EventException
	{
		if ( event instanceof Cancellable )
			if ( ( ( Cancellable ) event ).isCancelled() && isIgnoringCancelled() )
				return;

		if ( event instanceof Conditional )
			if ( priority != EventPriority.MONITOR && !( ( Conditional ) event ).conditional( this ) )
				return;

		executor.execute( listener, event );
	}

	/**
	 * Gets the plugin for this registration
	 *
	 * @return Registered Plugin
	 */
	public RegistrarBase getContext()
	{
		return context;
	}

	/**
	 * Gets the listener for this registration
	 *
	 * @return Registered Listener
	 */
	public Listener getListener()
	{
		return listener;
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
	 * Whether this listener accepts cancelled events
	 *
	 * @return True when ignoring cancelled events
	 */
	public boolean isIgnoringCancelled()
	{
		return ignoreCancelled;
	}
}
