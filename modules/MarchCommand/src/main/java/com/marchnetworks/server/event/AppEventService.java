package com.marchnetworks.server.event;

import com.marchnetworks.command.api.event.AppNotifiable;

import java.util.List;

public abstract interface AppEventService
{
	public abstract void processAppNotifiables( List<AppNotifiable> paramList );

	public abstract void processAppStopped( String paramString );

	public abstract boolean subscriptionExists( String paramString );
}

