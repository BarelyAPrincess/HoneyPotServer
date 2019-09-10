package com.marchnetworks.management.instrumentation.events;

public class DeviceChannelRemovedEvent extends AbstractDeviceEvent
{
	private String channelId;

	public DeviceChannelRemovedEvent( String deviceId, String channelId )
	{
		super( DeviceChannelRemovedEvent.class.getName(), deviceId );
		this.channelId = channelId;
	}

	public String getChannelId()
	{
		return channelId;
	}
}

