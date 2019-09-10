package com.marchnetworks.management.topology.model;

import com.marchnetworks.command.common.device.data.ChannelView;
import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.topology.TopologyExceptionTypeEnum;
import com.marchnetworks.command.common.topology.data.ChannelResource;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.management.instrumentation.DeviceRegistry;
import com.marchnetworks.management.instrumentation.model.ChannelMBean;

public class ChannelResourceFactory extends AbstractResourceFactory
{
	private DeviceRegistry deviceRegistry;

	public ResourceEntity newResource( Resource resourceData ) throws TopologyException
	{
		ChannelResourceEntity resource = new ChannelResourceEntity( ( ChannelResource ) resourceData );

		Long channelId = ( ( ChannelResource ) resourceData ).getChannelView().getId();

		if ( channelId != null )
		{
			ChannelMBean channel = deviceRegistry.getChannel( channelId );
			if ( channel == null )
				throw new TopologyException( TopologyExceptionTypeEnum.CHANNEL_NOT_FOUND );
			resource.setChannel( channel );
			resource.setName( channel.getName() );
		}
		else
		{
			throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, "Null Channel id." );
		}
		return resource;
	}

	public void onCreateAssociation( ResourceEntity resource, ResourceEntity parentResource ) throws TopologyException
	{
		if ( !( parentResource instanceof DeviceResourceEntity ) )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, "Channel Resource " + resource.getIdAsString() + " cannot be put under " + parentResource.getName() );
		}
	}

	public DeviceRegistry getDeviceRegistry()
	{
		return deviceRegistry;
	}

	public void setDeviceRegistry( DeviceRegistry deviceRegistry )
	{
		this.deviceRegistry = deviceRegistry;
	}
}

