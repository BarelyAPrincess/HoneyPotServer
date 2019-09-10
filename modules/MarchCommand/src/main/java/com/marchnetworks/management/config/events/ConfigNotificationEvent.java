package com.marchnetworks.management.config.events;

import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.command.api.event.Notifiable;
import com.marchnetworks.common.event.AbstractTerritoryAwareEvent;
import com.marchnetworks.common.event.EventTypesEnum;
import com.marchnetworks.management.config.DeviceImageState;
import com.marchnetworks.management.config.DeviceSnapshotState;

import java.util.Set;

public class ConfigNotificationEvent extends AbstractTerritoryAwareEvent implements Notifiable
{
	private ConfigNotification configNotification;
	private static final String DEVICE_CONFIG_ID_KEY = "deviceConfigurationId";
	private static final String DEVICE_FIRMWARE_VERSION_KEY = "deviceFirmwareVersion";

	public ConfigNotificationEvent()
	{
	}

	public ConfigNotificationEvent( ConfigNotification configNotification, Set<Long> territoryInfo )
	{
		super( configNotification.getNotificationType().name() );
		this.configNotification = configNotification;
	}

	public ConfigNotification getNotification()
	{
		return configNotification;
	}

	public String toString()
	{
		return configNotification.toString();
	}

	public String getEventNotificationType()
	{
		return EventTypesEnum.CONFIGURATION_DEVICE_UPDATED.getFullPathEventName();
	}

	public EventNotification getNotificationInfo()
	{
		String notificationValue = null;
		if ( configNotification.getImageState() != null )
		{
			notificationValue = getImageStateFromNotification();
		}

		return new Builder( getEventNotificationType() ).source( configNotification.getDeviceId() ).value( notificationValue ).info( "deviceConfigurationId", configNotification.getDevConfigId() == null ? "" : configNotification.getDevConfigId().toString() ).info( "deviceFirmwareVersion", configNotification.getFirmwareVersionInfo() == null ? "" : configNotification.getFirmwareVersionInfo() ).build();
	}

	private String getImageStateFromNotification()
	{
		String stateValue = null;
		if ( configNotification.getImageState() != null )
		{
			if ( ( configNotification.getImageState().equals( DeviceImageState.FAILED ) ) || ( configNotification.getImageState().equals( DeviceImageState.PENDING ) ) || ( configNotification.getImageState().equals( DeviceImageState.PENDING_FIRMWARE ) ) )
			{

				stateValue = configNotification.getImageState().name();
			}
			else if ( ( configNotification.getSnapshotState() != null ) && ( configNotification.getSnapshotState().equals( DeviceSnapshotState.MISMATCH ) ) )
			{
				stateValue = DeviceImageState.MISMATCH.name();
			}
			else
			{
				stateValue = configNotification.getImageState().name();
			}
		}

		return stateValue;
	}
}
