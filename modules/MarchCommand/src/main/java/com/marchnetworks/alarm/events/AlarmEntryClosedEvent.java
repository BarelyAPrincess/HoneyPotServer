package com.marchnetworks.alarm.events;

import com.marchnetworks.alarm.data.AlarmEntryView;
import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.command.api.event.Notifiable;
import com.marchnetworks.common.event.AbstractTerritoryAwareEvent;
import com.marchnetworks.common.event.EventTypesEnum;

import java.util.Set;

public class AlarmEntryClosedEvent extends AbstractTerritoryAwareEvent implements Notifiable
{
	private AlarmEntryView alarmEntry;
	private boolean updateAssociations;

	public AlarmEntryClosedEvent( Set<Long> territoryInfo, AlarmEntryView alarmEntry, boolean updateAssociations )
	{
		super( AlarmEntryClosedEvent.class.getName(), territoryInfo );
		this.alarmEntry = alarmEntry;
		this.updateAssociations = updateAssociations;
	}

	public String getEventNotificationType()
	{
		return EventTypesEnum.ALARM_ENTRY_CLOSED.getFullPathEventName();
	}

	public EventNotification getNotificationInfo()
	{
		Builder builder = new Builder( getEventNotificationType() ).source( alarmEntry.getAlarmSourceId().toString() ).timestamp( alarmEntry.getClosedTime() ).value( alarmEntry.getId().toString() ).info( "count", String.valueOf( alarmEntry.getCount() ) ).info( "first", String.valueOf( alarmEntry.getFirstInstanceTime() ) ).info( "last", String.valueOf( alarmEntry.getLastInstanceTime() ) ).info( "closedByUser", alarmEntry.getClosedByUser() ).info( "text", alarmEntry.getClosedText() );

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

		EventNotification en = builder.build();
		return en;
	}
}
