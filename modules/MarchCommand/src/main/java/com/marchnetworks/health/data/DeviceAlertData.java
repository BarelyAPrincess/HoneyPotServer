package com.marchnetworks.health.data;

import com.marchnetworks.command.api.alert.AlertCategoryEnum;
import com.marchnetworks.common.types.AlertSeverityEnum;
import com.marchnetworks.common.types.AlertUserStateEnum;

public class DeviceAlertData extends AlertData
{
	private String deviceId;
	private String channelName;
	private String deviceAlertId;
	private int duration;
	private int frequency;

	public String getDeviceAlertId()
	{
		return deviceAlertId;
	}

	public void setDeviceAlertId( String deviceAlertId )
	{
		this.deviceAlertId = deviceAlertId;
	}

	public DeviceAlertData()
	{
	}

	public DeviceAlertData( String alertCode, long alertTime, long lastInstanceTime, long count, long alertResolvedTime, boolean deviceState, String sourceId, String sourceDesc, AlertSeverityEnum severity, AlertCategoryEnum category, String info, long id, AlertUserStateEnum userState, long closedTime, String deviceId, String channelName, String deviceAlertId, int thresholdDuration, int thresholdFrequency )
	{
		super( alertCode, alertTime, lastInstanceTime, count, alertResolvedTime, deviceState, sourceId, sourceDesc, severity, category, info, id, userState, closedTime );

		this.deviceId = deviceId;
		this.channelName = channelName;
		this.deviceAlertId = deviceAlertId;
		duration = thresholdDuration;
		frequency = thresholdFrequency;
	}

	public String getDeviceId()
	{
		return deviceId;
	}

	public void setDeviceId( String deviceId )
	{
		this.deviceId = deviceId;
	}

	public String getChannelName()
	{
		return channelName;
	}

	public void setChannelName( String channelName )
	{
		this.channelName = channelName;
	}

	public int getDuration()
	{
		return duration;
	}

	public int getFrequency()
	{
		return frequency;
	}

	public void setDuration( int duration )
	{
		this.duration = duration;
	}

	public void setFrequency( int frequency )
	{
		this.frequency = frequency;
	}
}
