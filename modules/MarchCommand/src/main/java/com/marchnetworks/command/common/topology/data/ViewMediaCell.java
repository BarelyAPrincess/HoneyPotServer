package com.marchnetworks.command.common.topology.data;

import javax.xml.bind.annotation.XmlElement;

public class ViewMediaCell
{
	private String cellMetaData;
	private String channelId;
	private Long deviceResourceId;

	public String getCellMetaData()
	{
		return cellMetaData;
	}

	public void setCellMetaData( String cellMetaData )
	{
		this.cellMetaData = cellMetaData;
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
