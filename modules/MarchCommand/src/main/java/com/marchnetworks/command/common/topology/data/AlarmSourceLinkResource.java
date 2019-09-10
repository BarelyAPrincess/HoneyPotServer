package com.marchnetworks.command.common.topology.data;

import javax.xml.bind.annotation.XmlElement;

public class AlarmSourceLinkResource extends LinkResource
{
	private String alarmSourceId;
	private Long deviceResourceId;

	public String getAlarmSourceId()
	{
		return alarmSourceId;
	}

	public void setAlarmSourceId( String alarmSourceId )
	{
		this.alarmSourceId = alarmSourceId;
	}

	@XmlElement( required = true )
	public Long getDeviceResourceId()
	{
		return deviceResourceId;
	}

	public void setDeviceResourceId( Long deviceResourceId )
	{
		this.deviceResourceId = deviceResourceId;
	}
}
