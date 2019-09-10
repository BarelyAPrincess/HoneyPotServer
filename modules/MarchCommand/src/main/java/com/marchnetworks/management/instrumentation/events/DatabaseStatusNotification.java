package com.marchnetworks.management.instrumentation.events;

import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.command.api.event.Notifiable;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.common.event.EventTypesEnum;

public class DatabaseStatusNotification extends Event implements Notifiable
{
	private boolean connected;

	public DatabaseStatusNotification( boolean connected )
	{
		super( DatabaseStatusNotification.class.getName() );
		this.connected = connected;
	}

	public EventNotification getNotificationInfo()
	{
		EventNotification eventNotification = new Builder( getEventNotificationType() ).build();

		return eventNotification;
	}

	public String getEventNotificationType()
	{
		if ( connected )
		{
			return EventTypesEnum.WATCHDOG_DATABASE_CONNECTED.getFullPathEventName();
		}
		return EventTypesEnum.WATCHDOG_DATABASE_DISCONNECTED.getFullPathEventName();
	}
}

