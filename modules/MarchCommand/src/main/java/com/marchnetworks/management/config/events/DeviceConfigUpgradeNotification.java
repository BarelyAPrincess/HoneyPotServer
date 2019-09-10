package com.marchnetworks.management.config.events;

public class DeviceConfigUpgradeNotification extends ConfigNotification
{
	private static final long serialVersionUID = 5137228736818830495L;

	public DeviceConfigUpgradeNotification( String deviceId, Long devConfigId, ConfigNotificationType type )
	{
		super( deviceId, type );
		setDevConfigId( devConfigId );
	}

	public DeviceConfigUpgradeNotification( String deviceId, Long devConfigId, ConfigNotificationType type, String message )
	{
		super( deviceId, type, message );
		setDevConfigId( devConfigId );
	}
}
