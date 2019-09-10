package com.marchnetworks.management.config.events;

public class DeviceConfigAppliedNotification extends ConfigNotification
{
	private static final long serialVersionUID = -6370046661757630613L;

	public DeviceConfigAppliedNotification( String deviceId, Long devConfigId, ConfigNotificationType type )
	{
		super( deviceId, type );
		setDevConfigId( devConfigId );
	}

	public DeviceConfigAppliedNotification( String deviceId, Long devConfigId, ConfigNotificationType type, String message )
	{
		super( deviceId, type, message );
		setDevConfigId( devConfigId );
	}
}
