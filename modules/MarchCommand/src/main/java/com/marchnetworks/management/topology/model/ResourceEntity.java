package com.marchnetworks.management.topology.model;

import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.command.common.topology.data.ResourceAssociation;
import com.marchnetworks.management.topology.util.TopologyCache;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.Type;

@Entity
@Table( name = "RESOURCE" )
@Inheritance( strategy = InheritanceType.JOINED )
public abstract class ResourceEntity
{
	@Id
	@GeneratedValue
	@Column( name = "ID" )
	protected Long id;
	@Column( name = "NAME" )
	protected String name;
	@Column( name = "TIME_CREATED" )
	@Type( type = "com.marchnetworks.common.hibernate.UTCCalendarType" )
	protected Calendar timeCreated;
	@Version
	@Column( name = "VERSION" )
	protected Long version;
	@OneToMany( cascade = {javax.persistence.CascadeType.REMOVE, javax.persistence.CascadeType.DETACH}, orphanRemoval = true, mappedBy = "firstResource" )
	@MapKey( name = "id" )
	protected Map<Long, ResourceAssociationEntity> associationsMap = new HashMap();

	public ResourceEntity()
	{
	}

	public ResourceEntity( Resource dataObject )
	{
		this();
		readFromDataObject( dataObject );
	}

	public String getIdAsString()
	{
		if ( id != null )
		{
			return String.valueOf( id );
		}
		return null;
	}

	public Long getId()
	{
		return id;
	}

	public void setId( Long id )
	{
		this.id = id;
	}

	public void setIdAsString( Long id )
	{
		this.id = Long.valueOf( id.longValue() );
	}

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public Calendar getTimeCreated()
	{
		return timeCreated;
	}

	public void setTimeCreated( Calendar timeCreated )
	{
		this.timeCreated = timeCreated;
	}

	public Long getVersion()
	{
		return version;
	}

	public void setVersion( Long version )
	{
		this.version = version;
	}

	public Map<Long, ResourceAssociationEntity> getAssociationsMap()
	{
		return associationsMap;
	}

	public void setAssociationsMap( Map<Long, ResourceAssociationEntity> associationsMap )
	{
		this.associationsMap = associationsMap;
	}

	public List<ResourceAssociationEntity> getAssociations()
	{
		return new ArrayList( associationsMap.values() );
	}

	public final Resource toDataObject()
	{
		return toDataObject( 0, false );
	}

	public final Resource toDataObject( int recursionLevel, boolean useCache )
	{
		if ( ( recursionLevel < 0 ) && ( useCache ) )
		{
			Resource resourceDTO = TopologyCache.getResource( id );

			if ( resourceDTO != null )
			{
				return resourceDTO;
			}
		}

		Resource resourceDTO = toDataObjectAux( recursionLevel, new HashMap() );
		return resourceDTO;
	}

	private Resource toDataObjectAux( int recursionLevel, Map<Long, Resource> allResources )
	{
		Resource dataObject = createDataObject();
		allResources.put( getId(), dataObject );

		List<ResourceAssociation> associationList = new ArrayList( 1 );
		List<ResourceAssociationEntity> relatedResources = new ArrayList( associationsMap.values() );

		for ( ResourceAssociationEntity association : relatedResources )
		{
			if ( recursionLevel != 0 )
			{
				Resource associatedResource = ( Resource ) allResources.get( association.getSecondResource().getId() );

				if ( associatedResource == null )
				{
					associatedResource = association.getSecondResource().toDataObjectAux( recursionLevel - 1, allResources );
				}
				associationList.add( new ResourceAssociation( associatedResource, dataObject, association.getType() ) );
			}
			else
			{
				associationList.add( new ResourceAssociation( association.getSecondResource().getId(), dataObject.getId(), association.getType() ) );
			}
		}
		if ( !associationList.isEmpty() )
		{
			dataObject.setResourceAssociations( associationList );
		}
		return dataObject;
	}

	protected abstract Resource newDataObject();

	public Resource createDataObject()
	{
		Resource dataObject = newDataObject();
		dataObject.setId( getId() );
		dataObject.setName( getName() );
		dataObject.setTimeCreated( getTimeCreated().getTimeInMillis() );

		return dataObject;
	}

	public void readFromDataObject( Resource dataObject )
	{
		setName( dataObject.getName() );
	}

	public abstract Class<? extends Resource> getDataObjectClass();

	public int hashCode()
	{
		int prime = 31;
		int result = 1;
		result = 31 * result + ( id == null ? 0 : id.hashCode() );
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
		ResourceEntity other = ( ResourceEntity ) obj;
		if ( id == null )
		{
			if ( id != null )
			{
				return false;
			}
		}
		else if ( !id.equals( id ) )
		{
			return false;
		}
		return true;
	}

	public String toString()
	{
		return "ResourceEntity [id=" + id + ", name=" + name + ", version=" + version + "]";
	}
}

