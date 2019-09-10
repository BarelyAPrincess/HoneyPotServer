package com.marchnetworks.management.topology.model;

import com.marchnetworks.app.service.OsgiService;
import com.marchnetworks.command.api.topology.GenericResourceCoreFactory;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.topology.TopologyExceptionTypeEnum;
import com.marchnetworks.command.common.topology.data.GenericResource;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.common.spring.ApplicationContextSupport;

public class GenericResourceFactory extends AbstractResourceFactory
{
	private static final int GENERIC_VALUE_MAX_SIZE = 4000;

	public ResourceEntity newResource( Resource resourceData ) throws TopologyException
	{
		GenericResource genericResource = ( GenericResource ) resourceData;

		if ( ( genericResource.getValue() != null ) && ( genericResource.getValue().length > 4000 ) )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, "Size of value exceeds 4000 bytes." );
		}

		if ( CommonAppUtils.isNullOrEmptyString( genericResource.getOwner() ) )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, "GenericResource must specify owner GUID" );
		}

		GenericResourceEntity resource = new GenericResourceEntity( genericResource );
		return resource;
	}

	public void onRemove( Resource resource ) throws TopologyException
	{
		GenericResource genericResource = ( GenericResource ) resource;

		OsgiService osgiService = ( OsgiService ) ApplicationContextSupport.getBean( "osgiManager" );

		GenericResourceCoreFactory appResourceFactory = ( GenericResourceCoreFactory ) osgiService.getService( GenericResourceCoreFactory.class, genericResource.getOwner() );
		if ( appResourceFactory != null )
		{
			appResourceFactory.onRemove( genericResource );
		}
	}

	public void onCreateAssociation( ResourceEntity resource, ResourceEntity parentResource ) throws TopologyException
	{
		if ( ( !( parentResource instanceof GroupEntity ) ) && ( !( parentResource instanceof GenericResourceEntity ) ) )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, "Generic Resource " + resource.getIdAsString() + " cannot be put under " + parentResource.getName() );
		}
	}
}

