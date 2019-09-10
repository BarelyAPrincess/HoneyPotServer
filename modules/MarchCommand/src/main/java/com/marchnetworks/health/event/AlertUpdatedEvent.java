package com.marchnetworks.health.event;

import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.common.event.EventTypesEnum;

import java.util.Set;
import java.util.StringTokenizer;

public class AlertUpdatedEvent extends AlertChangedEvent
{
	private long count;
	private long lastInstance;
	private String description;
	private String info;
	private Integer duration;
	private Integer frequency = null;
	private Boolean deviceState;

	public AlertUpdatedEvent( Set<Long> territoryInfo, String id, long count, long lastInstance, String description, String info, Integer duration, Integer frequency, Boolean deviceState )
	{
		super( HealthEventEnum.DEVICE_ALERT.name(), territoryInfo, EventTypesEnum.HEALTH_UPDATED, id );
		this.count = count;
		this.lastInstance = lastInstance;
		this.description = description;
		this.info = info;
		this.duration = duration;
		this.frequency = frequency;
		this.deviceState = deviceState;
	}

	public AlertUpdatedEvent( Set<Long> territoryInfo, String id, long count, long lastInstance, String description, String info )
	{
		this( territoryInfo, id, count, lastInstance, description, info, null, null, null );
	}

	public EventNotification getNotificationInfo()
	{
		Builder builder = new Builder( getEventNotificationType() ).source( alertID ).info( "count", String.valueOf( count ) ).info( "last", String.valueOf( lastInstance ) );

		if ( duration != null )
		{
			builder.info( "duration", String.valueOf( duration ) );
		}

		if ( frequency != null )
		{
			builder.info( "frequency", String.valueOf( frequency ) );
		}

		if ( description != null )
		{
			builder.value( description );
		}

		if ( deviceState != null )
		{
			builder.info( "deviceState", deviceState.toString() );
		}

		if ( info != null )
		{
			if ( !info.isEmpty() )
			{
				StringTokenizer pairs = new StringTokenizer( info, "|" );

				while ( pairs.hasMoreTokens() )
				{
					String infoPair = pairs.nextToken();
					builder.info( "alertInfo", infoPair );
				}
			}
			else
			{
				builder.info( "alertInfo", "" );
			}
		}

		EventNotification en = builder.build();
		return en;
	}
}
