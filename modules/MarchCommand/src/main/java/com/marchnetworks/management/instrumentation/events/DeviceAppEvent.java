package com.marchnetworks.management.instrumentation.events;

import com.marchnetworks.command.api.event.AppNotifiable;
import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.command.common.transport.data.Event;
import com.marchnetworks.command.common.transport.data.GenericValue;
import com.marchnetworks.command.common.transport.data.Pair;
import com.marchnetworks.command.common.transport.data.Timestamp;

public class DeviceAppEvent extends AbstractDeviceEvent implements AppNotifiable
{
	private EventNotification notification;

	public DeviceAppEvent( String deviceId, String deviceResourceId, Event event )
	{
		this( deviceId, deviceResourceId, event.getId(), event.getTimestamp().getTicks() / 1000L, event.getName(), event.getValue().getValue(), event.getSource(), event.getInfo() );
	}

	public DeviceAppEvent( String deviceId, String deviceResourceId, long id, long timestamp, String name, Object value, String source, Pair[] info )
	{
		super( DeviceAppEvent.class.getName(), deviceId );

		Builder builder = new Builder( name ).id( id ).source( source ).timestamp( timestamp ).value( value );

		for ( Pair p : info )
		{
			builder.info( p.getName(), p.getValue() );
		}
		builder.info( "CES_DEVICE_RESOURCE_ID", deviceResourceId );
		builder.info( "CES_DEVICE_ID", deviceId );

		notification = builder.build();
	}

	public EventNotification getNotificationInfo()
	{
		return notification;
	}

	public String getEventNotificationType()
	{
		return notification.getPath();
	}
}

