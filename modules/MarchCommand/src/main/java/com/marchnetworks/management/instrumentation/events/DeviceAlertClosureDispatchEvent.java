package com.marchnetworks.management.instrumentation.events;

public class DeviceAlertClosureDispatchEvent extends AbstractDeviceEvent
{
	public DeviceAlertClosureDispatchEvent( String deviceId )
	{
		super( DeviceAlertClosureDispatchEvent.class.getName(), deviceId );
	}
}

