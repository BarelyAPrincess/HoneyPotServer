package com.marchnetworks.command.common.topology.data;

import javax.xml.bind.annotation.XmlElement;

public class ChannelLinkResource extends LinkResource
{
	private String channelId;
	private String metaData;
	private Long deviceResourceId;

	public void update( Resource updatedResource )
	{
		if ( ( updatedResource instanceof ChannelLinkResource ) )
		{
			super.update( updatedResource );
			ChannelLinkResource channelLink = ( ChannelLinkResource ) updatedResource;

			setMetaData( channelLink.getMetaData() );
		}
	}

	public String getMetaData()
	{
		return metaData;
	}

	public void setMetaData( String metaData )
	{
		this.metaData = metaData;
	}

	public String getChannelId()
	{
		return channelId;
	}

	public void setChannelId( String channelId )
	{
		this.channelId = channelId;
	}

	@XmlElement( required = true )
	public Long getDeviceResourceId()
	{
		return deviceResourceId;
	}

	public void setDeviceResourceId( Long deviceResourceId )
	{
		this.deviceResourceId = deviceResourceId;
	}
}
