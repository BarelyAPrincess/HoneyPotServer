package com.marchnetworks.management.config.events;

public class DeviceConfigAssociationNotification extends ConfigNotification
{
	private static final long serialVersionUID = -4170849037163724494L;

	public DeviceConfigAssociationNotification( String deviceId, Long devConfigId, ConfigNotificationType type )
	{
		super( deviceId, type );
		setDevConfigId( devConfigId );
	}

	public DeviceConfigAssociationNotification( String deviceId, Long devConfigId, ConfigNotificationType type, String message )
	{
		super( deviceId, type, message );
		setDevConfigId( devConfigId );
	}
}
