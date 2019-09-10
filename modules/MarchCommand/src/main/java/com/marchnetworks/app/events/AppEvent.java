package com.marchnetworks.app.events;

import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.command.api.event.Notifiable;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.common.event.EventTypesEnum;

public class AppEvent extends Event implements Notifiable
{
	protected AppEventType appEventType;
	protected String appID;

	public AppEvent( AppEventType eventType, String appID )
	{
		super( AppEvent.class.getName() );
		appEventType = eventType;
		this.appID = appID;
	}

	public AppEvent( String type, AppEventType eventType, String appID )
	{
		super( type );
		appEventType = eventType;
		this.appID = appID;
	}

	public String getEventNotificationType()
	{
		if ( appEventType == AppEventType.INSTALLED )
			return EventTypesEnum.APP_INSTALLED.getFullPathEventName();
		if ( appEventType == AppEventType.STATE )
			return EventTypesEnum.APP_STATE.getFullPathEventName();
		if ( appEventType == AppEventType.UNINSTALLED )
		{
			return EventTypesEnum.APP_UNINSTALLED.getFullPathEventName();
		}
		return EventTypesEnum.APP_UPGRADED.getFullPathEventName();
	}

	public EventNotification getNotificationInfo()
	{
		Builder builder = new Builder( getEventNotificationType() ).source( appID );

		EventNotification en = builder.build();
		return en;
	}

	public String getAppID()
	{
		return appID;
	}

	public AppEventType getAppEventType()
	{
		return appEventType;
	}
}
