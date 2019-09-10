package com.marchnetworks.management.config.events;

import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.common.event.EventTypesEnum;

public class ConfigurationAddedEvent extends ConfigurationEvent
{
	public ConfigurationAddedEvent( String configurationId )
	{
		super( ConfigurationEvent.class.getName() );
		setConfigurationId( configurationId );
	}

	public String getEventNotificationType()
	{
		return EventTypesEnum.CONFIGURATION_ADDED.getFullPathEventName();
	}

	public EventNotification getNotificationInfo()
	{
		return new Builder( getEventNotificationType() ).source( getConfigurationId() ).build();
	}
}
