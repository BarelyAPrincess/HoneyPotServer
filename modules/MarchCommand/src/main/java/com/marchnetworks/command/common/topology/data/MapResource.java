package com.marchnetworks.command.common.topology.data;

import javax.xml.bind.annotation.XmlElement;

public class MapResource extends LinkResource
{
	private byte[] metaData;
	private Long mapDataId;

	public void update( Resource updatedResource )
	{
		if ( ( updatedResource instanceof MapResource ) )
		{
			super.update( updatedResource );

			MapResource updatedView = ( MapResource ) updatedResource;
			metaData = updatedView.getMetaData();
			mapDataId = updatedView.getMapDataId();
		}
	}

	public LinkType getLinkType()
	{
		return LinkType.MAP;
	}

	public byte[] getMetaData()
	{
		return metaData;
	}

	public void setMetaData( byte[] metaData )
	{
		this.metaData = metaData;
	}

	@XmlElement( required = true, nillable = true )
	public Long getMapDataId()
	{
		return mapDataId;
	}

	public void setMapDataId( Long mapDataId )
	{
		this.mapDataId = mapDataId;
	}
}
