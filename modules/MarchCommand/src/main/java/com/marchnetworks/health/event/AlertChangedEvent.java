package com.marchnetworks.health.event;

import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.command.api.event.Notifiable;
import com.marchnetworks.common.event.AbstractTerritoryAwareEvent;
import com.marchnetworks.common.event.EventTypesEnum;

import java.util.Set;

public class AlertChangedEvent extends AbstractTerritoryAwareEvent implements Notifiable
{
	protected EventTypesEnum eventTypeEnum;
	protected String alertID;

	public AlertChangedEvent( String type, Set<Long> territoryInfo, EventTypesEnum eventType, String id )
	{
		super( type, territoryInfo );
		eventTypeEnum = eventType;
		alertID = id;
	}

	public String getEventNotificationType()
	{
		return eventTypeEnum.getFullPathEventName();
	}

	public EventNotification getNotificationInfo()
	{
		EventNotification en = new Builder( getEventNotificationType() ).source( alertID ).build();

		return en;
	}
}
