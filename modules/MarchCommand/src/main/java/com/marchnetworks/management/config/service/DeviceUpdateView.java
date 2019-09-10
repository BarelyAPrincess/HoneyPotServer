package com.marchnetworks.management.config.service;

import com.marchnetworks.management.config.DeviceUpdateState;

public class DeviceUpdateView
{
	private long id;
	private String deviceId;
	private String firmwareId;
	private Long schedulerId;
	private com.marchnetworks.management.config.DeviceUpdateType updateType;
	private DeviceUpdateState updateState;
	private long lastUpdateTime;

	public long getId()
	{
		return id;
	}

	public void setId( long id )
	{
		this.id = id;
	}

	public String getDeviceId()
	{
		return deviceId;
	}

	public void setDeviceId( String id )
	{
		deviceId = id;
	}

	public String getFirmwareId()
	{
		return firmwareId;
	}

	public void setFirmwareId( String id )
	{
		firmwareId = id;
	}

	public Long getSchedulerId()
	{
		return schedulerId;
	}

	public void setSchedulerId( Long id )
	{
		schedulerId = id;
	}

	public com.marchnetworks.management.config.DeviceUpdateType getUpdateType()
	{
		return updateType;
	}

	public void setUpdateType( com.marchnetworks.management.config.DeviceUpdateType state )
	{
		updateType = state;
	}

	public DeviceUpdateState getUpdateState()
	{
		return updateState;
	}

	public void setUpdateState( DeviceUpdateState state )
	{
		updateState = state;
	}

	public long getLastUpdateTime()
	{
		return lastUpdateTime;
	}

	public void setLastUpdateTime( long time )
	{
		lastUpdateTime = time;
	}
}
