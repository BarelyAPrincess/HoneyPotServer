package com.marchnetworks.management.instrumentation.events;

public class DeviceStateReconciliationEvent extends AbstractDeviceEvent
{
	public DeviceStateReconciliationEvent( String deviceId )
	{
		super( DeviceStateReconciliationEvent.class.getName(), deviceId );
	}
}

