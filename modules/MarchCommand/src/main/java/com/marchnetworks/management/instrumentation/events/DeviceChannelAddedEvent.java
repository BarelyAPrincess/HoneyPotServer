package com.marchnetworks.management.instrumentation.events;

public class DeviceChannelAddedEvent extends AbstractDeviceEvent
{
	private String channelId;

	public DeviceChannelAddedEvent( String deviceId, String channelId )
	{
		super( DeviceChannelAddedEvent.class.getName(), deviceId );
		this.channelId = channelId;
	}

	public String getChannelId()
	{
		return channelId;
	}
}

