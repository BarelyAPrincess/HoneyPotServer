package com.marchnetworks.management.topology.model;

import com.marchnetworks.command.common.topology.data.DataResource;
import com.marchnetworks.command.common.topology.data.Resource;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table( name = "DATA_RESOURCE" )
public class DataResourceEntity extends ResourceEntity
{
	@OneToOne( cascade = {javax.persistence.CascadeType.ALL}, fetch = FetchType.EAGER, orphanRemoval = true, optional = false )
	@JoinColumn( name = "LOB_DATA" )
	private LobDataEntity data = new LobDataEntity();

	public DataResourceEntity()
	{
	}

	public DataResourceEntity( DataResource dataObject )
	{
		this();
		readFromDataObject( dataObject );
	}

	public LobDataEntity getData()
	{
		return data;
	}

	private void setData( LobDataEntity data )
	{
		this.data = data;
	}

	protected Resource newDataObject()
	{
		DataResource dataResource = new DataResource();
		if ( data != null )
		{
			dataResource.setBlob( data.getBlob() );
			dataResource.setClob( data.getClob() );
			dataResource.setDataType( data.getDataType() );
		}
		return dataResource;
	}

	public void readFromDataObject( Resource dataObject )
	{
		super.readFromDataObject( dataObject );

		DataResource dataResourceObj = ( DataResource ) dataObject;
		LobDataEntity lobData = getData();
		if ( ( dataResourceObj != null ) && ( dataResourceObj.hasData() ) )
		{
			lobData.setBlob( dataResourceObj.getBlob() );
			lobData.setClob( dataResourceObj.getClob() );
			lobData.setDataType( dataResourceObj.getDataType() );
		}
		setData( lobData );
	}

	public Class<DataResource> getDataObjectClass()
	{
		return DataResource.class;
	}
}

