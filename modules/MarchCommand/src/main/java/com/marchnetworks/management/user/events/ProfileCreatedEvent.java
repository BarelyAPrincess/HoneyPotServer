package com.marchnetworks.management.user.events;

import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;

public class ProfileCreatedEvent extends ProfileEvent
{
	public ProfileCreatedEvent()
	{
		super( UserEvent.class.getName() );
	}

	public String getEventNotificationType()
	{
		return com.marchnetworks.common.event.EventTypesEnum.PROFILE_CREATED.getFullPathEventName();
	}

	public com.marchnetworks.command.api.event.EventNotification getNotificationInfo()
	{
		return new EventNotification.Builder( getEventNotificationType() ).source( getProfileId() ).build();
	}
}

