package com.marchnetworks.management.instrumentation.events;

import com.marchnetworks.command.api.event.AppNotifiable;
import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.common.event.EventTypesEnum;

public class DeviceIpChangedEvent extends AbstractDeviceEvent implements AppNotifiable
{
	private String ipAddress;
	private String deviceResourceId;

	public DeviceIpChangedEvent( String deviceResourceId, String deviceId, String ipAddress )
	{
		super( DeviceIpChangedEvent.class.getName(), deviceId );
		this.ipAddress = ipAddress;
		this.deviceResourceId = deviceResourceId;
	}

	public EventNotification getNotificationInfo()
	{
		return new Builder( getEventNotificationType() ).source( deviceResourceId ).value( ipAddress ).build();
	}

	public String getEventNotificationType()
	{
		return EventTypesEnum.DEVICE_IP_CHANGED.getFullPathEventName();
	}

	public String getDeviceResourceId()
	{
		return deviceResourceId;
	}
}

