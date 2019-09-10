package com.marchnetworks.management.instrumentation.events;

public class GenericDeviceUpgradeEvent extends AbstractDeviceConfigurationEvent
{
	public GenericDeviceUpgradeEvent( String deviceId, DeviceConfigurationEventType deviceEventType, String taskId, String reason )
	{
		super( GenericDeviceUpgradeEvent.class.getName(), deviceId, deviceEventType, taskId, reason );
	}
}

