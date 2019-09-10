package com.marchnetworks.management.config.events;

public class DeviceConfigUnregistrationNotification extends ConfigNotification
{
	private static final long serialVersionUID = 8305825814791754490L;

	public DeviceConfigUnregistrationNotification( String deviceId, Long devConfigId, ConfigNotificationType type )
	{
		super( deviceId, type );
		setDevConfigId( devConfigId );
	}

	public DeviceConfigUnregistrationNotification( String deviceId, Long devConfigId, ConfigNotificationType type, String message )
	{
		super( deviceId, type, message );
		setDevConfigId( devConfigId );
	}
}
