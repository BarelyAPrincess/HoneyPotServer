package com.marchnetworks.management.instrumentation.events;

public class DeviceAlarmClosuresDispatchEvent extends AbstractDeviceEvent
{
	public DeviceAlarmClosuresDispatchEvent( String deviceId )
	{
		super( DeviceAlarmClosuresDispatchEvent.class.getName(), deviceId );
	}
}

