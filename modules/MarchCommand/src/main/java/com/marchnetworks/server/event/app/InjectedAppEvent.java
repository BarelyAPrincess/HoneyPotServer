package com.marchnetworks.server.event.app;

import com.marchnetworks.command.api.event.AppNotifiable;
import com.marchnetworks.command.api.event.EventNotification;

public class InjectedAppEvent implements AppNotifiable
{
	private EventNotification notification;

	public InjectedAppEvent( EventNotification notification )
	{
		this.notification = notification;
	}

	public EventNotification getNotificationInfo()
	{
		return notification;
	}

	public String getEventNotificationType()
	{
		return notification.getPath();
	}
}

