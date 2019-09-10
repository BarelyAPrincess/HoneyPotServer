package com.marchnetworks.command.common.schedule.data;

import com.marchnetworks.command.common.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

public class Schedule
{
	private Long id;
	private String name;
	private List<DaySchedule> intervals;
	private String appId;
	private String group;
	private Set<Long> systemRoots;
	private Set<Long> logicalRoots;

	public void addDaySchedule( DaySchedule daySchedule )
	{
		if ( intervals == null )
		{
			intervals = new ArrayList();
		}
		intervals.add( daySchedule );
	}

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public List<DaySchedule> getIntervals()
	{
		return intervals;
	}

	public void setIntervals( List<DaySchedule> intervals )
	{
		this.intervals = intervals;
	}

	@XmlElement( required = true, nillable = true )
	public Long getId()
	{
		return id;
	}

	public void setId( Long id )
	{
		this.id = id;
	}

	public String getAppId()
	{
		return appId;
	}

	public void setAppId( String appId )
	{
		this.appId = appId;
	}

	public String getGroup()
	{
		return group;
	}

	public void setGroup( String group )
	{
		this.group = group;
	}

	@XmlTransient
	public Set<Long> getSystemRoots()
	{
		return systemRoots;
	}

	public void setSystemRoots( Set<Long> systemRoots )
	{
		this.systemRoots = systemRoots;
	}

	@XmlTransient
	public Set<Long> getLogicalRoots()
	{
		return logicalRoots;
	}

	public void setLogicalRoots( Set<Long> logicalRoots )
	{
		this.logicalRoots = logicalRoots;
	}

	public String toString()
	{
		return String.format( "Schedule [id=%s, name=%s, intervals=%s, appId=%s, group=%s, user=%s]", new Object[] {id, name, CollectionUtils.collectionToString( intervals, "," ), appId, group} );
	}
}
