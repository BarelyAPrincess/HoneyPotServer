package com.marchnetworks.management.instrumentation.events;

import com.marchnetworks.command.api.event.AppNotifiable;
import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.command.common.device.DeviceEventsEnum;
import com.marchnetworks.command.common.transport.data.GenericValue;
import com.marchnetworks.command.common.transport.data.Pair;
import com.marchnetworks.common.event.StateCacheable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class GenericDeviceStateEvent extends TerritoryAwareDeviceEvent implements StateCacheable, AppNotifiable
{
	private static final String ROOT_DEVICE_EXTRA_INFO_KEY = "rootDeviceId";
	private static final List<String> stateEventsList = new ArrayList();

	private String pathName;
	private GenericValue value;
	private boolean isDeleteEvent;
	private Pair[] details;
	private String source;

	static
	{
		Collections.addAll( stateEventsList, new String[] {DeviceEventsEnum.CLIENT_BUFFER_LIVE.getPath(), DeviceEventsEnum.CLIENT_BUFFER_LIVEPTZ.getPath(), DeviceEventsEnum.SYSTEM_EXPORT_QUEUE.getPath(), DeviceEventsEnum.EXTRACTOR_STORAGE_FREE.getPath()} );
	}

	public static boolean isDeviceStateEvent( String eventPath )
	{
		return stateEventsList.contains( eventPath );
	}

	public GenericDeviceStateEvent( String deviceId, Set<Long> territoryInfo, String source, String pathName, GenericValue value, Pair[] details, long timestamp, boolean isDeleteEvent )
	{
		super( GenericDeviceStateEvent.class.getName(), deviceId, territoryInfo );
		this.pathName = pathName;
		this.value = value;
		this.isDeleteEvent = isDeleteEvent;

		this.details = details;
		this.source = source;
	}

	public GenericDeviceStateEvent()
	{
		super( null, null, null );
	}

	public EventNotification getNotificationInfo()
	{
		EventNotification eventNotification = new Builder( getEventNotificationType() ).source( source ).build();

		if ( value.getType() == 0 )
		{
			eventNotification.setValue( Integer.valueOf( value.getIntValue() ) );
		}
		else if ( value.getType() == 2 )
		{
			eventNotification.setValue( Long.valueOf( value.getLongValue() ) );
		}
		else
		{
			eventNotification.setValue( value.getStringValue() );
		}
		eventNotification.setInfo( details );
		eventNotification.addInfo( "rootDeviceId", getDeviceId() );
		return eventNotification;
	}

	public String getEventNotificationType()
	{
		return pathName;
	}

	public Long getDeviceIdLong()
	{
		return Long.valueOf( Long.parseLong( getDeviceId() ) );
	}

	public GenericValue getValue()
	{
		return value;
	}

	public boolean isDeleteEvent()
	{
		return isDeleteEvent;
	}
}

