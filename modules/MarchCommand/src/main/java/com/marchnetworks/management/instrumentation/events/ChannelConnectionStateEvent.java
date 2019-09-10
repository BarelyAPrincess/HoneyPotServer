package com.marchnetworks.management.instrumentation.events;

import com.marchnetworks.command.api.event.AppNotifiable;
import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.command.common.device.data.ChannelState;

public class ChannelConnectionStateEvent extends AbstractDeviceEvent implements AppNotifiable
{
	private static String DEVICE_RESOURCE_ID = "deviceResourceId";
	private String channelId;
	private ChannelState connectionState;
	private Long deviceResourceId;

	public ChannelConnectionStateEvent( String deviceId, String channelId, ChannelState state, Long rootDeviceResourceId )
	{
		super( ChannelConnectionStateEvent.class.getName(), deviceId );
		this.channelId = channelId;
		connectionState = state;
		deviceResourceId = rootDeviceResourceId;
	}

	public ChannelState getConnectionState()
	{
		return connectionState;
	}

	public String getChannelId()
	{
		return channelId;
	}

	public Long getDeviceResourceId()
	{
		return deviceResourceId;
	}

	public EventNotification getNotificationInfo()
	{
		Builder builder = new Builder( getEventNotificationType() ).source( getChannelId() ).value( connectionState ).info( DEVICE_RESOURCE_ID, getDeviceResourceId().toString() );

		EventNotification en = builder.build();
		return en;
	}

	public String getEventNotificationType()
	{
		return "channel.connection.state";
	}
}

