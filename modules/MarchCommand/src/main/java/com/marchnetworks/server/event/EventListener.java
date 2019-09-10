package com.marchnetworks.server.event;

import com.marchnetworks.common.event.Event;

public abstract interface EventListener
{
	public abstract void process( Event paramEvent );

	public abstract String getListenerName();
}

