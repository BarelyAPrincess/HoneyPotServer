package com.marchnetworks.command.api.event;

import java.util.List;

public abstract interface EventListener
{
	public abstract void processEvents( List<EventNotification> paramList );
}
