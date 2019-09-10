package com.marchnetworks.monitoring.diagnostics;

import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.common.event.EventTypesEnum;
import com.marchnetworks.common.event.StateCacheable;

public class DatabaseSizeStateEvent implements StateCacheable
{
	protected int percentage;

	public DatabaseSizeStateEvent()
	{
	}

	public DatabaseSizeStateEvent( int percentage )
	{
		this.percentage = percentage;
	}

	public EventNotification getNotificationInfo()
	{
		Builder builder = new Builder( getEventNotificationType() ).source( "" ).value( Integer.valueOf( percentage ) );

		EventNotification en = builder.build();
		return en;
	}

	public String getEventNotificationType()
	{
		return EventTypesEnum.DATABASE_SIZE.getFullPathEventName();
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

