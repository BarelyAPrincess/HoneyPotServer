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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.ListIterator;

/**
 * A list of event handlers, stored per-event.
 */
public class EventHandlers extends AbstractList<RegisteredListener>
{
	private static final List<EventHandlers> handlers = new ArrayList<>();

	/**
	 * Get a specific creator's registered listeners associated with this handler list
	 *
	 * @param source the source to get the listeners of
	 * @return the list of registered listeners
	 */
	public static ArrayList<RegisteredListener> getRegisteredListeners( Object source )
	{
		ArrayList<RegisteredListener> listeners = new ArrayList<>();
		synchronized ( handlers )
		{
			for ( EventHandlers handler : handlers )
				synchronized ( handler )
				{
					for ( List<RegisteredListener> list : handler.listeners.values() )
						for ( RegisteredListener listener : list )
							if ( listener.getContext().getSource().equals( source ) )
								listeners.add( listener );
				}
		}
		return listeners;
	}

	/**
	 * Unregister all listeners from all handler lists.
	 */
	public static void unregisterAll()
	{
		synchronized ( handlers )
		{
			for ( EventHandlers handler : handlers )
				synchronized ( handler )
				{
					for ( List<RegisteredListener> list : handler.listeners.values() )
						list.clear();
				}
		}
	}

	/**
	 * Unregister a specific creator's listeners from all handler lists.
	 *
	 * @param creator creator to unregister
	 */
	public static void unregisterAll( EventRegistrar creator )
	{
		synchronized ( handlers )
		{
			for ( EventHandlers handler : handlers )
				handler.unregister( creator );
		}
	}

	/**
	 * Unregister a specific listener from all handler lists.
	 *
	 * @param listener listener to unregister
	 */
	public static void unregisterAll( Listener listener )
	{
		synchronized ( handlers )
		{
			for ( EventHandlers handler : handlers )
				handler.unregister( listener );
		}
	}

	private final EnumMap<EventPriority, List<RegisteredListener>> listeners = new EnumMap<>( EventPriority.class );

	public EventHandlers()
	{
		for ( EventPriority o : EventPriority.values() )
			listeners.put( o, new ArrayList<>() );
	}

	@Override
	public RegisteredListener get( int index )
	{
		return getRegisteredListeners().get( index );
	}

	public List<RegisteredListener> getRegisteredListeners()
	{
		List<RegisteredListener> registeredListeners = new ArrayList<>();
		for ( List<RegisteredListener> listOfListeners : listeners.values() )
			registeredListeners.addAll( listOfListeners );
		return registeredListeners;
	}

	/**
	 * Register a new listener in this handler list
	 *
	 * @param listener listener to register
	 */
	public synchronized void register( RegisteredListener listener )
	{
		if ( listeners.get( listener.getPriority() ).contains( listener ) )
			throw new IllegalStateException( "This listener is already registered to priority " + listener.getPriority().toString() );
		listeners.get( listener.getPriority() ).add( listener );
	}

	/**
	 * Register a collection of new listeners in this handler list
	 *
	 * @param listeners listeners to register
	 */
	public void registerAll( Collection<RegisteredListener> listeners )
	{
		for ( RegisteredListener listener : listeners )
			register( listener );
	}

	@Override
	public int size()
	{
		return getRegisteredListeners().size();
	}

	/**
	 * Remove a specific listener from this handler
	 *
	 * @param listener listener to remove
	 */
	public synchronized void unregister( Listener listener )
	{
		for ( List<RegisteredListener> list : listeners.values() )
			for ( ListIterator<RegisteredListener> i = list.listIterator(); i.hasNext(); )
				if ( i.next().getListener().equals( listener ) )
					i.remove();
	}

	/**
	 * Remove a specific creator's listeners from this handler
	 *
	 * @param source creator to remove
	 */
	public synchronized void unregister( Object source )
	{
		for ( List<RegisteredListener> list : listeners.values() )
			for ( ListIterator<RegisteredListener> i = list.listIterator(); i.hasNext(); )
				if ( i.next().getContext().getSource().equals( source ) )
					i.remove();
	}

	/**
	 * Remove a listener from a specific order slot
	 *
	 * @param listener listener to remove
	 */
	public synchronized void unregister( RegisteredListener listener )
	{
		listeners.get( listener.getPriority() ).remove( listener );
	}
}
