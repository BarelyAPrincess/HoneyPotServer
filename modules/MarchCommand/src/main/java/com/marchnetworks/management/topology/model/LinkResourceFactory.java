package com.marchnetworks.management.topology.model;

import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.topology.TopologyExceptionTypeEnum;
import com.marchnetworks.command.common.topology.data.AudioOutputLinkResource;
import com.marchnetworks.command.common.topology.data.ChannelLinkResource;
import com.marchnetworks.command.common.topology.data.LinkResource;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.command.common.topology.data.SwitchLinkResource;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class LinkResourceFactory extends AbstractResourceFactory
{
	private Map<Class<? extends LinkResource>, Constructor<? extends LinkResourceEntity>> dataTypeMap = new HashMap();

	public void init() throws SecurityException, NoSuchMethodException
	{
		dataTypeMap.put( ChannelLinkResource.class, ChannelLinkResourceEntity.class.getConstructor( new Class[] {ChannelLinkResource.class} ) );
		dataTypeMap.put( SwitchLinkResource.class, DeviceOutputLinkResourceEntity.class.getConstructor( new Class[] {SwitchLinkResource.class} ) );
		dataTypeMap.put( AudioOutputLinkResource.class, DeviceOutputLinkResourceEntity.class.getConstructor( new Class[] {AudioOutputLinkResource.class} ) );
	}

	public ResourceEntity newResource( Resource resourceData ) throws TopologyException
	{
		ResourceEntity resource = null;
		LinkResource linkResourceDTO = ( LinkResource ) resourceData;

		if ( ( linkResourceDTO.getLinkedResourceIds() == null ) || ( linkResourceDTO.getLinkedResourceIds().length == 0 ) )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, "Link resources should have a linkedId set." );
		}

		Constructor<? extends LinkResourceEntity> constructor = ( Constructor ) dataTypeMap.get( resourceData.getClass() );
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
		if ( resource == null )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.INTERNAL_ERROR, "No factory configured for resource type " + resourceData.getClass().getName() );
		}

		return resource;
	}
}

