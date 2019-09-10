package com.marchnetworks.management.topology.model;

import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.topology.TopologyExceptionTypeEnum;
import com.marchnetworks.command.common.topology.data.GenericLinkResource;
import com.marchnetworks.command.common.topology.data.Resource;

import java.util.UUID;

public class GenericLinkResourceFactory extends AbstractResourceFactory
{
	private static final int GENERIC_METADATA_MAX_SIZE = 4000;

	public ResourceEntity newResource( Resource resourceData ) throws TopologyException
	{
		GenericLinkResource genericLinkResource = ( GenericLinkResource ) resourceData;
		if ( ( !genericLinkResource.isContainer() ) && ( ( genericLinkResource.getLinkedResourceIds() == null ) || ( genericLinkResource.getLinkedResourceIds().length == 0 ) ) )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, "Link resources should have a linkedId set." );
		}
		if ( ( genericLinkResource.getMetaData() != null ) && ( genericLinkResource.getMetaData().length > 4000 ) )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, "Size of metaData exceeds 4000 bytes." );
		}
		if ( genericLinkResource.getOwner() == null )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, "GenericLinkResource must specify owner GUID" );
		}
		try
		{
			UUID.fromString( genericLinkResource.getOwner() );
		}
		catch ( IllegalArgumentException e )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, "GenericLinkResource owner GUID not in correct format: " + genericLinkResource.getOwner() );
		}
		GenericLinkResourceEntity resource = new GenericLinkResourceEntity( genericLinkResource );
		return resource;
	}
}

