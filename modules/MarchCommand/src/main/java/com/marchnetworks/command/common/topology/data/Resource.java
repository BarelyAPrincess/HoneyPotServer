package com.marchnetworks.command.common.topology.data;

import com.marchnetworks.command.api.query.Criteria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

public abstract class Resource
{
	protected Long id;
	protected String name;
	protected long timeCreated;
	protected List<ResourceAssociation> resourceAssociations = new CopyOnWriteArrayList();
	protected transient Resource parentResource;

	@XmlTransient
	public String getIdAsString()
	{
		return String.valueOf( id );
	}

	@XmlElement( required = true )
	public Long getId()
	{
		return id;
	}

	public void setId( Long id )
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public long getTimeCreated()
	{
		return timeCreated;
	}

	public void setTimeCreated( long timeCreated )
	{
		this.timeCreated = timeCreated;
	}

	public List<ResourceAssociation> getResourceAssociationsByType( String type )
	{
		List<ResourceAssociation> associationByType = new ArrayList();
		for ( ResourceAssociation resourceAssociation : getResourceAssociations() )
		{
			if ( resourceAssociation.getAssociationType().equals( type ) )
			{
				associationByType.add( resourceAssociation );
			}
		}
		return associationByType;
	}

	public List<ResourceAssociation> getResourceAssociationsByTypes( String... types )
	{
		Set<String> typeSet = new HashSet( Arrays.asList( types ) );
		List<ResourceAssociation> associationByType = new ArrayList();
		for ( ResourceAssociation resourceAssociation : getResourceAssociations() )
		{
			if ( typeSet.contains( resourceAssociation.getAssociationType() ) )
			{
				associationByType.add( resourceAssociation );
			}
		}
		return associationByType;
	}

	public List<ResourceAssociation> getResourceAssociations()
	{
		return resourceAssociations;
	}

	public void setResourceAssociations( List<ResourceAssociation> resourceAssociations )
	{
		this.resourceAssociations = new CopyOnWriteArrayList( resourceAssociations );
	}

	public List<Resource> createResourceList()
	{
		List<Resource> resourceList = new ArrayList();

		getResourceListAux( resourceList );

		return resourceList;
	}

	private void getResourceListAux( List<Resource> resourceList )
	{
		resourceList.add( this );

		for ( ResourceAssociation association : resourceAssociations )
		{
			if ( association.getResource() != null )
			{
				association.getResource().getResourceListAux( resourceList );
			}
		}
	}

	public boolean containsResource( Long id )
	{
		if ( getId().equals( id ) )
		{
			return true;
		}
		for ( ResourceAssociation association : resourceAssociations )
		{
			if ( ( association.getResource() != null ) && ( association.getResource().containsResource( id ) ) )
			{
				return true;
			}
		}

		return false;
	}

	public List<Resource> createFilteredResourceList( Class<?>... resourceTypeFilter )
	{
		List<Resource> resourceList = new ArrayList();
		getFilteredResourceListAux( resourceList, resourceTypeFilter );
		return resourceList;
	}

	private void getFilteredResourceListAux( List<Resource> resourceList, Class<?>... resourceTypeFilter )
	{
		for ( Class<?> clazz : resourceTypeFilter )
		{
			if ( clazz.isAssignableFrom( getClass() ) )
			{
				resourceList.add( this );
				break;
			}
		}
		for ( ResourceAssociation association : resourceAssociations )
		{
			if ( association.getResource() != null )
			{
				association.getResource().getFilteredResourceListAux( resourceList, resourceTypeFilter );
			}
		}
	}

	public Resource getChildResourceByName( String name )
	{
		for ( ResourceAssociation association : resourceAssociations )
		{
			Resource child = association.getResource();
			if ( ( child != null ) && ( child.getName().equals( name ) ) )
			{
				return child;
			}
		}
		return null;
	}

	public void createAssociation( Resource childResource, String associationType )
	{
		ResourceAssociation resourceAssociation = new ResourceAssociation( childResource, this, associationType );
		resourceAssociations.add( resourceAssociation );
	}

	public String toString()
	{
		return "id: " + id + ", name: " + name + ", class:" + getClass().getSimpleName();
	}

	public Set<Long> getAllResourceAssociationIds()
	{
		Set<Long> resourceIdSet = new LinkedHashSet();
		getAllResourceAssociationIdsAux( resourceIdSet );
		return resourceIdSet;
	}

	public Resource copyFolders()
	{
		Resource res = null;
		if ( ( this instanceof Group ) )
		{
			res = new Group();
			copy( this, res );
		}
		return res;
	}

	private void copy( Resource from, Resource to )
	{
		to.setId( from.getId() );
		to.setName( from.getName() );
		to.setParentResource( from.getParentResource() );
		to.setTimeCreated( from.getTimeCreated() );
		List<ResourceAssociation> resAssociations = from.getResourceAssociations();
		for ( ResourceAssociation resAssociation : resAssociations )
		{
			Resource child = resAssociation.getResource();
			if ( ( child != null ) && ( ( child instanceof Group ) ) )
			{
				Group newRes = new Group();
				to.createAssociation( newRes, resAssociation.getAssociationType() );
				copy( child, newRes );
			}
		}
	}

	private void getAllResourceAssociationIdsAux( Set<Long> resourceList )
	{
		resourceList.add( getId() );
		for ( ResourceAssociation association : resourceAssociations )
		{
			if ( association.getResource() != null )
			{
				association.getResource().getAllResourceAssociationIdsAux( resourceList );
			}
		}
	}

	public Set<Long> getResourceAssociationIds()
	{
		Set<Long> resourceIdSet = new LinkedHashSet();
		for ( ResourceAssociation association : resourceAssociations )
		{
			resourceIdSet.add( association.getResourceId() );
		}
		return resourceIdSet;
	}

	public void update( Resource updatedResource )
	{
		name = updatedResource.getName();
		timeCreated = updatedResource.getTimeCreated();
	}

	public LinkType getLinkType()
	{
		return null;
	}

	@XmlTransient
	public Long getParentResourceId()
	{
		if ( parentResource != null )
		{
			return parentResource.getId();
		}
		return null;
	}

	@XmlTransient
	public Resource getParentResource()
	{
		return parentResource;
	}

	public void setParentResource( Resource parentResource )
	{
		this.parentResource = parentResource;
	}

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
			return true;
		if ( obj == null )
			return false;
		if ( getClass() != obj.getClass() )
			return false;
		Resource other = ( Resource ) obj;
		if ( id == null )
		{
			if ( id != null )
				return false;
		}
		else if ( !id.equals( id ) )
			return false;
		return true;
	}

	public boolean containsResource( Class<?>... resourceTypeFilter )
	{
		return containsResourceAux( resourceTypeFilter );
	}

	private boolean containsResourceAux( Class<?>... resourceTypeFilter )
	{
		for ( Class<?> clazz : resourceTypeFilter )
		{
			if ( clazz.isAssignableFrom( getClass() ) )
			{
				return true;
			}
		}
		for ( ResourceAssociation association : resourceAssociations )
		{
			if ( association.getResource() != null )
			{
				boolean found = association.getResource().containsResourceAux( resourceTypeFilter );
				if ( found )
				{
					return true;
				}
			}
		}

		return false;
	}

	public boolean containsResource( Criteria criteria )
	{
		return containsResourceAux( criteria );
	}

	private boolean containsResourceAux( Criteria criteria )
	{
		if ( criteria.match( this ) )
		{
			return true;
		}
		for ( ResourceAssociation association : resourceAssociations )
		{
			if ( association.getResource() != null )
			{
				boolean found = association.getResource().containsResourceAux( criteria );
				if ( found )
				{
					return true;
				}
			}
		}

		return false;
	}
}
