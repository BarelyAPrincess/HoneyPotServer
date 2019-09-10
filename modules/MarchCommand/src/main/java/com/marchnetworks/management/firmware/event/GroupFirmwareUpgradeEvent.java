package com.marchnetworks.management.firmware.event;

import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.common.event.EventTypesEnum;

public class GroupFirmwareUpgradeEvent extends Event implements com.marchnetworks.command.api.event.Notifiable
{
	private String groupId;
	private String firmwareId;

	public GroupFirmwareUpgradeEvent( String groupId, String firmwareId )
	{
		super( GroupFirmwareUpgradeEvent.class.getName() );
		this.groupId = groupId;
		this.firmwareId = firmwareId;
	}

	public String getFirmwareGroupId()
	{
		return groupId;
	}

	public String getDefaultFirmwareId()
	{
		return firmwareId;
	}

	public com.marchnetworks.command.api.event.EventNotification getNotificationInfo()
	{
		return new EventNotification.Builder( getEventNotificationType() ).source( groupId ).info( "groupId", groupId ).info( "defaultFirmwareId", firmwareId == null ? "" : firmwareId ).build();
	}

	public String getEventNotificationType()
	{
		return EventTypesEnum.GROUP_FIRMWARE_UPGRADE_CHANGED.getFullPathEventName();
	}
}

