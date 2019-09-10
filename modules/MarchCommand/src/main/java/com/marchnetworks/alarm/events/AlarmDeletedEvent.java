package com.marchnetworks.alarm.events;

import com.marchnetworks.command.api.event.AppNotifiable;
import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.common.event.EventTypesEnum;

public class AlarmDeletedEvent extends Event implements AppNotifiable
{
	private Long resourceId;

	public AlarmDeletedEvent( Long resourceId )
	{
		this.resourceId = resourceId;
	}

	public String getEventNotificationType()
	{
		return EventTypesEnum.ALARM_REMOVED.getFullPathEventName();
	}

	public EventNotification getNotificationInfo()
	{
		Builder builder = new Builder( getEventNotificationType() ).value( resourceId );

		EventNotification en = builder.build();
		return en;
	}
}
