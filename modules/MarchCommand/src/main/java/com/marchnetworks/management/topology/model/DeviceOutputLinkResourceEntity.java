package com.marchnetworks.management.topology.model;

import com.marchnetworks.command.common.topology.data.AudioOutputLinkResource;
import com.marchnetworks.command.common.topology.data.LinkResource;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.command.common.topology.data.SwitchLinkResource;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table( name = "DEVICE_OUTPUT_LINK_RESOURCE" )
public class DeviceOutputLinkResourceEntity extends LinkResourceEntity
{
	@Column( name = "DISCRIMINATOR" )
	private String outputLinkType;
	@Column( name = "OUTPUT_ID" )
	private String outputId;
	@Column( name = "DEVICE_RESOURCE_ID" )
	private Long deviceResourceId;

	public DeviceOutputLinkResourceEntity()
	{
	}

	public DeviceOutputLinkResourceEntity( AudioOutputLinkResource dataObject )
	{
		super( dataObject );
		outputId = dataObject.getAudioOutputId();
		deviceResourceId = dataObject.getDeviceResourceId();
		outputLinkType = AudioOutputLinkResource.class.getSimpleName();
	}

	public DeviceOutputLinkResourceEntity( SwitchLinkResource dataObject )
	{
		super( dataObject );
		outputId = dataObject.getSwitchId();
		deviceResourceId = dataObject.getDeviceResourceId();
		outputLinkType = SwitchLinkResource.class.getSimpleName();
	}

	public boolean isContainer()
	{
		return false;
	}

	protected Resource newDataObject()
	{
		LinkResource deviceOutputLinkResource = null;
		if ( getOutputLinkType().equals( AudioOutputLinkResource.class.getSimpleName() ) )
		{
			deviceOutputLinkResource = new AudioOutputLinkResource();
			( ( AudioOutputLinkResource ) deviceOutputLinkResource ).setAudioOutputId( outputId );
			( ( AudioOutputLinkResource ) deviceOutputLinkResource ).setDeviceResourceId( deviceResourceId );
		}
		else if ( getOutputLinkType().equals( SwitchLinkResource.class.getSimpleName() ) )
		{
			deviceOutputLinkResource = new SwitchLinkResource();
			( ( SwitchLinkResource ) deviceOutputLinkResource ).setSwitchId( outputId );
			( ( SwitchLinkResource ) deviceOutputLinkResource ).setDeviceResourceId( deviceResourceId );
		}

		super.newDataObject( deviceOutputLinkResource );
		return deviceOutputLinkResource;
	}

	public Class<? extends LinkResource> getDataObjectClass()
	{
		if ( getOutputLinkType().equals( AudioOutputLinkResource.class.getSimpleName() ) )
		{
			return AudioOutputLinkResource.class;
		}
		if ( getOutputLinkType().equals( SwitchLinkResource.class.getSimpleName() ) )
		{
			return SwitchLinkResource.class;
		}
		return null;
	}

	public String getOutputId()
	{
		return outputId;
	}

	public void setOutputId( String outputId )
	{
		this.outputId = outputId;
	}

	public Long getDeviceResourceId()
	{
		return deviceResourceId;
	}

	public void setDeviceResourceId( Long deviceResourceId )
	{
		this.deviceResourceId = deviceResourceId;
	}

	public String getOutputLinkType()
	{
		return outputLinkType;
	}

	public void setOutputLinkType( String outputLinkType )
	{
		this.outputLinkType = outputLinkType;
	}
}

