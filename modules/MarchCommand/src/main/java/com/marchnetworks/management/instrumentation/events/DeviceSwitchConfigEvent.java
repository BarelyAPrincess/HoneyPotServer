package com.marchnetworks.management.instrumentation.events;

public class DeviceSwitchConfigEvent extends DeviceSwitchEvent
{
	public DeviceSwitchConfigEvent( String deviceId )
	{
		super( DeviceSwitchConfigEvent.class.getName(), deviceId, DeviceOutputEventType.OUTPUT_CONFIG, null, null, null );
	}
}

