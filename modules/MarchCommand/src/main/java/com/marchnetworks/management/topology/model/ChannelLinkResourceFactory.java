package com.marchnetworks.management.topology.model;

import com.marchnetworks.command.common.device.data.ChannelView;
import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.topology.TopologyExceptionTypeEnum;
import com.marchnetworks.command.common.topology.data.ChannelLinkResource;
import com.marchnetworks.command.common.topology.data.ChannelResource;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;

import java.util.ArrayList;
import java.util.List;

public class ChannelLinkResourceFactory extends AbstractResourceFactory
{
	public ResourceEntity newResource( Resource resourceData ) throws TopologyException
	{
		ChannelLinkResource channelLinkDTO = ( ChannelLinkResource ) resourceData;

		if ( ( channelLinkDTO.getLinkedResourceIds() == null ) || ( channelLinkDTO.getLinkedResourceIds().length == 0 ) )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, "Channel links should have a linkedId set." );
		}
		ResourceTopologyServiceIF topologyService = ( ResourceTopologyServiceIF ) ApplicationContextSupport.getBean( "resourceTopologyService_internal" );
		if ( topologyService.getDeviceResource( channelLinkDTO.getDeviceResourceId() ) == null )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, "DeviceResourceId set is invalid." );
		}

		Long[] ids = channelLinkDTO.getLinkedResourceIds();
		Resource r = topologyService.getResource( ids[0], 0 );
		if ( ( r instanceof ChannelResource ) )
		{
			ChannelResource cr = ( ChannelResource ) r;
			String[] assocIds = cr.getChannelView().getAssocIds();

			if ( assocIds.length > 0 )
			{
				List<Long> newIds = new ArrayList( 4 );

				newIds.add( ids[0] );

				DeviceResource devResource = topologyService.getDeviceResource( channelLinkDTO.getDeviceResourceId() );
				for ( String channelId : assocIds )
				{
					ChannelResource channelResource = topologyService.getChannelResource( devResource.getDeviceId(), channelId );
					if ( channelResource != null )
					{
						newIds.add( channelResource.getId() );
					}
				}

				channelLinkDTO.setLinkedResourceIds( ( Long[] ) newIds.toArray( new Long[newIds.size()] ) );
			}
		}

		ChannelLinkResourceEntity channelLinkEntity = new ChannelLinkResourceEntity( channelLinkDTO );
		return channelLinkEntity;
	}

	public void onCreateAssociation( ResourceEntity resource, ResourceEntity parentResource ) throws TopologyException
	{
		if ( ( !( parentResource instanceof GroupEntity ) ) && ( !( parentResource instanceof GenericResourceEntity ) ) )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, "Channel link " + resource.getIdAsString() + " cannot be put under " + parentResource.getName() );
		}
	}
}

