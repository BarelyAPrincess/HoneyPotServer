package com.marchnetworks.management.instrumentation.events;

import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.command.common.device.DeviceEventsEnum;
import com.marchnetworks.command.common.transport.data.GenericValue;
import com.marchnetworks.command.common.transport.data.Pair;
import com.marchnetworks.common.event.StateCacheable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeviceStatisticsStateEvent extends AbstractDeviceEvent implements StateCacheable
{
	private static final String ROOT_DEVICE_EXTRA_INFO_KEY = "rootDeviceId";
	private static final List<String> statisticsEventsList = new ArrayList();

	private String pathName;
	private GenericValue value;
	private boolean isDeleteEvent;
	private Pair[] details;
	private String source;

	static
	{
		Collections.addAll( statisticsEventsList, new String[] {DeviceEventsEnum.CHANNEL_RECORDING.getPath(), DeviceEventsEnum.CHANNEL_STREAMING.getPath(), DeviceEventsEnum.CHANNEL_CONFIGURED.getPath(), DeviceEventsEnum.CHANNEL_BANDWIDH_INCOMING.getPath(), DeviceEventsEnum.CHANNEL_BANDWIDH_RECORDING.getPath(), DeviceEventsEnum.SYSTEM_STREAMING.getPath(), DeviceEventsEnum.SYSTEM_RECORDING.getPath(), DeviceEventsEnum.SYSTEM_CONFIGURED.getPath(), DeviceEventsEnum.SYSTEM_BANDWIDTH_RECORDING.getPath(), DeviceEventsEnum.SYSTEM_BANDWIDTH_INCOMING_IP.getPath(), DeviceEventsEnum.SYSTEM_BANDWIDTH_OUTGOING.getPath(), DeviceEventsEnum.SYSTEM_CPULOAD.getPath(), DeviceEventsEnum.SYSTEM_MEMORYUSED.getPath(), DeviceEventsEnum.SYSTEM_CPULOAD_TOTAL.getPath(), DeviceEventsEnum.SYSTEM_MEMORYUSED_TOTAL.getPath(), DeviceEventsEnum.SYSTEM_MEMORY_TOTAL.getPath()} );
	}

	public static boolean isDeviceStatisticsEvent( String eventPath )
	{
		return statisticsEventsList.contains( eventPath );
	}

	public DeviceStatisticsStateEvent( String deviceId, String source, String pathName, GenericValue value, Pair[] details, long timestamp, boolean isDeleteEvent )
	{
		super( DeviceStatisticsStateEvent.class.getName(), deviceId );
		this.pathName = pathName;
		this.value = value;
		this.isDeleteEvent = isDeleteEvent;

		this.details = details;
		this.source = source;
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

