package com.marchnetworks.management.topology.events;

import com.marchnetworks.command.api.event.AppNotifiable;
import com.marchnetworks.command.api.event.Notifiable;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.common.event.AbstractTerritoryAwareEvent;

import java.util.Set;

public abstract class TopologyEvent extends AbstractTerritoryAwareEvent implements Notifiable, AppNotifiable
{
	protected Resource resource;
	protected Long resourceId;
	protected Long parentResourceId;

	public TopologyEvent()
	{
	}

	public TopologyEvent( String type, Resource resource, Set<Long> territoryInfo )
	{
		super( type, territoryInfo );
		this.resource = resource;
		resourceId = resource.getId();
	}

	public Long getResourceId()
	{
		return resourceId;
	}

	public Long getParentResourceId()
	{
		return parentResourceId;
	}

	public Resource getResource()
	{
		return resource;
	}
}

