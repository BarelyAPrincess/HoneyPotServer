package com.marchnetworks.management.instrumentation.events;

public class DeviceChannelsMaxEvent extends AbstractDeviceEvent
{
	protected int m_MaxChannels;

	public DeviceChannelsMaxEvent( String deviceId, int max )
	{
		super( DeviceChannelsMaxEvent.class.getName(), deviceId );
		m_MaxChannels = max;
	}

	public int getChannelsMax()
	{
		return m_MaxChannels;
	}
}

