package com.marchnetworks.management.instrumentation.events;

import com.marchnetworks.command.common.device.data.RegistrationStatus;

public class ChildDeviceRegistrationEvent extends AbstractDeviceEvent
{
	private RegistrationStatus registrationStatus;

	public ChildDeviceRegistrationEvent( String deviceId, RegistrationStatus registrationStatus )
	{
		super( ChildDeviceRegistrationEvent.class.getName(), deviceId );
		this.registrationStatus = registrationStatus;
	}

	public RegistrationStatus getRegistrationStatus()
	{
		return registrationStatus;
	}
}

