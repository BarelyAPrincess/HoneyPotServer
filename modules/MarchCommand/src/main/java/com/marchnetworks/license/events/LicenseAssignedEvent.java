package com.marchnetworks.license.events;

import com.marchnetworks.command.api.event.EventNotification;

public class LicenseAssignedEvent extends LicenseExternalEvent
{
	private String deviceId;

	public LicenseAssignedEvent()
	{
		super( LicenseExternalEvent.class.getName() );
	}

	public LicenseAssignedEvent( String deviceId )
	{
		super( LicenseExternalEvent.class.getName() );
		this.deviceId = deviceId;
	}

	public String getEventNotificationType()
	{
		return null;
	}

	public EventNotification getNotificationInfo()
	{
		return null;
	}

	public String getDeviceId()
	{
		return deviceId;
	}

	public void setDeviceId( String deviceId )
	{
		this.deviceId = deviceId;
	}
}
