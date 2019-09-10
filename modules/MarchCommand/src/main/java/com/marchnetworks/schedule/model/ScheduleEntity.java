package com.marchnetworks.schedule.model;

import com.google.gson.reflect.TypeToken;
import com.marchnetworks.command.common.CollectionUtils;
import com.marchnetworks.command.common.schedule.data.DaySchedule;
import com.marchnetworks.command.common.schedule.data.Schedule;
import com.marchnetworks.common.serialization.CoreJsonSerializer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table( name = "SCHEDULES" )
public class ScheduleEntity
{
	@Id
	@GeneratedValue
	@Column( name = "ID" )
	private Long id;
	@Column( name = "NAME" )
	private String name;
	@Column( name = "INTERVALS", length = 4000 )
	private String intervalsString;
	@Column( name = "APP_ID" )
	private String appId;
	@Column( name = "SCHEDULE_GROUP" )
	private String group;
	@Column( name = "SYSTEM_ROOTS", length = 2000 )
	private String systemRoots;
	@Column( name = "LOGICAL_ROOTS", length = 2000 )
	private String logicalRoots;

	public List<DaySchedule> getIntervals()
	{
		if ( intervalsString == null )
			return null;

		return CoreJsonSerializer.collectionFromJson( intervalsString, new TypeToken<ArrayList>()
		{
		} );
	}

	public void setIntervals( List<DaySchedule> intervals )
	{
		if ( ( intervals == null ) || ( intervals.isEmpty() ) )
		{
			intervalsString = null;
		}
		else
		{
			intervalsString = CoreJsonSerializer.toJson( intervals );
		}
	}

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public Schedule toDataObject()
	{
		Schedule target = new Schedule();

		target.setId( id );
		target.setName( name );
		target.setIntervals( getIntervals() );
		target.setAppId( appId );
		target.setGroup( group );
		target.setSystemRoots( getSystemRoots() );
		target.setLogicalRoots( getLogicalRoots() );

		return target;
	}

	public void readFromDataObject( Schedule origin )
	{
		setName( origin.getName() );
		setIntervals( origin.getIntervals() );
		setAppId( origin.getAppId() );
		setGroup( origin.getGroup() );
		Set<Long> systemRoots = origin.getSystemRoots();
		Set<Long> logicalRoots = origin.getLogicalRoots();
		if ( systemRoots != null )
		{
			setSystemRoots( systemRoots );
		}
		if ( logicalRoots != null )
		{
			setLogicalRoots( logicalRoots );
		}
	}

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

	public Set<Long> getSystemRoots()
	{
		if ( systemRoots != null )
		{
			Set<String> stringSet = ( Set ) CoreJsonSerializer.collectionFromJson( systemRoots, new TypeToken()
			{
			} );
			return CollectionUtils.convertStringToLongSet( stringSet );
		}
		return new HashSet( 0 );
	}

	public void setSystemRoots( Set<Long> systemIdSet )
	{
		systemRoots = null;
		if ( ( systemIdSet != null ) && ( !systemIdSet.isEmpty() ) )
		{
			Set<String> stringSet = CollectionUtils.convertToStringSet( systemIdSet );
			systemRoots = CoreJsonSerializer.toJson( stringSet );
		}
	}

	public Set<Long> getLogicalRoots()
	{
		if ( logicalRoots != null )
		{
			Set<String> stringSet = ( Set ) CoreJsonSerializer.collectionFromJson( logicalRoots, new TypeToken()
			{
			} );
			return CollectionUtils.convertStringToLongSet( stringSet );
		}
		return new HashSet( 0 );
	}

	public void setLogicalRoots( Set<Long> logicalIdSet )
	{
		logicalRoots = null;
		if ( ( logicalIdSet != null ) && ( !logicalIdSet.isEmpty() ) )
		{
			Set<String> stringSet = CollectionUtils.convertToStringSet( logicalIdSet );
			logicalRoots = CoreJsonSerializer.toJson( stringSet );
		}
	}
}

