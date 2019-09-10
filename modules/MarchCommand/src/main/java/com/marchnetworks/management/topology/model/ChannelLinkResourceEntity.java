package com.marchnetworks.management.topology.model;

import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.topology.data.ChannelLinkResource;
import com.marchnetworks.command.common.topology.data.Resource;

import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table( name = "CHANNEL_LINK_RESOURCE" )
public class ChannelLinkResourceEntity extends LinkResourceEntity
{
	@Column( name = "CHANNEL_ID" )
	private String channelId;
	@Column( name = "DEVICE_RESOURCE_ID" )
	private Long deviceResourceId;
	@Lob
	@Column( name = "META_DATA" )
	private byte[] metaData;

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append( "ChannelLinkResourceEntity [channelId=" ).append( channelId ).append( ", deviceResourceId=" ).append( deviceResourceId ).append( ", metaData=" ).append( Arrays.toString( metaData ) ).append( "]" );

		return builder.toString();
	}

	public ChannelLinkResourceEntity()
	{
	}

	public ChannelLinkResourceEntity( ChannelLinkResource channelLinkResource )
	{
		super( channelLinkResource );
		setChannelId( channelLinkResource.getChannelId() );
		setDeviceResourceId( channelLinkResource.getDeviceResourceId() );
		setMetaData( channelLinkResource.getMetaData() );
	}

	protected Resource newDataObject()
	{
		ChannelLinkResource channelLinkDTO = new ChannelLinkResource();
		super.newDataObject( channelLinkDTO );
		channelLinkDTO.setChannelId( channelId );
		channelLinkDTO.setDeviceResourceId( deviceResourceId );
		channelLinkDTO.setMetaData( getMetaData() );
		return channelLinkDTO;
	}

	public Class<ChannelLinkResource> getDataObjectClass()
	{
		return ChannelLinkResource.class;
	}

	public void readFromDataObject( Resource dataObject )
	{
		super.readFromDataObject( dataObject );
		ChannelLinkResource channelLink = ( ChannelLinkResource ) dataObject;

		setMetaData( channelLink.getMetaData() );
	}

	public String getChannelId()
	{
		return channelId;
	}

	public void setChannelId( String channelId )
	{
		this.channelId = channelId;
	}

	public Long getDeviceResourceId()
	{
		return deviceResourceId;
	}

	public void setDeviceResourceId( Long deviceResourceId )
	{
		this.deviceResourceId = deviceResourceId;
	}

	protected String getMetaData()
	{
		return CommonAppUtils.encodeToUTF8String( metaData );
	}

	protected void setMetaData( String metaData )
	{
		this.metaData = CommonAppUtils.encodeStringToBytes( metaData );
	}

	public boolean isContainer()
	{
		return false;
	}
}

