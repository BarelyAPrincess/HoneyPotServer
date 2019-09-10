package com.marchnetworks.management.topology.model;

import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.topology.TopologyExceptionTypeEnum;
import com.marchnetworks.command.common.topology.data.Group;
import com.marchnetworks.command.common.topology.data.Resource;

public class FolderResourceFactory extends AbstractResourceFactory
{
	public ResourceEntity newResource( Resource resourceData ) throws TopologyException
	{
		return new GroupEntity( ( Group ) resourceData );
	}

	public void onCreateAssociation( ResourceEntity resource, ResourceEntity parentResource ) throws TopologyException
	{
		if ( !( parentResource instanceof GroupEntity ) )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, "Folder Resource " + resource.getIdAsString() + " cannot be put under " + parentResource.getName() );
		}
	}
}

