package com.marchnetworks.management.firmware.data;

import javax.xml.bind.annotation.XmlElement;

public class Firmware
{
	private String deviceId;
	private String firmwareId;
	private String optParams;
	private Long schedulerId = null;
	private UpdateTypeEnum updateType;
	private UpdateStateEnum updateState;

	public String getDeviceId()
	{
		return deviceId;
	}

	public void setDeviceId( String id )
	{
		deviceId = id;
	}

	public String getOptParams()
	{
		return optParams;
	}

	public void setOptParams( String optParams )
	{
		this.optParams = optParams;
	}

	public String getFirmwareId()
	{
		return firmwareId;
	}

	public void setFirmwareId( String firmwareId )
	{
		this.firmwareId = firmwareId;
	}

	@XmlElement( required = true, nillable = true )
	public Long getSchedulerId()
	{
		return schedulerId;
	}

	public void setSchedulerId( Long schedulerId )
	{
		this.schedulerId = schedulerId;
	}

	@XmlElement( required = true )
	public UpdateStateEnum getUpdateState()
	{
		return updateState;
	}

	public void setUpdateState( UpdateStateEnum updateState )
	{
		this.updateState = updateState;
	}

	@XmlElement( required = true )
	public UpdateTypeEnum getUpdateType()
	{
		return updateType;
	}

	public void setUpdateType( UpdateTypeEnum updateType )
	{
		this.updateType = updateType;
	}
}

