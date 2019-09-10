package com.marchnetworks.management.instrumentation.events;

public class DeviceSetAlertConfigEvent extends AbstractDeviceEvent
{
	public DeviceSetAlertConfigEvent( String deviceId )
	{
		super( DeviceSetAlertConfigEvent.class.getName(), deviceId );
	}
}

