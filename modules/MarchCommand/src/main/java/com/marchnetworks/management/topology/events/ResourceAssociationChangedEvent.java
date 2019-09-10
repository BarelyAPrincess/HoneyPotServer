package com.marchnetworks.management.topology.events;

import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.common.event.EventTypesEnum;

import java.util.Set;

public class ResourceAssociationChangedEvent extends TopologyEvent
{
	private String associationType;

	public ResourceAssociationChangedEvent( com.marchnetworks.command.common.topology.data.Resource resource, Long parentResourceId, String associationType, Set<Long> territoryInfo )
	{
		super( TopologyEvent.class.getName(), resource, territoryInfo );
		this.parentResourceId = parentResourceId;
		this.associationType = associationType;
	}

	public String getEventNotificationType()
	{
		return EventTypesEnum.TOPOLOGY_MOVED.getFullPathEventName();
	}

	public com.marchnetworks.command.api.event.EventNotification getNotificationInfo()
	{
		return new EventNotification.Builder( getEventNotificationType() ).source( getResourceId().toString() ).value( getParentResourceId() ).build();
	}

	public String getAssociationType()
	{
		return associationType;
	}
}

