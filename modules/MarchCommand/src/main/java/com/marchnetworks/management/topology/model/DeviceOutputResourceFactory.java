package com.marchnetworks.management.topology.model;

import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.topology.TopologyExceptionTypeEnum;
import com.marchnetworks.command.common.topology.data.AudioOutputResource;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.command.common.topology.data.SwitchResource;
import com.marchnetworks.management.instrumentation.AudioOutputService;
import com.marchnetworks.management.instrumentation.DeviceSwitchService;
import com.marchnetworks.management.instrumentation.model.DeviceOutputMBean;

public class DeviceOutputResourceFactory extends AbstractResourceFactory
{
	private AudioOutputService audioOutputService;
	private DeviceSwitchService switchService;

	public ResourceEntity newResource( Resource resourceData ) throws TopologyException
	{
		DeviceOutputResourceEntity resource = null;
		DeviceOutputMBean deviceOutputEntity = null;

		if ( ( resourceData instanceof AudioOutputResource ) )
		{
			AudioOutputResource audioOutputRes = ( AudioOutputResource ) resourceData;
			if ( audioOutputRes.getAudioOutputId() == null )
			{
				throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, "Audio output id is not set." );
			}

			deviceOutputEntity = audioOutputService.getDeviceOutputById( audioOutputRes.getAudioOutputId() );
			if ( deviceOutputEntity == null )
			{
				throw new TopologyException( TopologyExceptionTypeEnum.AUDIO_OUTPUT_NOT_FOUND );
			}
		}
		else if ( ( resourceData instanceof SwitchResource ) )
		{
			SwitchResource switchRes = ( SwitchResource ) resourceData;
			if ( switchRes.getSwitchId() == null )
			{
				throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, "switch id is not set." );
			}
			deviceOutputEntity = switchService.getDeviceOutputById( switchRes.getSwitchId() );
			if ( deviceOutputEntity == null )
			{
				throw new TopologyException( TopologyExceptionTypeEnum.SWITCH_NOT_FOUND );
			}
		}
		resource = new DeviceOutputResourceEntity( resourceData );
		resource.setDeviceOutputEntity( deviceOutputEntity );
		resource.setName( deviceOutputEntity.getName() );
		return resource;
	}

	public void onCreateAssociation( ResourceEntity resource, ResourceEntity parentResource ) throws TopologyException
	{
		if ( !( parentResource instanceof DeviceResourceEntity ) )
			throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, "Output Resource " + resource.getIdAsString() + " cannot be put under " + parentResource.getName() );
	}

	public void setAudioOutputService( AudioOutputService audioOutputService )
	{
		this.audioOutputService = audioOutputService;
	}

	public void setSwitchService( DeviceSwitchService switchService )
	{
		this.switchService = switchService;
	}
}

