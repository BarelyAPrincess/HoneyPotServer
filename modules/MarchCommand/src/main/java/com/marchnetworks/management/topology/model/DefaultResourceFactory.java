package com.marchnetworks.management.topology.model;

import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.topology.TopologyExceptionTypeEnum;
import com.marchnetworks.command.common.topology.data.AlarmSourceLinkResource;
import com.marchnetworks.command.common.topology.data.DataResource;
import com.marchnetworks.command.common.topology.data.Group;
import com.marchnetworks.command.common.topology.data.Resource;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class DefaultResourceFactory implements ResourceFactory
{
	private Map<Class<? extends Resource>, Constructor<? extends ResourceEntity>> dataTypeMap;
	private Map<String, ResourceFactory> customizedFactories = new HashMap();

	public void init() throws SecurityException, NoSuchMethodException
	{
		dataTypeMap = new HashMap();
		dataTypeMap.put( Group.class, GroupEntity.class.getConstructor( new Class[] {Group.class} ) );
		dataTypeMap.put( DataResource.class, DataResourceEntity.class.getConstructor( new Class[] {DataResource.class} ) );
		dataTypeMap.put( AlarmSourceLinkResource.class, AlarmSourceLinkResourceEntity.class.getConstructor( new Class[] {AlarmSourceLinkResource.class} ) );
	}

	public ResourceEntity newResource( Resource resourceData ) throws TopologyException
	{
		ResourceEntity resource = null;
		Constructor<? extends ResourceEntity> constructor = ( Constructor ) dataTypeMap.get( resourceData.getClass() );

		if ( constructor != null )
		{
			try
			{
				resource = ( ResourceEntity ) constructor.newInstance( new Object[] {resourceData} );
			}
			catch ( IllegalArgumentException e )
			{
				throw new RuntimeException( e );
			}
			catch ( InstantiationException e )
			{
				throw new RuntimeException( e );
			}
			catch ( IllegalAccessException e )
			{
				throw new RuntimeException( e );
			}
			catch ( InvocationTargetException e )
			{
				throw new RuntimeException( e );
			}
		}
		else
		{
			ResourceFactory factory = ( ResourceFactory ) customizedFactories.get( resourceData.getClass().getName() );
			if ( factory != null )
			{
				resource = factory.newResource( resourceData );
			}
		}
		if ( resource == null )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.INTERNAL_ERROR, "No factory configured for resource type " + resourceData.getClass().getName() );
		}
		return resource;
	}

	public void onCreate( ResourceEntity resource ) throws TopologyException
	{
		ResourceFactory factory = ( ResourceFactory ) customizedFactories.get( resource.getDataObjectClass().getName() );
		if ( factory != null )
		{
			factory.onCreate( resource );
		}
	}

	public void onRemove( Resource resource ) throws TopologyException
	{
		ResourceFactory factory = ( ResourceFactory ) customizedFactories.get( resource.getClass().getName() );
		if ( factory != null )
		{
			factory.onRemove( resource );
		}
	}

	public void onCreateAssociation( ResourceEntity resource, ResourceEntity parentResource ) throws TopologyException
	{
		ResourceFactory factory = ( ResourceFactory ) customizedFactories.get( resource.getDataObjectClass().getName() );
		if ( factory != null )
			factory.onCreateAssociation( resource, parentResource );
	}

	public Map<String, ResourceFactory> getCustomizedFactories()
	{
		return customizedFactories;
	}

	public void setCustomizedFactories( Map<String, ResourceFactory> customizedFactories )
	{
		this.customizedFactories = customizedFactories;
	}
}

