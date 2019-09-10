package com.marchnetworks.management.topology.model;

import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.topology.data.Resource;

public abstract class AbstractResourceFactory implements ResourceFactory
{
	public abstract ResourceEntity newResource( Resource paramResource ) throws TopologyException;

	public void onCreate( ResourceEntity resource ) throws TopologyException
	{
	}

	public void onRemove( Resource resource ) throws TopologyException
	{
	}

	public void onCreateAssociation( ResourceEntity resource, ResourceEntity parentResource ) throws TopologyException
	{
	}
}

