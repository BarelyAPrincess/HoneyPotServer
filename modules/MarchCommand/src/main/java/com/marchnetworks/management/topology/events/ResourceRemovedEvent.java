package com.marchnetworks.management.topology.events;

import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.common.event.EventTypesEnum;

import java.util.Set;

public class ResourceRemovedEvent extends TopologyEvent
{
	public ResourceRemovedEvent( Resource resource, Set<Long> territoryInfo )
	{
		super( TopologyEvent.class.getName(), resource, territoryInfo );
	}

	public String getEventNotificationType()
	{
		return EventTypesEnum.TOPOLOGY_REMOVED.getFullPathEventName();
	}

	public EventNotification getNotificationInfo()
	{
		return new Builder( getEventNotificationType() ).source( getResourceId().toString() ).value( resource ).build();
	}
}

