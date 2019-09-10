package com.marchnetworks.management.user.events;

import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;

public class UserUpdatedEvent extends UserEvent
{
	public UserUpdatedEvent( String userName )
	{
		super( UserEvent.class.getName(), userName );
	}

	public String getEventNotificationType()
	{
		return com.marchnetworks.common.event.EventTypesEnum.USER_UPDATED.getFullPathEventName();
	}

	public com.marchnetworks.command.api.event.EventNotification getNotificationInfo()
	{
		return new EventNotification.Builder( getEventNotificationType() ).source( getUserName() ).build();
	}
}

