package com.marchnetworks.management.topology.model;

import com.marchnetworks.command.common.topology.data.GenericResource;
import com.marchnetworks.command.common.topology.data.Resource;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table( name = "GENERIC_RESOURCE" )
public class GenericResourceEntity extends ResourceEntity
{
	@Column( name = "TYPE", nullable = false )
	protected String type;
	@Lob
	@Column( name = "VALUE" )
	protected byte[] value;
	@Column( name = "OWNER", nullable = false )
	protected String owner;
	@Column( name = "APP_SPECIFIC_ID" )
	protected String appSpecificId;

	public GenericResourceEntity()
	{
	}

	public GenericResourceEntity( GenericResource genericResource )
	{
		super( genericResource );
		type = genericResource.getType();
		value = genericResource.getValue();
		owner = genericResource.getOwner();
		appSpecificId = genericResource.getAppSpecificId();
	}

	protected Resource newDataObject()
	{
		GenericResource ret = new GenericResource();
		ret.setType( type );
		ret.setValue( value );
		ret.setOwner( owner );
		ret.setAppSpecificId( appSpecificId );
		return ret;
	}

	public void readFromDataObject( Resource dataObject )
	{
		super.readFromDataObject( dataObject );
		GenericResource genericResource = ( GenericResource ) dataObject;
		value = genericResource.getValue();
		appSpecificId = genericResource.getAppSpecificId();
	}

	public Class<GenericResource> getDataObjectClass()
	{
		return GenericResource.class;
	}

	public String getType()
	{
		return type;
	}

	public byte[] getValue()
	{
		return value;
	}

	public String getOwner()
	{
		return owner;
	}

	public String getAppSpecificId()
	{
		return appSpecificId;
	}
}

