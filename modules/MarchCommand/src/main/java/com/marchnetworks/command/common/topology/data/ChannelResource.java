package com.marchnetworks.command.common.topology.data;

import com.marchnetworks.command.common.device.data.ChannelView;

public class ChannelResource extends Resource
{
	private String channelId;
	private ChannelView channelView;

	public void update( Resource updatedResource )
	{
		if ( ( updatedResource instanceof ChannelResource ) )
		{
			super.update( updatedResource );
			ChannelResource updatedChannelResource = ( ChannelResource ) updatedResource;
			channelId = updatedChannelResource.getChannelId();
			channelView = updatedChannelResource.getChannelView();
		}
	}

	public LinkType getLinkType()
	{
		return LinkType.CHANNEL;
	}

	public String getChannelId()
	{
		return channelId;
	}

	public void setChannelId( String channelId )
	{
		this.channelId = channelId;
	}

	public ChannelView getChannelView()
	{
		return channelView;
	}

	public boolean isVideo()
	{
		return ( channelView.getVideo() != null ) && ( channelView.getVideo().length > 0 );
	}

	public boolean isText()
	{
		return ( channelView.getText() != null ) && ( channelView.getText().length > 0 );
	}

	public void setChannelView( ChannelView channelView )
	{
		this.channelView = channelView;
	}
}
