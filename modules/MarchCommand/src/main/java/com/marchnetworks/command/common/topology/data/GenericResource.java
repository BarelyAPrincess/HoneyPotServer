package com.marchnetworks.command.common.topology.data;

public class GenericResource extends Resource
{
	private String type;

	private byte[] value;

	private String owner;

	private String appSpecificId;

	public void update( Resource updatedResource )
	{
		if ( ( updatedResource instanceof GenericResource ) )
		{
			super.update( updatedResource );
			GenericResource updatedGenericResource = ( GenericResource ) updatedResource;
			type = updatedGenericResource.getType();
			value = updatedGenericResource.getValue();
			owner = updatedGenericResource.getOwner();
			appSpecificId = updatedGenericResource.getAppSpecificId();
		}
	}

	public LinkType getLinkType()
	{
		return LinkType.GENERIC;
	}

	public String getType()
	{
		return type;
	}

	public void setType( String type )
	{
		this.type = type;
	}

	public byte[] getValue()
	{
		return value;
	}

	public void setValue( byte[] value )
	{
		this.value = value;
	}

	public String getOwner()
	{
		return owner;
	}

	public void setOwner( String owner )
	{
		this.owner = owner;
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
