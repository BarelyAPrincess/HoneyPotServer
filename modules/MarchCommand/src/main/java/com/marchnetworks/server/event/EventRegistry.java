package com.marchnetworks.server.event;

import com.marchnetworks.command.api.event.Notifiable;
import com.marchnetworks.common.event.Event;

import java.util.List;

public abstract interface EventRegistry
{
	public abstract void sendEventAfterTransactionCommits( Event paramEvent );

	public abstract void send( Event paramEvent );

	public abstract void send( List<Event> paramList );

	public abstract void sendNotifiable( Notifiable paramNotifiable );

	public abstract void sendNotifiable( List<Notifiable> paramList );
}

