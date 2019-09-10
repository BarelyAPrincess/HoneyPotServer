package com.marchnetworks.command.common.topology.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public abstract class LinkResource extends Resource
{
	private Long[] linkedResourceIds;
	private List<ContainerItem> containerItems;

	public void update( Resource updatedResource )
	{
		if ( ( updatedResource instanceof LinkResource ) )
		{
			LinkResource updatedLinkResource = ( LinkResource ) updatedResource;
			super.update( updatedResource );
			linkedResourceIds = updatedLinkResource.getLinkedResourceIds();
			containerItems = updatedLinkResource.getContainerItems();
		}
	}

	public void removeLinkedResource( Long id )
	{
		List<Long> linkedIds = new ArrayList( Arrays.asList( linkedResourceIds ) );
		linkedIds.removeAll( Collections.singletonList( id ) );
		linkedResourceIds = ( ( Long[] ) linkedIds.toArray( new Long[linkedIds.size()] ) );

		for ( Iterator<ContainerItem> iterator = containerItems.iterator(); iterator.hasNext(); )
		{
			ContainerItem containerItem = ( ContainerItem ) iterator.next();
			if ( containerItem.getId().equals( id ) )
			{
				iterator.remove();
			}
		}
	}

	@XmlElement( required = true )
	public Long[] getLinkedResourceIds()
	{
		return linkedResourceIds;
	}

	public void setLinkedResourceIds( Long[] linkedResourceIds )
	{
		this.linkedResourceIds = linkedResourceIds;
	}

	public List<ContainerItem> getContainerItems()
	{
		return containerItems;
	}

	public void setContainerItems( List<ContainerItem> containerItems )
	{
		this.containerItems = containerItems;
	}
}
