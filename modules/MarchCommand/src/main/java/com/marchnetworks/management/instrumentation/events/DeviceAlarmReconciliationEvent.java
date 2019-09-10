package com.marchnetworks.management.instrumentation.events;

public class DeviceAlarmReconciliationEvent extends AbstractDeviceEvent
{
	public DeviceAlarmReconciliationEvent( String deviceId )
	{
		super( DeviceAlarmReconciliationEvent.class.getName(), deviceId );
	}
}

