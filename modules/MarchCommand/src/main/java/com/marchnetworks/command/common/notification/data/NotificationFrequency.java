package com.marchnetworks.command.common.notification.data;

public enum NotificationFrequency
{
	HOURLY( "Hourly", 3600000L, "this hour" ),
	DAILY( "Daily", 86400000L, " today" ),
	WEEKLY( "Weekly", 604800000L, "this week" );

	private String displayName;
	private String displayPeriod;
	private long timeWindow;

	private NotificationFrequency( String displayName, long timeWindow, String displayPeriod )
	{
		this.displayName = displayName;
		this.timeWindow = timeWindow;
		this.displayPeriod = displayPeriod;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public long getTimeWindow()
	{
		return timeWindow;
	}

	public String getDisplayPeriod()
	{
		return displayPeriod;
	}

	public void setDisplayPeriod( String displayPeriod )
	{
		this.displayPeriod = displayPeriod;
	}
}
