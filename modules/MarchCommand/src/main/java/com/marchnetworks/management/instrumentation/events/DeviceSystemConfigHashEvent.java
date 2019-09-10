package com.marchnetworks.management.instrumentation.events;

public class DeviceSystemConfigHashEvent extends AbstractDeviceConfigurationEvent
{
	public DeviceSystemConfigHashEvent( String deviceId, String taskId, String reason )
	{
		super( DeviceSystemConfigHashEvent.class.getName(), deviceId, DeviceConfigurationEventType.CONFIG_CHANGED, taskId, reason );
	}
}

