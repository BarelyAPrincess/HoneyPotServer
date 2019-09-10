package com.marchnetworks.command.api.event;

public abstract interface AppNotifiable
{
	public abstract EventNotification getNotificationInfo();

	public abstract String getEventNotificationType();
}
