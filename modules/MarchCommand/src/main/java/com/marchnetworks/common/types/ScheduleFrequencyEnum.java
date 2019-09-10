package com.marchnetworks.common.types;

public enum ScheduleFrequencyEnum
{
	HOURLY( "Hourly", 3600000L ),
	DAILY( "Daily", 86400000L ),
	WEEKLY( "Weekly", 604800000L );

	private String frequencyString;
	private long timeWindowInMillis;

	private ScheduleFrequencyEnum( String frequencyString, long timeWindowInMillis )
	{
		this.frequencyString = frequencyString;
		this.timeWindowInMillis = timeWindowInMillis;
	}

	public String getString()
	{
		return frequencyString;
	}

	public long getTimeWindow()
	{
		return timeWindowInMillis;
	}
}
