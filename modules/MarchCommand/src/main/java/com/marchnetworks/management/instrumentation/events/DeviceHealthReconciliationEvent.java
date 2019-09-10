package com.marchnetworks.management.instrumentation.events;

public class DeviceHealthReconciliationEvent extends AbstractDeviceEvent
{
	public DeviceHealthReconciliationEvent( String deviceId )
	{
		super( DeviceHealthReconciliationEvent.class.getName(), deviceId );
	}
}

