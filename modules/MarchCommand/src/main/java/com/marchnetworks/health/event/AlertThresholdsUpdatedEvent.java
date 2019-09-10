package com.marchnetworks.health.event;

import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.command.api.event.Notifiable;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.common.event.EventTypesEnum;

public class AlertThresholdsUpdatedEvent extends Event implements Notifiable
{
	public AlertThresholdsUpdatedEvent()
	{
		super( AlertThresholdsUpdatedEvent.class.getName() );
	}

	public AlertThresholdsUpdatedEvent( String type )
	{
		super( type );
	}

	public EventNotification getNotificationInfo()
	{
		return new Builder( getEventNotificationType() ).build();
	}

	public String getEventNotificationType()
	{
		return EventTypesEnum.HEALTH_THRESHOLD_UPDATED.getFullPathEventName();
	}
}
