package com.marchnetworks.notification.events;

import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.common.event.EventTypesEnum;

public class NotificationEvent extends Event implements com.marchnetworks.command.api.event.AppNotifiable
{
	private Long notificationId;

	public NotificationEvent( Long notificationId )
	{
		super( NotificationEvent.class.getName() );
		this.notificationId = notificationId;
	}

	public com.marchnetworks.command.api.event.EventNotification getNotificationInfo()
	{
		return new EventNotification.Builder( getEventNotificationType() ).source( notificationId.toString() ).build();
	}

	public String getEventNotificationType()
	{
		return EventTypesEnum.NOTIFICATION_DELETED.getFullPathEventName();
	}

	public Long getNotificationId()
	{
		return notificationId;
	}
}

