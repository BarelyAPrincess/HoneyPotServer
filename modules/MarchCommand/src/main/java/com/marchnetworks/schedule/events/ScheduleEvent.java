package com.marchnetworks.schedule.events;

import com.marchnetworks.command.api.event.AppNotifiable;
import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.command.api.event.Notifiable;
import com.marchnetworks.command.common.schedule.data.Schedule;
import com.marchnetworks.common.event.AbstractTerritoryAwareEvent;
import com.marchnetworks.common.event.EventTypesEnum;

import java.util.Set;

public class ScheduleEvent extends AbstractTerritoryAwareEvent implements Notifiable, AppNotifiable
{
	private Long scheduleId;
	private Schedule schedule;
	private ScheduleEventType scheduleEventType;
	private boolean isStart;

	public ScheduleEvent( Schedule schedule, ScheduleEventType scheduleEventType, Set<Long> territoryIds )
	{
		super( ScheduleEvent.class.getName(), territoryIds );
		scheduleId = schedule.getId();
		this.schedule = schedule;
		this.scheduleEventType = scheduleEventType;
	}

	public ScheduleEvent( Long scheduleId, ScheduleEventType scheduleEventType )
	{
		super( ScheduleEvent.class.getName() );
		this.scheduleEventType = scheduleEventType;
		this.scheduleId = scheduleId;
	}

	public ScheduleEvent( Long scheduleId, ScheduleEventType scheduleEventType, boolean isStart )
	{
		this( scheduleId, scheduleEventType );
		this.isStart = isStart;
	}

	public boolean isStart()
	{
		return isStart;
	}

	public EventNotification getNotificationInfo()
	{
		return new Builder( getEventNotificationType() ).source( scheduleId.toString() ).value( schedule ).build();
	}

	public String getEventNotificationType()
	{
		if ( scheduleEventType == ScheduleEventType.UPDATED )
			return EventTypesEnum.SCHEDULE_UPDATED.getFullPathEventName();
		if ( scheduleEventType == ScheduleEventType.DELETED )
		{
			return EventTypesEnum.SCHEDULE_DELETED.getFullPathEventName();
		}
		return EventTypesEnum.SCHEDULE_NOTIFICATION.getFullPathEventName();
	}

	public ScheduleEventType getScheduleEventType()
	{
		return scheduleEventType;
	}

	public Schedule getSchedule()
	{
		return schedule;
	}

	public Long getScheduleId()
	{
		return scheduleId;
	}
}

