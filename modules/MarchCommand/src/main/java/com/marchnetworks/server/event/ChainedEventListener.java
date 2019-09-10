package com.marchnetworks.server.event;

import com.marchnetworks.common.event.Event;

import java.util.List;

public abstract interface ChainedEventListener
{
	public abstract void processChain( Event paramEvent, List<EventListener> paramList );
}

