package com.marchnetworks.management.topology.events;

import com.marchnetworks.command.api.event.AppNotifiable;
import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.common.event.EventTypesEnum;

public class ArchiverAssociationUpdatedEvent extends Event implements AppNotifiable
{
	private Long archiverResourceId;
	private Long[] deviceResourceIds;

	public ArchiverAssociationUpdatedEvent( Long archiverResourceId, Long[] deviceResourceIds )
	{
		this.archiverResourceId = archiverResourceId;
		this.deviceResourceIds = deviceResourceIds;
	}

	public EventNotification getNotificationInfo()
	{
		return new Builder( getEventNotificationType() ).source( archiverResourceId.toString() ).value( deviceResourceIds != null ? deviceResourceIds : new Long[0] ).build();
	}

	public String getEventNotificationType()
	{
		return EventTypesEnum.ARCHIVER_ASSOCIATION_UPDATED.getFullPathEventName();
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

