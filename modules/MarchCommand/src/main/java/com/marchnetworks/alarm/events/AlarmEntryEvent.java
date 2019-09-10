package com.marchnetworks.alarm.events;

import com.marchnetworks.alarm.alarmdetails.AlarmDetailEnum;
import com.marchnetworks.alarm.data.AlarmEntryView;
import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.command.api.event.Notifiable;
import com.marchnetworks.common.event.AbstractTerritoryAwareEvent;
import com.marchnetworks.common.event.EventTypesEnum;

import java.util.Set;

public class AlarmEntryEvent extends AbstractTerritoryAwareEvent implements Notifiable
{
	private AlarmEntryView alarmEntry;
	private boolean updateAssociations;
	private boolean updateAlarmDetails;

	public AlarmEntryEvent( Set<Long> territoryInfo, AlarmEntryView alarmEntry, boolean updateAssociations, boolean updateAlarmDetails )
	{
		super( AlarmEntryEvent.class.getName(), territoryInfo );
		this.alarmEntry = alarmEntry;
		this.updateAssociations = updateAssociations;
		this.updateAlarmDetails = updateAlarmDetails;
	}

	public String getEventNotificationType()
	{
		return EventTypesEnum.ALARM_ENTRY.getFullPathEventName();
	}

	public EventNotification getNotificationInfo()
	{
		Builder builder = new Builder( getEventNotificationType() ).source( alarmEntry.getAlarmSourceId().toString() ).timestamp( alarmEntry.getLastInstanceTime() ).value( alarmEntry.getId().toString() ).info( "count", String.valueOf( alarmEntry.getCount() ) ).info( "first", String.valueOf( alarmEntry.getFirstInstanceTime() ) );

		for ( String handler : alarmEntry.getHandlingUsers() )
		{
			builder.info( "handler", handler );
		}

		if ( updateAssociations )
		{
			String[] channels = alarmEntry.getAssociatedChannels();
			for ( String channel : channels )
			{
				builder.info( "assocId", channel );
			}
			if ( channels.length == 0 )
			{
				builder.info( "assocId", "" );
			}
		}

		if ( updateAlarmDetails )
		{
			if ( alarmEntry.getAlarmDetails() != null )
			{
				for ( AlarmDetailEnum detail : alarmEntry.getAlarmDetails() )
				{
					builder.info( "alarmDetails", detail.toString() );
				}
			}
			builder.info( "text", alarmEntry.getClosedText() );
		}

		EventNotification en = builder.build();
		return en;
	}
}
