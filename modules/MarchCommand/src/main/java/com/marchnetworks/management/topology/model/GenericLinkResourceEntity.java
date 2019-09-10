package com.marchnetworks.management.topology.model;

import com.marchnetworks.command.common.topology.data.GenericLinkResource;
import com.marchnetworks.command.common.topology.data.Resource;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table( name = "GENERIC_LINK_RESOURCE" )
public class GenericLinkResourceEntity extends LinkResourceEntity
{
	@Column( name = "TYPE" )
	protected String type;
	@Lob
	@Column( name = "META_DATA" )
	private byte[] metaData;
	@Column( name = "OWNER", nullable = false )
	protected String owner;
	@Column( name = "IS_CONTAINER" )
	private boolean isContainer = false;

	@Column( name = "APP_SPECIFIC_ID" )
	protected String appSpecificId;

	public GenericLinkResourceEntity()
	{
	}

	public GenericLinkResourceEntity( GenericLinkResource dataObject )
	{
		super( dataObject );
		metaData = dataObject.getMetaData();
		type = dataObject.getType();
		owner = dataObject.getOwner();
		isContainer = dataObject.isContainer();
		appSpecificId = dataObject.getAppSpecificId();
	}

	protected Resource newDataObject()
	{
		GenericLinkResource ret = new GenericLinkResource();
		super.newDataObject( ret );
		ret.setMetaData( metaData );
		ret.setType( type );
		ret.setOwner( owner );
		ret.setContainer( isContainer );
		ret.setAppSpecificId( appSpecificId );
		return ret;
	}

	public void readFromDataObject( Resource dataObject )
	{
		super.readFromDataObject( dataObject );
		GenericLinkResource genericLinkResource = ( GenericLinkResource ) dataObject;
		metaData = genericLinkResource.getMetaData();
		appSpecificId = genericLinkResource.getAppSpecificId();
	}

	public Class<GenericLinkResource> getDataObjectClass()
	{
		return GenericLinkResource.class;
	}

	protected byte[] getMetaData()
	{
		return metaData;
	}

	protected void setMetaData( byte[] metaData )
	{
		this.metaData = metaData;
	}

	public boolean isContainer()
	{
		return isContainer;
	}

	public void setContainer( boolean isContainer )
	{
		this.isContainer = isContainer;
	}
}

