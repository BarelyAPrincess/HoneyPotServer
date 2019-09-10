package com.marchnetworks.health.search;

import com.marchnetworks.command.api.alert.AlertCategoryEnum;
import com.marchnetworks.common.types.AlertSeverityEnum;
import com.marchnetworks.common.types.HistoricalAlertSearchTimeFieldEnum;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlElement;

public class AlertSearchQuery
{
	private AlertSeverityEnum[] severities;
	private AlertCategoryEnum[] categories;
	private HistoricalAlertSearchTimeFieldEnum timeField;
	private long startTime;
	private long stopTime;
	private long lastPeriod;

	public String toString()
	{
		return String.format( "AlertSearchQuery [severities=%s, categories=%s, userState=%s, timeField=%s, startTime=%s, stopTime=%s, lastPeriod=%s, useTimePeriod=%s]", new Object[] {Arrays.toString( severities ), Arrays.toString( categories ), timeField, Long.valueOf( startTime ), Long.valueOf( stopTime ), Long.valueOf( lastPeriod ), Boolean.valueOf( useTimePeriod )} );
	}

	private boolean useTimePeriod = false;

	public AlertCategoryEnum[] getCategories()
	{
		return categories;
	}

	public void setCategories( AlertCategoryEnum[] categories )
	{
		this.categories = categories;
	}

	public void setUseTimePeriod( boolean last )
	{
		useTimePeriod = last;
	}

	public boolean getUseTimePeriod()
	{
		return useTimePeriod;
	}

	public void setLastPeriod( long lastPeriod )
	{
		this.lastPeriod = lastPeriod;
	}

	public long getLastPeriod()
	{
		return lastPeriod;
	}

	public AlertSeverityEnum[] getSeverities()
	{
		return severities;
	}

	public void setSeverities( AlertSeverityEnum[] severities )
	{
		this.severities = severities;
	}

	public long getStartTime()
	{
		return startTime;
	}

	public void setStartTime( long startTime )
	{
		this.startTime = startTime;
	}

	public long getStopTime()
	{
		return stopTime;
	}

	public void setStopTime( long stopTime )
	{
		this.stopTime = stopTime;
	}

	@XmlElement( required = true )
	public HistoricalAlertSearchTimeFieldEnum getTimeField()
	{
		return timeField;
	}

	public void setTimeField( HistoricalAlertSearchTimeFieldEnum timeField )
	{
		this.timeField = timeField;
	}
}
