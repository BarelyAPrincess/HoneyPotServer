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
import io.amelia.support.ConsumerWithException;

public final class RegisteredListener<E extends AbstractEvent>
{
	private static Class<?> getCommonSuperclass( Class<?> class1, Class<?> class2 )
	{
		while ( !class1.isAssignableFrom( class2 ) )
			class1 = class1.getSuperclass();
		return class1;
	}

	private final ConsumerWithException<E, EventException.Error> listener;
	private final EventPriority priority;
	private final RegistrarBase registrar;
	private int count;
	private Class<? extends AbstractEvent> eventClass;
	private boolean multiple = false;
	private long totalTime;
	private boolean useTimings;

	public RegisteredListener( final RegistrarBase registrar, final EventPriority priority, final ConsumerWithException<E, EventException.Error> listener )
	{
		this.registrar = registrar;
		this.priority = priority;
		this.listener = listener;
	}

	/**
	 * Calls the event executor
	 *
	 * @param event The event
	 *
	 * @throws EventException.Error If an event handler throws an exception.
	 */
	public void callEvent( final E event ) throws EventException.Error
	{
		if ( useTimings )
		{
			if ( event.isAsynchronous() )
			{
				callEvent0( event );
				return;
			}
			count++;
			Class<? extends AbstractEvent> newEventClass = event.getClass();
			if ( eventClass == null )
				eventClass = newEventClass;
			else if ( !eventClass.equals( newEventClass ) )
			{
				multiple = true;
				eventClass = getCommonSuperclass( newEventClass, eventClass ).asSubclass( AbstractEvent.class );
			}
			long start = System.nanoTime();
			// super.callEvent( event );
			totalTime += System.nanoTime() - start;
		}
		else
			callEvent0( event );
	}

	private void callEvent0( final E event ) throws EventException.Error
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
	 * Gets the total times this listener has been called
	 *
	 * Moved from TimedRegisteredListener
	 *
	 * @return Times this listener has been called
	 */
	public int getCount()
	{
		return count;
	}

	/**
	 * Gets the class of the events this listener handled. If it handled multiple classes of event, the closest shared
	 * superclass will be returned, such that for any event this listener has handled, <code>this.getEventClass().isAssignableFrom(event.getClass())</code> and no class <code>this.getEventClass().isAssignableFrom(clazz)
	 * && this.getEventClass() != clazz &&
	 * event.getClass().isAssignableFrom(clazz)</code> for all handled events.
	 *
	 * Moved from TimedRegisteredListener
	 *
	 * @return the event class handled by this RegisteredListener
	 */
	public Class<? extends AbstractEvent> getEventClass()
	{
		return eventClass;
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

	/**
	 * Gets the total time calls to this listener have taken
	 *
	 * Moved from TimedRegisteredListener
	 *
	 * @return Total time for all calls of this listener
	 */
	public long getTotalTime()
	{
		return totalTime;
	}

	/**
	 * Gets whether this listener has handled multiple events, such that for some two events, <code>eventA.getClass() != eventB.getClass()</code>.
	 *
	 * Moved from TimedRegisteredListener
	 *
	 * @return true if this listener has handled multiple events
	 */
	public boolean hasMultiple()
	{
		return multiple;
	}

	/**
	 * Resets the call count and total time for this listener
	 *
	 * Moved from TimedRegisteredListener
	 */
	public void reset()
	{
		count = 0;
		totalTime = 0;
	}

	public void setUseTimings( boolean useTimings )
	{
		this.useTimings = useTimings;
	}
}
