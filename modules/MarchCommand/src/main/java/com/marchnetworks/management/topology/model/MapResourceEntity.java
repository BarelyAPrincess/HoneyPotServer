package com.marchnetworks.management.topology.model;

import com.marchnetworks.command.common.topology.data.MapResource;
import com.marchnetworks.command.common.topology.data.Resource;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table( name = "MAP_RESOURCE" )
public class MapResourceEntity extends LinkResourceEntity
{
	@Column( name = "MAP_DATA_ID" )
	private Long mapDataId;
	@Column( name = "META_DATA", length = 4000 )
	private byte[] metaData;

	public MapResourceEntity()
	{
	}

	public MapResourceEntity( MapResource dataObject )
	{
		super( dataObject );
	}

	protected Resource newDataObject()
	{
		MapResource ret = new MapResource();
		super.newDataObject( ret );

		ret.setMapDataId( mapDataId );
		ret.setMetaData( metaData );
		return ret;
	}

	public void readFromDataObject( Resource dataObject )
	{
		super.readFromDataObject( dataObject );

		MapResource mapDataObject = ( MapResource ) dataObject;
		mapDataId = mapDataObject.getMapDataId();
		metaData = mapDataObject.getMetaData();
	}

	public Class<MapResource> getDataObjectClass()
	{
		return MapResource.class;
	}

	public Long getMapDataId()
	{
		return mapDataId;
	}

	public void setMapDataId( Long mapDataId )
	{
		this.mapDataId = mapDataId;
	}

	public byte[] getMetaData()
	{
		return metaData;
	}

	public void setMetaData( byte[] metaData )
	{
		this.metaData = metaData;
	}

	public boolean isContainer()
	{
		return true;
	}
}

