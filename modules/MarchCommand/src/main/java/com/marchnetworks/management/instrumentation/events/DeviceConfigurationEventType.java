package com.marchnetworks.management.instrumentation.events;

public enum DeviceConfigurationEventType
{
	UPGRADE_WAITING_ACCEPT,
	UPGRADE_ACCEPTED,
	UPGRADE_FAILED,
	SCHEDULE_CONFIG_APPLY,
	CONFIG_APPLIED,
	CONFIG_APPLIED_INTERNAL,
	CONFIG_FAILED,
	CONFIG_CHANGED,
	CHANNEL_CHANGED,
	SYSTEM_CHANGED,
	CONFIG_PENDING,
	CONFIG_FAILED_FROM_DEVICE,
	CONFIG_APPLIED_LASTCONFIG;

	private DeviceConfigurationEventType()
	{
	}
}

