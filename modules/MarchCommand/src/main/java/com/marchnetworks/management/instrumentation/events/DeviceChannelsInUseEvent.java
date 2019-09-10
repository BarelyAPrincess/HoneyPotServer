package com.marchnetworks.management.instrumentation.events;

public class DeviceChannelsInUseEvent extends AbstractDeviceEvent
{
	protected int m_InUse;

	public DeviceChannelsInUseEvent( String deviceId, int inuse )
	{
		super( DeviceChannelsInUseEvent.class.getName(), deviceId );
		m_InUse = inuse;
	}

	public int getInUse()
	{
		return m_InUse;
	}
}

