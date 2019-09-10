package com.marchnetworks.management.topology.model;

import com.marchnetworks.command.common.topology.data.DefaultRootResource;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table( name = "DEFAULT_ROOT_RESOURCE" )
public class DefaultRootResourceEntity
{
	@Id
	@Column( name = "ID" )
	private String id;
	@OneToOne
	@javax.persistence.JoinColumn( name = "RESOURCE" )
	private ResourceEntity resource;

	public String getId()
	{
		return id;
	}

	public void setId( String id )
	{
		this.id = id;
	}

	public ResourceEntity getResource()
	{
		return resource;
	}

	public void setResource( ResourceEntity resource )
	{
		this.resource = resource;
	}

	public DefaultRootResource toDataObject()
	{
		DefaultRootResource ret = new DefaultRootResource();
		ret.setKey( getId() );
		ret.setResource( resource.toDataObject() );

		return ret;
	}
}

