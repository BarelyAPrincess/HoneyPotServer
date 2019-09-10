package com.marchnetworks.command.common.topology.data;

public class DataResource extends Resource
{
	private String clob;

	private byte[] blob;

	private String dataType;

	public void update( Resource updatedResource )
	{
		if ( ( updatedResource instanceof DataResource ) )
		{
			super.update( updatedResource );
			DataResource updatedDataResource = ( DataResource ) updatedResource;
			clob = updatedDataResource.getClob();
			blob = updatedDataResource.getBlob();
			dataType = updatedDataResource.getDataType();
		}
	}

	public String getClob()
	{
		return clob;
	}

	public void setClob( String clob )
	{
		this.clob = clob;
	}

	public byte[] getBlob()
	{
		return blob;
	}

	public void setBlob( byte[] blob )
	{
		this.blob = blob;
	}

	public String getDataType()
	{
		return dataType;
	}

	public void setDataType( String dataType )
	{
		this.dataType = dataType;
	}

	public boolean hasData()
	{
		return ( ( clob != null ) || ( blob != null ) ? Boolean.TRUE : Boolean.FALSE ).booleanValue();
	}
}
