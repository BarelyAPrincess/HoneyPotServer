package com.marchnetworks.management.user.events;

import com.marchnetworks.command.api.event.EventNotification;

public class ProfileUpdatedEvent extends ProfileEvent
{
	public ProfileUpdatedEvent()
	{
		super( UserEvent.class.getName() );
	}

	public String getEventNotificationType()
	{
		return com.marchnetworks.common.event.EventTypesEnum.PROFILE_UPDATED.getFullPathEventName();
	}

	public com.marchnetworks.command.api.event.EventNotification getNotificationInfo()
	{
		return new EventNotification.Builder( getEventNotificationType() ).source( getProfileId() ).build();
	}
}

