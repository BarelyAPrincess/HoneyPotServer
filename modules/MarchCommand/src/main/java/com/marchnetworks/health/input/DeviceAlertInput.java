package com.marchnetworks.health.input;

import com.marchnetworks.command.api.alert.AlertCategoryEnum;
import com.marchnetworks.command.api.alert.AlertDefinitionEnum;

public class DeviceAlertInput extends AlertInput
{
	private String deviceId;
	private String alertId;
	private int thresholdDuration;
	private int thresholdFrequency;
	private int count = -1;

	public DeviceAlertInput( String deviceId, AlertDefinitionEnum definition, String sourceId, long alertTime, long lastTime, long resolvedTime, String info, String value, boolean deviceState )
	{
		this( deviceId, "", -1, definition.getPath(), definition.getCategory(), sourceId, alertTime, lastTime, resolvedTime, info, value, deviceState, 0, 0 );
	}

	public DeviceAlertInput( String deviceId, String alertId, int count, AlertDefinitionEnum definition, String sourceId, long alertTime, long lastTime, long resolvedTime, String info, String value, boolean deviceState, int thresholdDuration, int thresholdFrequency )
	{
		this( deviceId, alertId, count, definition.getPath(), definition.getCategory(), sourceId, alertTime, lastTime, resolvedTime, info, value, deviceState, thresholdDuration, thresholdFrequency );
	}

	public DeviceAlertInput( String deviceId, String alertId, int count, String alertCode, AlertCategoryEnum category, String sourceId, long alertTime, long lastTime, long resolvedTime, String info, String value, boolean deviceState, int thresholdDuration, int thresholdFrequency )
	{
		super( alertCode, category, sourceId, alertTime, lastTime, resolvedTime, info, value, deviceState );

		this.deviceId = deviceId;
		this.alertId = alertId;
		this.count = count;
		this.thresholdDuration = thresholdDuration;
		this.thresholdFrequency = thresholdFrequency;
	}

	public int getCount()
	{
		return count;
	}

	public void setCount( int count )
	{
		this.count = count;
	}

	public String getAlertId()
	{
		return alertId;
	}

	public void setAlertId( String alertId )
	{
		this.alertId = alertId;
	}

	public String getDeviceId()
	{
		return deviceId;
	}

	public void setDeviceId( String deviceId )
	{
		this.deviceId = deviceId;
	}

	public int getThresholdDuration()
	{
		return thresholdDuration;
	}

	public int getThresholdFrequency()
	{
		return thresholdFrequency;
	}
}
