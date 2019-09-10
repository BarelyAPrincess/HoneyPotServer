package com.marchnetworks.management.firmware.data;

import java.util.List;

public class ChannelGroupFirmware
{
	private String parentDeviceId;
	private String firmwareId;
	private List<String> channelDeviceIDs = new java.util.ArrayList();

	public ChannelGroupFirmware( String parentDeviceId, String firmwareId )
	{
		this.parentDeviceId = parentDeviceId;
		this.firmwareId = firmwareId;
	}

	public String getParentDeviceId()
	{
		return parentDeviceId;
	}

	public void setParentDeviceId( String id )
	{
		parentDeviceId = id;
	}

	public String getFirmwareId()
	{
		return firmwareId;
	}

	public void addChannelDeviceID( String channelDeviceID )
	{
		channelDeviceIDs.add( channelDeviceID );
	}

	public List<String> getChannelDeviceIDs()
	{
		return channelDeviceIDs;
	}
}

