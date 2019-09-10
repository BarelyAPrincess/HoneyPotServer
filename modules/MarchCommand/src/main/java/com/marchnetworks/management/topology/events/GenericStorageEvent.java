package com.marchnetworks.management.topology.events;

import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.command.api.event.Notifiable;
import com.marchnetworks.command.common.topology.data.Store;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.common.event.EventTypesEnum;

public class GenericStorageEvent extends Event implements Notifiable, com.marchnetworks.command.api.event.AppNotifiable
{
	public final String INFO_STORE = "store";
	public final String INFO_APPID = "appId";
	protected GenericStorageEventType eventType;
	protected Store store;
	protected String objectId;
	protected String appId;
	protected int size;

	public GenericStorageEvent( GenericStorageEventType eventType, Store store, String objectId, String appId, long size )
	{
		super( GenericStorageEvent.class.getName() );
		this.eventType = eventType;
		this.store = store;
		this.objectId = objectId;
		this.appId = appId;
		this.size = ( ( int ) size );
	}

	public String getEventNotificationType()
	{
		if ( eventType == GenericStorageEventType.UPDATED )
		{
			return EventTypesEnum.OBJECT_UPDATED.getFullPathEventName();
		}
		return EventTypesEnum.OBJECT_REMOVED.getFullPathEventName();
	}

	public EventNotification getNotificationInfo()
	{
		Builder builder = new Builder( getEventNotificationType() ).source( objectId ).value( Integer.valueOf( size ) ).info( "store", store.toString() );

		if ( appId != null )
		{
			builder.info( "appId", appId );
		}

		EventNotification en = builder.build();
		return en;
	}
}

