package com.marchnetworks.management.user.events;

import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.common.event.EventTypesEnum;

public class UserLogoffEvent extends UserEvent implements com.marchnetworks.command.api.event.UserNotifiable
{
	public UserLogoffEvent( String userName )
	{
		super( UserEvent.class.getName(), userName );
	}

	public String getEventNotificationType()
	{
		return EventTypesEnum.USER_LOGOFF.getFullPathEventName();
	}

	public com.marchnetworks.command.api.event.EventNotification getNotificationInfo()
	{
		return new EventNotification.Builder( getEventNotificationType() ).source( getUserName() ).build();
	}

	public String getUser()
	{
		return super.getUserName();
	}
}

