package com.marchnetworks.management.user.events;

import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.common.event.EventTypesEnum;

public class UserRemovedEvent extends UserEvent implements com.marchnetworks.command.api.event.AppNotifiable
{
	public UserRemovedEvent( String userName )
	{
		super( UserEvent.class.getName(), userName );
	}

	public String getEventNotificationType()
	{
		return EventTypesEnum.USER_REMOVED.getFullPathEventName();
	}

	public com.marchnetworks.command.api.event.EventNotification getNotificationInfo()
	{
		return new EventNotification.Builder( getEventNotificationType() ).source( getUserName() ).build();
	}
}

