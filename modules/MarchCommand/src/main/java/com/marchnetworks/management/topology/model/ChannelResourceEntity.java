package com.marchnetworks.management.topology.model;

import com.marchnetworks.command.common.device.data.ChannelView;
import com.marchnetworks.command.common.topology.data.ChannelResource;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.management.instrumentation.model.Channel;
import com.marchnetworks.management.instrumentation.model.ChannelMBean;
import com.marchnetworks.management.instrumentation.model.Device;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table( name = "CHANNEL_RESOURCE" )
public class ChannelResourceEntity extends ResourceEntity
{
	@OneToOne( cascade = {javax.persistence.CascadeType.DETACH}, targetEntity = Channel.class )
	@JoinColumn( name = "CHANNEL", nullable = false )
	private ChannelMBean channel;

	public ChannelResourceEntity()
	{
	}

	public ChannelResourceEntity( ChannelResource dataObject )
	{
		super( dataObject );
	}

	protected Resource newDataObject()
	{
		ChannelResource channelResourceDTO = new ChannelResource();

		if ( getChannel() != null )
		{
			channelResourceDTO.setChannelId( channel.getChannelId() );

			ChannelView channelView = new ChannelView();
			channelView.setId( Long.valueOf( Long.parseLong( channel.getIdAsString() ) ) );
			channelView.setChannelId( channel.getChannelId() );
			channelView.setChannelName( channel.getName() );
			channelView.setChannelState( channel.getChannelState() );
			channelView.setPtzDomeIdentifier( channel.getPtzDomeIdentifier() );
			channelView.setAssocIds( channel.getAssocIds() );
			Device device = ( ( Channel ) channel ).getDevice();
			if ( device != null )
			{
				channelView.setDeviceId( device.getDeviceId() );
			}

			channelView.setVideo( channel.getVideoEncoders() );
			channelView.setAudio( channel.getAudioEncoders() );
			channelView.setText( channel.getTextEncoders() );
			channelView.setData( channel.getDataEncoders() );

			channelResourceDTO.setChannelView( channelView );
		}
		return channelResourceDTO;
	}

	public Class<ChannelResource> getDataObjectClass()
	{
		return ChannelResource.class;
	}

	public ChannelMBean getChannel()
	{
		return channel;
	}

	public void setChannel( ChannelMBean channel )
	{
		this.channel = channel;
	}
}

