package com.marchnetworks.command.api.event;

public abstract interface Notifiable
{
	public abstract EventNotification getNotificationInfo();

	public abstract String getEventNotificationType();
}
