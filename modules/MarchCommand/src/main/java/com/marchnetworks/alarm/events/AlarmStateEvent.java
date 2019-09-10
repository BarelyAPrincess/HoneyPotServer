package com.marchnetworks.alarm.events;

import com.marchnetworks.command.api.event.AppNotifiable;
import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.command.api.event.Notifiable;
import com.marchnetworks.command.common.alarm.data.AlarmSourceView;
import com.marchnetworks.common.event.AbstractTerritoryAwareEvent;
import com.marchnetworks.common.event.EventTypesEnum;

import java.util.Set;

public class AlarmStateEvent extends AbstractTerritoryAwareEvent implements Notifiable, AppNotifiable
{
	private AlarmSourceView alarmSource;
	private boolean updateAssociations;
	private long timestamp;
	private Long resourceId;
	private Long lastStateChange;

	public AlarmStateEvent( Set<Long> territoryInfo, Long resourceId, Long lastStateChange, AlarmSourceView alarmSource, long timestamp, boolean updateAssociations )
	{
		super( AlarmStateEvent.class.getName(), territoryInfo );
		this.alarmSource = alarmSource;
		this.updateAssociations = updateAssociations;
		this.timestamp = timestamp;
		this.resourceId = resourceId;
		this.lastStateChange = lastStateChange;
	}

	public String getEventNotificationType()
	{
		return EventTypesEnum.ALARM_STATE.getFullPathEventName();
	}

	public EventNotification getNotificationInfo()
	{
		Builder builder = new Builder( getEventNotificationType() ).source( alarmSource.getId().toString() ).value( alarmSource.getState().getValue() ).timestamp( timestamp );

		if ( alarmSource.getExtendedState() != null )
		{
			builder.info( "extState", alarmSource.getExtendedState().getValue() );
		}
		builder.info( "CES_RESOURCE_ID", resourceId.toString() );
		if ( lastStateChange != null )
		{
			builder.info( "lastStateChange", lastStateChange.toString() );
		}

		if ( updateAssociations )
		{
			String[] channels = alarmSource.getAssociatedChannels();
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
