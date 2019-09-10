package com.marchnetworks.management.topology.model;

import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.topology.data.Resource;

public abstract interface ResourceFactory
{
	public abstract ResourceEntity newResource( Resource paramResource ) throws TopologyException;

	public abstract void onCreate( ResourceEntity paramResourceEntity ) throws TopologyException;

	public abstract void onRemove( Resource paramResource ) throws TopologyException;

	public abstract void onCreateAssociation( ResourceEntity paramResourceEntity1, ResourceEntity paramResourceEntity2 ) throws TopologyException;
}

