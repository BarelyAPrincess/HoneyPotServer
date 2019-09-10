package com.marchnetworks.management.instrumentation.events;

import com.marchnetworks.command.api.event.AppNotifiable;
import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.command.common.device.data.ConnectState;

public class DeviceConnectionStateChangeEvent extends AbstractDeviceEvent implements AppNotifiable
{
	private ConnectState connectState;

	public DeviceConnectionStateChangeEvent( String deviceId, ConnectState connectState )
	{
		super( DeviceConnectionStateChangeEvent.class.getName(), deviceId );
		this.connectState = connectState;
	}

	public EventNotification getNotificationInfo()
	{
		Builder builder = new Builder( getEventNotificationType() ).source( getDeviceId() ).value( connectState );

		EventNotification en = builder.build();
		return en;
	}

	public String getEventNotificationType()
	{
		return "device.connection.state";
	}

	public ConnectState getConnectState()
	{
		return connectState;
	}
}

