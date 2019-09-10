package com.marchnetworks.management.instrumentation.events;

public class DeviceChannelChangedEvent extends AbstractDeviceEvent
{
	private String channelId;

	public DeviceChannelChangedEvent( String deviceId, String channelId )
	{
		super( DeviceChannelChangedEvent.class.getName(), deviceId );
		this.channelId = channelId;
	}

	public String getChannelId()
	{
		return channelId;
	}
}

