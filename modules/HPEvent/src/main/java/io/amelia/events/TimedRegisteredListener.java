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

public class TimedRegisteredListener extends RegisteredListener
{
	private static Class<?> getCommonSuperclass( Class<?> class1, Class<?> class2 )
	{
		while ( !class1.isAssignableFrom( class2 ) )
			class1 = class1.getSuperclass();
		return class1;
	}

	private int count;
	private Class<? extends AbstractEvent> eventClass;
	private boolean multiple = false;
	private long totalTime;

	public TimedRegisteredListener( final Listener pluginListener, final EventExecutor eventExecutor, final EventPriority eventPriority, final RegistrarBase context, final boolean listenCancelled )
	{
		super( pluginListener, eventExecutor, eventPriority, context, listenCancelled );
	}

	@Override
	public void callEvent( AbstractEvent event ) throws EventException
	{
		if ( event.isAsynchronous() )
		{
			super.callEvent( event );
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
		super.callEvent( event );
		totalTime += System.nanoTime() - start;
	}

	/**
	 * Gets the total times this listener has been called
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
	 * @return the event class handled by this RegisteredListener
	 */
	public Class<? extends AbstractEvent> getEventClass()
	{
		return eventClass;
	}

	/**
	 * Gets the total time calls to this listener have taken
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
	 * @return true if this listener has handled multiple events
	 */
	public boolean hasMultiple()
	{
		return multiple;
	}

	/**
	 * Resets the call count and total time for this listener
	 */
	public void reset()
	{
		count = 0;
		totalTime = 0;
	}
}
