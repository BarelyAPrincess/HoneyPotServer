package com.marchnetworks.command.common.alarm.data;

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType( name = "alarmSource" )
public class AlarmSourceView
{
	private String id;
	private String deviceAlarmSourceId;
	private String deviceId;
	private String alarmType;
	private String name;
	private String[] associatedChannels;
	private AlarmState state;
	private AlarmExtendedState extendedState;

	public AlarmSourceView()
	{
	}

	public AlarmSourceView( String id, String deviceAlarmSourceId, String deviceId, String alarmType, String name, Collection<String> associatedChannels, AlarmState state, AlarmExtendedState extendedState )
	{
		this.id = id;
		this.deviceAlarmSourceId = deviceAlarmSourceId;
		this.deviceId = deviceId;
		this.alarmType = alarmType;
		this.name = name;
		this.associatedChannels = ( associatedChannels != null ? ( String[] ) associatedChannels.toArray( new String[associatedChannels.size()] ) : null );
		this.state = state;
		this.extendedState = extendedState;
	}

	public String getId()
	{
		return id;
	}

	public void setId( String id )
	{
		this.id = id;
	}

	public String getDeviceAlarmSourceId()
	{
		return deviceAlarmSourceId;
	}

	public void setDeviceAlarmSourceId( String deviceAlarmSourceId )
	{
		this.deviceAlarmSourceId = deviceAlarmSourceId;
	}

	public String getDeviceId()
	{
		return deviceId;
	}

	public void setDeviceId( String deviceId )
	{
		this.deviceId = deviceId;
	}

	public String getAlarmType()
	{
		return alarmType;
	}

	public void setAlarmType( String alarmType )
	{
		this.alarmType = alarmType;
	}

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public String[] getAssociatedChannels()
	{
		return associatedChannels;
	}

	public void setAssociatedChannels( String[] associatedChannels )
	{
		this.associatedChannels = associatedChannels;
	}

	@XmlElement( required = true )
	public AlarmState getState()
	{
		return state;
	}

	public void setState( AlarmState state )
	{
		this.state = state;
	}

	@XmlElement( required = true, nillable = true )
	public AlarmExtendedState getExtendedState()
	{
		return extendedState;
	}

	public void setExtendedState( AlarmExtendedState extendedState )
	{
		this.extendedState = extendedState;
	}
}

