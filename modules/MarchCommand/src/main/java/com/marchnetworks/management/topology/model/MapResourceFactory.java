package com.marchnetworks.management.topology.model;

import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.topology.TopologyExceptionTypeEnum;
import com.marchnetworks.command.common.topology.data.MapResource;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.map.service.MapService;

public class MapResourceFactory extends AbstractResourceFactory
{
	private MapService mapService;

	public ResourceEntity newResource( Resource resourceData ) throws TopologyException
	{
		MapResource mapResource = ( MapResource ) resourceData;
		MapResourceEntity resource = new MapResourceEntity( mapResource );
		return resource;
	}

	public void onCreate( ResourceEntity resource ) throws TopologyException
	{
		MapResourceEntity mapResource = ( MapResourceEntity ) resource;

		Long mapDataId = mapResource.getMapDataId();
		if ( mapDataId != null )
		{
			mapService.addReference( mapResource.getMapDataId(), mapResource.getId() );
		}
	}

	public void onRemove( Resource resource ) throws TopologyException
	{
		MapResource mapResource = ( MapResource ) resource;
		Long mapDataId = mapResource.getMapDataId();
		if ( mapDataId != null )
		{
			mapService.removeReference( mapResource.getMapDataId(), mapResource.getId() );
		}
	}

	public void onCreateAssociation( ResourceEntity resource, ResourceEntity parentResource ) throws TopologyException
	{
		if ( !( parentResource instanceof GroupEntity ) )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, "Map resource " + resource.getIdAsString() + " cannot be put under " + parentResource.getName() );
		}
	}

	public void setMapService( MapService mapService )
	{
		this.mapService = mapService;
	}
}

