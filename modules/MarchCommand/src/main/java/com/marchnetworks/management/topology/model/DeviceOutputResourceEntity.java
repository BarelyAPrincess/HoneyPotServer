package com.marchnetworks.management.topology.model;

import com.marchnetworks.command.common.device.data.AudioOutputView;
import com.marchnetworks.command.common.device.data.DeviceOutputView;
import com.marchnetworks.command.common.device.data.SwitchView;
import com.marchnetworks.command.common.topology.data.AudioOutputResource;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.command.common.topology.data.SwitchResource;
import com.marchnetworks.management.instrumentation.model.DeviceOutputEntity;
import com.marchnetworks.management.instrumentation.model.DeviceOutputMBean;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table( name = "DEVICE_OUTPUT_RESOURCE" )
public class DeviceOutputResourceEntity extends ResourceEntity
{
	@OneToOne( cascade = {javax.persistence.CascadeType.DETACH}, targetEntity = DeviceOutputEntity.class )
	@JoinColumn( name = "DEVICE_OUTPUT", nullable = false, unique = true )
	protected DeviceOutputMBean deviceOutputEntity;

	public DeviceOutputResourceEntity()
	{
	}

	public DeviceOutputResourceEntity( Resource deviceOutputResource )
	{
		super( deviceOutputResource );
	}

	public DeviceOutputMBean getDeviceOutputEntity()
	{
		return deviceOutputEntity;
	}

	public void setDeviceOutputEntity( DeviceOutputMBean deviceOutputEntity )
	{
		this.deviceOutputEntity = deviceOutputEntity;
	}

	protected Resource newDataObject()
	{
		DeviceOutputView outputView = deviceOutputEntity.toDataObject();
		Resource outputResource = null;
		if ( ( outputView instanceof AudioOutputView ) )
		{
			AudioOutputView audioOutputView = ( AudioOutputView ) outputView;

			outputResource = new AudioOutputResource();
			( ( AudioOutputResource ) outputResource ).setAudioOutputView( audioOutputView );
			( ( AudioOutputResource ) outputResource ).setAudioOutputId( deviceOutputEntity.getId() );
		}
		else if ( ( outputView instanceof SwitchView ) )
		{
			SwitchView switchView = ( SwitchView ) outputView;

			outputResource = new SwitchResource();
			( ( SwitchResource ) outputResource ).setSwitchView( switchView );
			( ( SwitchResource ) outputResource ).setSwitchId( deviceOutputEntity.getId() );
		}
		return outputResource;
	}

	public Class<? extends Resource> getDataObjectClass()
	{
		if ( deviceOutputEntity.getDataObjectClass() == AudioOutputView.class )
		{
			return AudioOutputResource.class;
		}
		if ( deviceOutputEntity.getDataObjectClass() == SwitchView.class )
		{
			return SwitchResource.class;
		}
		return null;
	}
}

