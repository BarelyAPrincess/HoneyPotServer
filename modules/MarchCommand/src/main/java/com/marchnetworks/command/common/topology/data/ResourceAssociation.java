package com.marchnetworks.command.common.topology.data;

import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

public class ResourceAssociation
{
	private Long resourceId;
	private Long parentResourceId;
	private transient Resource resource;
	private transient Resource parentResource;
	private String associationType;

	public ResourceAssociation()
	{
	}

	public ResourceAssociation( String resourceId, String parentResourceId, String associationType )
	{
		this.resourceId = Long.valueOf( resourceId );
		this.parentResourceId = Long.valueOf( parentResourceId );
		this.associationType = associationType;
	}

	public ResourceAssociation( Long resourceId, Long parentResourceId, String associationType )
	{
		this.resourceId = resourceId;
		this.parentResourceId = parentResourceId;
		this.associationType = associationType;
	}

	public ResourceAssociation( Resource resource, Resource parentResource, String associationType )
	{
		setResource( resource );
		setParentResource( parentResource );
		this.associationType = associationType;
	}

	@XmlTransient
	public String getResourceIdAsString()
	{
		return String.valueOf( resourceId );
	}

	@XmlElement( required = true )
	public Long getResourceId()
	{
		return resourceId;
	}

	public void setResourceIdAsString( String resourceId )
	{
		this.resourceId = Long.valueOf( resourceId );
	}

	public void setResourceId( Long resourceId )
	{
		this.resourceId = resourceId;
	}

	@XmlTransient
	public String getParentResourceIdAsString()
	{
		return String.valueOf( parentResourceId );
	}

	@XmlElement( required = true )
	public Long getParentResourceId()
	{
		return parentResourceId;
	}

	public void setParentResourceIdAsString( String parentResourceId )
	{
		this.parentResourceId = Long.valueOf( parentResourceId );
	}

	public void setParentResourceId( Long parentResourceId )
	{
		this.parentResourceId = parentResourceId;
	}

	@XmlElement( required = true )
	public String getAssociationType()
	{
		return associationType;
	}

	public void setAssociationType( String associationType )
	{
		this.associationType = associationType;
	}

	@XmlTransient
	public Resource getResource()
	{
		return resource;
	}

	public void setResource( Resource resource )
	{
		this.resource = resource;
		resourceId = resource.getId();
	}

	@XmlTransient
	public Resource getParentResource()
	{
		return parentResource;
	}

	public void setParentResource( Resource parentResource )
	{
		this.parentResource = parentResource;
		parentResourceId = parentResource.getId();
	}

	public String toString()
	{
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		JAXB.marshal( this, bout );
		return bout.toString().replace( "\n", "" );
	}

	public int hashCode()
	{
		int prime = 31;
		int result = 1;
		result = 31 * result + ( associationType == null ? 0 : associationType.hashCode() );
		result = 31 * result + ( parentResourceId == null ? 0 : parentResourceId.hashCode() );
		result = 31 * result + ( resourceId == null ? 0 : resourceId.hashCode() );
		return result;
	}

	public boolean equals( Object obj )
	{
		if ( this == obj )
			return true;
		if ( obj == null )
			return false;
		if ( getClass() != obj.getClass() )
			return false;
		ResourceAssociation other = ( ResourceAssociation ) obj;
		if ( associationType == null )
		{
			if ( associationType != null )
				return false;
		}
		else if ( !associationType.equals( associationType ) )
			return false;
		if ( parentResourceId == null )
		{
			if ( parentResourceId != null )
				return false;
		}
		else if ( !parentResourceId.equals( parentResourceId ) )
			return false;
		if ( resourceId == null )
		{
			if ( resourceId != null )
				return false;
		}
		else if ( !resourceId.equals( resourceId ) )
			return false;
		return true;
	}
}
