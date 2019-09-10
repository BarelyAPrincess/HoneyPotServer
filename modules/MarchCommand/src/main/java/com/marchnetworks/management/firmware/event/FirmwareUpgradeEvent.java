package com.marchnetworks.management.firmware.event;

import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.common.event.EventTypesEnum;
import com.marchnetworks.management.firmware.data.UpdateStateEnum;
import com.marchnetworks.management.instrumentation.events.AbstractDeviceEvent;

public class FirmwareUpgradeEvent extends AbstractDeviceEvent implements com.marchnetworks.command.api.event.Notifiable
{
	private String deviceId;
	private UpdateStateEnum state;
	private String version;

	public FirmwareUpgradeEvent( UpdateStateEnum state, String deviceId, String version )
	{
		super( FirmwareUpgradeEvent.class.getName(), deviceId );
		this.deviceId = deviceId;
		this.state = state;
		this.version = version;
	}

	public String getDeviceId()
	{
		return deviceId;
	}

	public String getFirmwareVersion()
	{
		return version;
	}

	public com.marchnetworks.command.api.event.EventNotification getNotificationInfo()
	{
		return new EventNotification.Builder( getEventNotificationType() ).source( deviceId ).info( "deviceId", deviceId ).info( "deviceFirmwareVersion", version == null ? "" : version ).build();
	}

	public String getEventNotificationType()
	{
		String eventPath = null;
		if ( state == UpdateStateEnum.FIRMWARE_UPGRADE_IDLE )
		{
			eventPath = EventTypesEnum.FIRMWARE_UPGRADE_CHANGED.getFullPathEventName();
		}
		else if ( state == UpdateStateEnum.FIRMWARE_UPGRADE_PENDING )
		{
			eventPath = EventTypesEnum.FIRMWARE_UPGRADE_PENDING.getFullPathEventName();
		}
		else if ( state == UpdateStateEnum.FIRMWARE_UPGRADE_COMPLETED )
		{
			eventPath = EventTypesEnum.FIRMWARE_UPGRADE_COMPLETED.getFullPathEventName();
		}
		else if ( state == UpdateStateEnum.FIRMWARE_UPGRADE_TIMEOUT )
		{
			eventPath = EventTypesEnum.FIRMWARE_UPGRADE_TIMEOUT.getFullPathEventName();
		}
		else if ( state == UpdateStateEnum.FIRMWARE_UPGRADE_ACCEPTED )
		{
			eventPath = EventTypesEnum.FIRMWARE_UPGRADE_ACCEPTED.getFullPathEventName();
		}
		else
		{
			eventPath = EventTypesEnum.FIRMWARE_UPGRADE_FAILED.getFullPathEventName();
		}
		return eventPath;
	}
}

