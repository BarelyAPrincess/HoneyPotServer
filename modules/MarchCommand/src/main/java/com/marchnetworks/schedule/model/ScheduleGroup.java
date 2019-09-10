package com.marchnetworks.schedule.model;

public enum ScheduleGroup
{
	FIRMWARE_UPDATE( true ),
	OPERATIONS_AUDIT( false );

	private boolean needsNotifications;

	private ScheduleGroup( boolean needsNotifications )
	{
		this.needsNotifications = needsNotifications;
	}

	public boolean getNeedsNotifications()
	{
		return needsNotifications;
	}
}

