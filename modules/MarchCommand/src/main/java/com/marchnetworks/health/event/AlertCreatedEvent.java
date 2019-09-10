package com.marchnetworks.health.event;

import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.health.data.AlertData;

import java.util.Set;

public class AlertCreatedEvent extends AlertChangedEvent
{
	public AlertData alert;

	public AlertCreatedEvent( String type, Set<Long> territoryInfo, AlertData alert )
	{
		super( type, territoryInfo, com.marchnetworks.common.event.EventTypesEnum.HEALTH_CREATED, String.valueOf( alert.getId() ) );
		this.alert = alert;
	}

	public EventNotification getNotificationInfo()
	{
		EventNotification en = new Builder( getEventNotificationType() ).source( alertID ).value( alert ).build();

		return en;
	}
}
