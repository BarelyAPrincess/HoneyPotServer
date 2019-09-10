package com.marchnetworks.management.topology.events;

import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.common.event.EventTypesEnum;

import java.util.Set;

public class ResourceUpdatedEvent extends TopologyEvent
{
	public ResourceUpdatedEvent( Resource resource, Set<Long> territoryInfo )
	{
		super( TopologyEvent.class.getName(), resource, territoryInfo );
		parentResourceId = resource.getParentResourceId();
	}

	public String getEventNotificationType()
	{
		return EventTypesEnum.TOPOLOGY_UPDATED.getFullPathEventName();
	}

	public EventNotification getNotificationInfo()
	{
		EventNotification event = new Builder( getEventNotificationType() ).source( getResourceId().toString() ).value( resource ).build();

		if ( getParentResourceId() != null )
		{
			event.addInfo( "CES_PARENT_RESOURCE_ID", getParentResourceId().toString() );
		}
		return event;
	}
}

