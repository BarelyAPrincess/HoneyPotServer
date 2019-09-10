package com.marchnetworks.app.events;

import com.marchnetworks.app.data.AppStatus;
import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;

public class AppStateEvent extends AppEvent
{
	protected AppStatus status;
	protected long startedTime;

	public AppStateEvent( String appID, AppStatus status )
	{
		super( AppStateEvent.class.getName(), AppEventType.STATE, appID );
		this.status = status;
		startedTime = 0L;
	}

	public AppStateEvent( String appID, AppStatus status, long startedTime )
	{
		super( AppStateEvent.class.getName(), AppEventType.STATE, appID );
		this.status = status;
		this.startedTime = ( startedTime * 1000L );
	}

	public EventNotification getNotificationInfo()
	{
		Builder builder = new Builder( getEventNotificationType() ).source( appID ).value( status.toString() );

		if ( startedTime != 0L )
		{
			builder.info( "startTime", String.valueOf( startedTime ) );
		}

		EventNotification en = builder.build();
		return en;
	}

	public AppStatus getStatus()
	{
		return status;
	}
}
