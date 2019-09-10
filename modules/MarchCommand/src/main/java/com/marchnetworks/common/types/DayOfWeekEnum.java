package com.marchnetworks.common.types;

public enum DayOfWeekEnum
{
	Monday( "Mon" ),
	Tuesday( "Tue" ),
	Wednesday( "Wed" ),
	Thursday( "Thu" ),
	Friday( "Fri" ),
	Saturday( "Sat" ),
	Sunday( "Sun" ),
	NA( "Na" ),
	Everyday( "Everyday" ),
	Weekdays( "Weekdays" ),
	Weekend( "Weekend" );

	private String text;

	private DayOfWeekEnum( String text )
	{
		this.text = text;
	}

	public String getString()
	{
		return text;
	}
}
