package com.marchnetworks.management.instrumentation.events;

import com.marchnetworks.command.api.event.AppNotifiable;
import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.common.event.EventTypesEnum;

public class DeviceRestartEvent extends AbstractDeviceEvent implements AppNotifiable
{
	public DeviceRestartEvent( String deviceId )
	{
		super( DeviceRestartEvent.class.getName(), deviceId );
	}

	public EventNotification getNotificationInfo()
	{
		Builder builder = new Builder( getEventNotificationType() ).source( m_deviceId );

		EventNotification en = builder.build();
		return en;
	}

	public String getEventNotificationType()
	{
		return EventTypesEnum.DEVICE_RESTART.getFullPathEventName();
	}
}

