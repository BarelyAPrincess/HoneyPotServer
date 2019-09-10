package com.marchnetworks.management.topology.events;

import com.marchnetworks.command.api.event.AppNotifiable;
import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.common.event.EventTypesEnum;

public class ArchiverAssociationRemovedEvent extends Event implements AppNotifiable
{
	private Long archiverResourceId;
	private Long[] deviceResourceIds;

	public ArchiverAssociationRemovedEvent( Long archiverId, Long[] deviceResourceIds )
	{
		archiverResourceId = archiverId;
		this.deviceResourceIds = deviceResourceIds;
	}

	public EventNotification getNotificationInfo()
	{
		return new Builder( getEventNotificationType() ).source( archiverResourceId.toString() ).value( deviceResourceIds ).build();
	}

	public String getEventNotificationType()
	{
		return EventTypesEnum.ARCHIVER_ASSOCIATION_REMOVED.getFullPathEventName();
	}

	public Long getArchiverResourceId()
	{
		return archiverResourceId;
	}

	public Long[] getDeviceResourceIds()
	{
		return deviceResourceIds;
	}
}

