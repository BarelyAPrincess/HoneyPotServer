package com.marchnetworks.management.instrumentation.events;

public class GenericDeviceConfigurationEvent extends AbstractDeviceConfigurationEvent
{
	public GenericDeviceConfigurationEvent( String deviceId, DeviceConfigurationEventType deviceEventType, String taskId, String reason )
	{
		super( GenericDeviceConfigurationEvent.class.getName(), deviceId, deviceEventType, taskId, reason );
	}

	public GenericDeviceConfigurationEvent( String deviceId, DeviceConfigurationEventType deviceEventType, String taskId, String hash, String reason )
	{
		super( GenericDeviceConfigurationEvent.class.getName(), deviceId, deviceEventType, taskId, hash, reason );
	}

	public GenericDeviceConfigurationEvent( String deviceId, DeviceConfigurationEventType deviceEventType, String taskId, String reason, boolean deferred )
	{
		super( GenericDeviceConfigurationEvent.class.getName(), deviceId, deviceEventType, taskId, reason, deferred );
	}
}

