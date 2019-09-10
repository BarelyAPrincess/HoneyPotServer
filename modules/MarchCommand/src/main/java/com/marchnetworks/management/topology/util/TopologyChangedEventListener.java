package com.marchnetworks.management.topology.util;

import com.marchnetworks.common.event.Event;
import com.marchnetworks.management.topology.events.ResourceAssociationChangedEvent;
import com.marchnetworks.management.topology.events.ResourceCreatedEvent;
import com.marchnetworks.management.topology.events.ResourceRemovedEvent;
import com.marchnetworks.management.topology.events.ResourceUpdatedEvent;
import com.marchnetworks.server.event.EventListener;

public class TopologyChangedEventListener implements EventListener
{
	public void process( Event event )
	{
		if ( ( event instanceof ResourceUpdatedEvent ) )
		{

			ResourceUpdatedEvent resourceUpdatedEvent = ( ResourceUpdatedEvent ) event;
			TopologyCache.updateResource( resourceUpdatedEvent.getResource() );
		}
		else if ( ( event instanceof ResourceCreatedEvent ) )
		{
			ResourceCreatedEvent resourceCreatedEvent = ( ResourceCreatedEvent ) event;
			TopologyCache.addResource( resourceCreatedEvent.getResource(), resourceCreatedEvent.getAssociationType(), resourceCreatedEvent.getParentResourceId() );
		}
		else if ( ( event instanceof ResourceRemovedEvent ) )
		{
			ResourceRemovedEvent resourceRemovedEvent = ( ResourceRemovedEvent ) event;
			TopologyCache.removeResource( resourceRemovedEvent.getResourceId() );
		}
		else if ( ( event instanceof ResourceAssociationChangedEvent ) )
		{
			ResourceAssociationChangedEvent resourceMovedEvent = ( ResourceAssociationChangedEvent ) event;
			TopologyCache.updateResourceAssociation( resourceMovedEvent.getResourceId(), resourceMovedEvent.getParentResourceId(), resourceMovedEvent.getAssociationType() );
		}
	}

	public String getListenerName()
	{
		return TopologyChangedEventListener.class.getSimpleName();
	}
}

