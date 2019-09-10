package com.marchnetworks.command.common.schedule.data;

import javax.xml.bind.annotation.XmlElement;

public class DaySchedule
{
	private Integer dayOfWeek;
	private Integer startTime;
	private Integer endTime;

	@XmlElement( required = true )
	public Integer getDayOfWeek()
	{
		return dayOfWeek;
	}

	public void setDayOfWeek( Integer dayOfWeek )
	{
		this.dayOfWeek = dayOfWeek;
	}

	@XmlElement( required = true )
	public Integer getStartTime()
	{
		return startTime;
	}

	public void setStartTime( Integer startTime )
	{
		this.startTime = startTime;
	}

	@XmlElement( required = true )
	public Integer getEndTime()
	{
		return endTime;
	}

	public void setEndTime( Integer endTime )
	{
		this.endTime = endTime;
	}

	public String toString()
	{
		return String.format( "DaySchedule [dayOfWeek=%s, startTime=%s, endTime=%s]", new Object[] {dayOfWeek, startTime, endTime} );
	}
}
