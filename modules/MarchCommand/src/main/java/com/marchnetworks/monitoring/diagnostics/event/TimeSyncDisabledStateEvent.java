package com.marchnetworks.monitoring.diagnostics.event;

import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.common.event.EventTypesEnum;
import com.marchnetworks.common.event.StateCacheable;

public class TimeSyncDisabledStateEvent implements StateCacheable
{
	public EventNotification getNotificationInfo()
	{
		return new Builder( getEventNotificationType() ).source( "" ).build();
	}

	public String getEventNotificationType()
	{
		return EventTypesEnum.TIME_SYNC_DISABLED.getFullPathEventName();
	}

	public Long getDeviceIdLong()
	{
		return StateCacheable.CES_EVENT;
	}

	public long getTimestamp()
	{
		return 0L;
	}

	public boolean isDeleteEvent()
	{
		return false;
	}
}

