package com.marchnetworks.management.topology.events;

import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.common.event.EventTypesEnum;

import java.util.Set;

public class ResourceCreatedEvent extends TopologyEvent
{
	private String associationType;

	public ResourceCreatedEvent( Resource resource, Long parentResourceId, String associationType, Set<Long> territoryInfo )
	{
		super( TopologyEvent.class.getName(), resource, territoryInfo );
		this.parentResourceId = parentResourceId;
		this.associationType = associationType;
	}

	public String getEventNotificationType()
	{
		return EventTypesEnum.TOPOLOGY_CREATED.getFullPathEventName();
	}

	public com.marchnetworks.command.api.event.EventNotification getNotificationInfo()
	{
		return new EventNotification.Builder( getEventNotificationType() ).source( getResourceId().toString() ).value( resource ).info( "CES_PARENT_RESOURCE_ID", getParentResourceId().toString() ).build();
	}

	public String getAssociationType()
	{
		return associationType;
	}
}

