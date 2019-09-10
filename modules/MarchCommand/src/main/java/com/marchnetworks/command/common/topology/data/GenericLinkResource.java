package com.marchnetworks.command.common.topology.data;

public class GenericLinkResource extends LinkResource
{
	private String type;

	private byte[] metaData;

	private String owner;

	private boolean isContainer;

	private String appSpecificId;

	public void update( Resource updatedResource )
	{
		super.update( updatedResource );
		if ( ( updatedResource instanceof GenericLinkResource ) )
		{
			GenericLinkResource updatedLinkResource = ( GenericLinkResource ) updatedResource;
			metaData = updatedLinkResource.getMetaData();
		}
	}

	public byte[] getMetaData()
	{
		return metaData;
	}

	public void setMetaData( byte[] metaData )
	{
		this.metaData = metaData;
	}

	public String getType()
	{
		return type;
	}

	public void setType( String type )
	{
		this.type = type;
	}

	public String getOwner()
	{
		return owner;
	}

	public void setOwner( String owner )
	{
		this.owner = owner;
	}

	public boolean isContainer()
	{
		return isContainer;
	}

	public void setContainer( boolean isContainer )
	{
		this.isContainer = isContainer;
	}

	public String getAppSpecificId()
	{
		return appSpecificId;
	}

	public void setAppSpecificId( String appSpecificId )
	{
		this.appSpecificId = appSpecificId;
	}
}
