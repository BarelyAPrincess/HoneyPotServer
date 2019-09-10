package com.marchnetworks.management.instrumentation.events;

public class CompositeDeviceUpgradeEvent extends GenericDeviceUpgradeEvent
{
	public CompositeDeviceUpgradeEvent( String deviceId, DeviceConfigurationEventType deviceEventType, String taskId, String reason )
	{
		super( deviceId, deviceEventType, taskId, reason );
	}
}

