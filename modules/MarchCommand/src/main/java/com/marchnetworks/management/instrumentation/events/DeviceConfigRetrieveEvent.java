package com.marchnetworks.management.instrumentation.events;

import com.marchnetworks.command.common.device.data.RegistrationStatus;

public class DeviceConfigRetrieveEvent extends AbstractDeviceEvent
{
	private RegistrationStatus registrationStatus;

	public DeviceConfigRetrieveEvent( String deviceId, RegistrationStatus registrationStatus )
	{
		super( DeviceConfigRetrieveEvent.class.getName(), deviceId );

		this.registrationStatus = registrationStatus;
	}

	public RegistrationStatus getRegistrationStatus()
	{
		return registrationStatus;
	}
}

