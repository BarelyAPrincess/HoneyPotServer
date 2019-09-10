package com.marchnetworks.common.event;

import com.marchnetworks.command.api.event.Notifiable;

public abstract interface StateCacheable extends Notifiable
{
	public static final Long CES_EVENT = Long.valueOf( 0L );

	public abstract Long getDeviceIdLong();

	public abstract long getTimestamp();

	public abstract boolean isDeleteEvent();
}
