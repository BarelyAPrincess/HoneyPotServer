package com.marchnetworks.command.common.topology.data;

import javax.xml.bind.annotation.XmlElement;

public class ContainerItem
{
	private Long id;
	private Long deviceResourceId;
	private LinkType linktype;
	private String metaData;

	public ContainerItem()
	{
	}

	public ContainerItem( Long id, String metaData )
	{
		this.id = id;
		this.metaData = metaData;
	}

	@XmlElement( required = true )
	public Long getId()
	{
		return id;
	}

	public void setId( Long id )
	{
		this.id = id;
	}

	@XmlElement( required = true, nillable = true )
	public Long getDeviceResourceId()
	{
		return deviceResourceId;
	}

	public void setDeviceResourceId( Long deviceResourceId )
	{
		this.deviceResourceId = deviceResourceId;
	}

	@XmlElement( required = true, nillable = true )
	public LinkType getLinktype()
	{
		return linktype;
	}

	public void setLinktype( LinkType linktype )
	{
		this.linktype = linktype;
	}

	public String getMetaData()
	{
		return metaData;
	}

	public void setMetaData( String metaData )
	{
		this.metaData = metaData;
	}
}
