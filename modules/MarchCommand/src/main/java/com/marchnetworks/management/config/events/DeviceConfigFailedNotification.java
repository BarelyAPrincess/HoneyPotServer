package com.marchnetworks.management.config.events;

public class DeviceConfigFailedNotification extends ConfigNotification
{
	private static final long serialVersionUID = 8510761024081240733L;

	public DeviceConfigFailedNotification( String deviceId, Long devConfigId, ConfigNotificationType type )
	{
		super( deviceId, type );
		setDevConfigId( devConfigId );
	}

	public DeviceConfigFailedNotification( String deviceId, Long devConfigId, ConfigNotificationType type, String message )
	{
		super( deviceId, type, message );
		setDevConfigId( devConfigId );
	}
}
