package com.marchnetworks.management.topology.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table( name = "RESOURCE_ASSOCIATION", uniqueConstraints = {@javax.persistence.UniqueConstraint( columnNames = {"FIRST_RESOURCE", "SECOND_RESOURCE", "TYPE"} )} )
public class ResourceAssociationEntity
{
	@Id
	@GeneratedValue
	@Column( name = "ID" )
	private Long id;
	@ManyToOne( cascade = {javax.persistence.CascadeType.DETACH} )
	@JoinColumn( name = "FIRST_RESOURCE", nullable = false )
	private ResourceEntity firstResource;
	@ManyToOne( cascade = {javax.persistence.CascadeType.DETACH} )
	@JoinColumn( name = "SECOND_RESOURCE", nullable = false )
	private ResourceEntity secondResource;
	@Column( name = "TYPE", nullable = false )
	private String type;

	public ResourceAssociationEntity()
	{
	}

	public ResourceAssociationEntity( ResourceEntity firstResource, ResourceEntity secondResource, String type )
	{
		this.firstResource = firstResource;
		this.secondResource = secondResource;
		this.type = type;
	}

	public Long getId()
	{
		return id;
	}

	public void setId( Long id )
	{
		this.id = id;
	}

	public ResourceEntity getFirstResource()
	{
		return firstResource;
	}

	public void setFirstResource( ResourceEntity firstResource )
	{
		this.firstResource = firstResource;
	}

	public ResourceEntity getSecondResource()
	{
		return secondResource;
	}

	public void setSecondResource( ResourceEntity secondResource )
	{
		this.secondResource = secondResource;
	}

	public String getType()
	{
		return type;
	}

	public void setType( String type )
	{
		this.type = type;
	}

	public int hashCode()
	{
		int prime = 31;
		int result = 1;
		result = 31 * result + ( firstResource == null ? 0 : firstResource.hashCode() );
		result = 31 * result + ( secondResource == null ? 0 : secondResource.hashCode() );
		result = 31 * result + ( type == null ? 0 : type.hashCode() );
		return result;
	}

	public boolean equals( Object obj )
	{
		if ( this == obj )
		{
			return true;
		}
		if ( obj == null )
		{
			return false;
		}
		if ( getClass() != obj.getClass() )
		{
			return false;
		}
		ResourceAssociationEntity other = ( ResourceAssociationEntity ) obj;
		if ( firstResource == null )
		{
			if ( firstResource != null )
			{
				return false;
			}
		}
		else if ( !firstResource.equals( firstResource ) )
		{
			return false;
		}
		if ( secondResource == null )
		{
			if ( secondResource != null )
			{
				return false;
			}
		}
		else if ( !secondResource.equals( secondResource ) )
		{
			return false;
		}
		if ( type == null )
		{
			if ( type != null )
			{
				return false;
			}
		}
		else if ( !type.equals( type ) )
		{
			return false;
		}
		return true;
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append( "ResourceAssociationEntity [id=" ).append( id ).append( ", firstResource=" ).append( firstResource ).append( ", secondResource=" ).append( secondResource ).append( ", type=" ).append( type ).append( "]" );

		return builder.toString();
	}
}

