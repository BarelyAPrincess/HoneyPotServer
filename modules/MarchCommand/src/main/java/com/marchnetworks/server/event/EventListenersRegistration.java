package com.marchnetworks.server.event;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EventListenersRegistration
{
	private HashMap<Object, LinkedList<EventListener>> m_ListenerMap = new HashMap<>();
	private int poolSize = 1;
	private EventProcessingType processingType = EventProcessingType.SYNCHRONOUS;

	public EventListenersRegistration( Object a_EventType, EventListener a_Listener )
	{
		LinkedList<EventListener> list = new LinkedList();
		list.add( a_Listener );
		m_ListenerMap.put( a_EventType, list );
	}

	public EventListenersRegistration( Object a_EventType, List a_Listeners )
	{
		LinkedList<EventListener> list = new LinkedList();
		for ( Object listener : a_Listeners )
		{
			list.add( ( EventListener ) listener );
		}
		m_ListenerMap.put( a_EventType, list );
	}

	public EventListenersRegistration( List a_EventTypes, EventListener a_Listener )
	{
		LinkedList<EventListener> list = new LinkedList();
		list.add( a_Listener );

		for ( Object eventType : a_EventTypes )
		{
			m_ListenerMap.put( eventType, list );
		}
	}

	public EventListenersRegistration( Map a_EventTypesAndListeners )
	{
		for ( Object eventType : a_EventTypesAndListeners.keySet() )
		{
			LinkedList<EventListener> list = new LinkedList();
			for ( Object listener : ( List ) a_EventTypesAndListeners.get( eventType ) )
			{
				list.add( ( EventListener ) listener );
			}

			m_ListenerMap.put( eventType, list );
		}
	}

	public HashMap<Object, LinkedList<EventListener>> getListenerMap()
	{
		return m_ListenerMap;
	}

	public int getPoolSize()
	{
		return poolSize;
	}

	public EventProcessingType getProcessingType()
	{
		return processingType;
	}

	public void setPoolSize( int poolSize )
	{
		this.poolSize = poolSize;
	}

	public void setProcessingType( EventProcessingType processingType )
	{
		this.processingType = processingType;
	}
}

