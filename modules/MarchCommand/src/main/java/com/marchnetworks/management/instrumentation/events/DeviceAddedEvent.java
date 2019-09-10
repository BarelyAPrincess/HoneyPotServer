package com.marchnetworks.management.instrumentation.events;

import java.util.Map;

public class DeviceAddedEvent extends AbstractDeviceEvent
{
	public DeviceAddedEvent( String deviceId )
	{
		super( DeviceAddedEvent.class.getName(), deviceId );
	}

	public DeviceAddedEvent( String deviceId, Map<String, Object> deviceRegistrationInfo )
	{
		super( DeviceAddedEvent.class.getName(), deviceId );
		setDeviceExtraInfo( deviceRegistrationInfo );
	}
}

