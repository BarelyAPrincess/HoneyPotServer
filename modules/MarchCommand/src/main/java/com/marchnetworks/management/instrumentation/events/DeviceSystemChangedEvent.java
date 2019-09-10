package com.marchnetworks.management.instrumentation.events;

public class DeviceSystemChangedEvent extends AbstractDeviceEvent
{
	public DeviceSystemChangedEvent( String deviceId )
	{
		super( DeviceSystemChangedEvent.class.getName(), deviceId );
	}
}

