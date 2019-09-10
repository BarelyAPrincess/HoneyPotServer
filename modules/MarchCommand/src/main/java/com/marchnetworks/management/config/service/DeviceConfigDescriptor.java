package com.marchnetworks.management.config.service;

import com.marchnetworks.management.config.DeviceImageState;

import javax.xml.bind.annotation.XmlElement;

public class DeviceConfigDescriptor
{
	private String id;
	private DeviceImageState assignState;
	private String deviceID;
	private String imageID;
	private String firmwareVersion;
	private String deviceModel;
	private String deviceFamily;
	private String deviceSerial;

	public String getId()
	{
		return id;
	}

	public void setId( String id )
	{
		this.id = id;
	}

	@XmlElement( required = true )
	public DeviceImageState getAssignState()
	{
		return assignState;
	}

	public void setAssignState( DeviceImageState state )
	{
		assignState = state;
	}

	public void setDeviceID( String deviceID )
	{
		this.deviceID = deviceID;
	}

	public String getDeviceID()
	{
		return deviceID;
	}

	public void setImageID( String imageID )
	{
		this.imageID = imageID;
	}

	public String getImageID()
	{
		return imageID;
	}

	public String getFirmwareVersion()
	{
		return firmwareVersion;
	}

	public void setFirmwareVersion( String firmwareVersion )
	{
		this.firmwareVersion = firmwareVersion;
	}

	public String getDeviceModel()
	{
		return deviceModel;
	}

	public void setDeviceModel( String deviceModel )
	{
		this.deviceModel = deviceModel;
	}

	public String getDeviceFamily()
	{
		return deviceFamily;
	}

	public void setDeviceFamily( String deviceFamily )
	{
		this.deviceFamily = deviceFamily;
	}

	public String getDeviceSerial()
	{
		return deviceSerial;
	}

	public void setDeviceSerial( String deviceSerial )
	{
		this.deviceSerial = deviceSerial;
	}
}
