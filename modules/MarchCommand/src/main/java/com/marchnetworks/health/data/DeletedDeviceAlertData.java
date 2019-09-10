package com.marchnetworks.health.data;

import com.marchnetworks.command.api.alert.AlertCategoryEnum;
import com.marchnetworks.common.device.DeletedDevice;
import com.marchnetworks.common.types.AlertSeverityEnum;
import com.marchnetworks.common.types.AlertUserStateEnum;

public class DeletedDeviceAlertData extends AlertData
{
	private String path;
	private String deviceName;
	private String deviceAddress;
	private String manufacturer;
	private String manufacturerID;
	private String familyID;
	private String modelID;
	private String channelName;
	private int duration;
	private int frequency;

	public DeletedDeviceAlertData()
	{
	}

	public DeletedDeviceAlertData( String alertCode, long alertTime, long lastInstanceTime, long count, long alertResolvedTime, boolean deviceState, String sourceId, String sourceDesc, AlertSeverityEnum severity, AlertCategoryEnum category, String info, long id, AlertUserStateEnum userState, long closedTime, String channelName, DeletedDevice device, int thresholdDuration, int thresholdFrequency )
	{
		super( alertCode, alertTime, lastInstanceTime, count, alertResolvedTime, deviceState, sourceId, sourceDesc, severity, category, info, id, userState, closedTime );

		path = device.getPathString();
		deviceName = device.getName();
		deviceAddress = device.getAddress();
		manufacturer = device.getManufacturerName();
		manufacturerID = device.getManufacturer();
		familyID = device.getFamily();
		modelID = device.getModel();
		this.channelName = channelName;
		duration = thresholdDuration;
		frequency = thresholdFrequency;
	}

	public String getPath()
	{
		return path;
	}

	public void setPath( String path )
	{
		this.path = path;
	}

	public String getDeviceName()
	{
		return deviceName;
	}

	public void setDeviceName( String deviceName )
	{
		this.deviceName = deviceName;
	}

	public String getDeviceAddress()
	{
		return deviceAddress;
	}

	public void setDeviceAddress( String deviceAddress )
	{
		this.deviceAddress = deviceAddress;
	}

	public String getManufacturer()
	{
		return manufacturer;
	}

	public void setManufacturer( String manufacturer )
	{
		this.manufacturer = manufacturer;
	}

	public String getManufacturerID()
	{
		return manufacturerID;
	}

	public void setManufacturerID( String manufacturerID )
	{
		this.manufacturerID = manufacturerID;
	}

	public String getFamilyID()
	{
		return familyID;
	}

	public void setFamilyID( String familyID )
	{
		this.familyID = familyID;
	}

	public String getModelID()
	{
		return modelID;
	}

	public void setModelID( String modelID )
	{
		this.modelID = modelID;
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
