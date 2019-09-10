package com.marchnetworks.management.instrumentation.events;

import com.marchnetworks.command.api.event.AppNotifiable;
import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.command.common.transport.data.Event;
import com.marchnetworks.command.common.transport.data.GenericValue;
import com.marchnetworks.command.common.transport.data.Pair;

public class ExtractorJobEvent extends AbstractDeviceEvent implements AppNotifiable
{
	private EventNotification notification;

	public ExtractorJobEvent( String deviceId, String deviceResourceId, Event event )
	{
		super( ExtractorJobEvent.class.getName(), deviceId );

		Builder builder = new Builder( event.getName() ).id( event.getId() ).source( event.getSource() ).timestamp( timestamp ).value( event.getValue().getValue() );

		for ( Pair p : event.getInfo() )
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

	public String getName()
	{
		return notification.getPath();
	}

	public String getSource()
	{
		return notification.getSource();
	}

	public Object getValue()
	{
		return notification.getValue();
	}

	public String getInfo( String name )
	{
		return notification.getInfo( name );
	}
}

