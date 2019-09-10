package com.marchnetworks.management.topology.util;

import com.marchnetworks.command.api.query.Criteria;
import com.marchnetworks.command.common.topology.TopologyConstants;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.command.common.topology.data.ResourceAssociation;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.topology.dao.GroupResourceDAO;
import com.marchnetworks.management.topology.dao.ResourceAssociationDAO;
import com.marchnetworks.management.topology.dao.ResourceDAO;
import com.marchnetworks.management.topology.model.GroupEntity;
import com.marchnetworks.management.topology.model.ResourceAssociationEntity;
import com.marchnetworks.management.topology.model.ResourceEntity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TopologyCache
{
	private static final Logger LOG = LoggerFactory.getLogger( TopologyCache.class );

	private static Map<Long, Resource> resourcesCache;
	private static Object lock = new Object();
	private static boolean initialized = false;

	public static void initCache()
	{
		synchronized ( lock )
		{
			rebuildCache();
		}
	}

	public static Resource getResource( Long resourceId )
	{
		if ( !initialized )
		{
			initCache();
		}
		return ( Resource ) resourcesCache.get( resourceId );
	}

	public static List<Resource> getResources( Class<?>... classes )
	{
		if ( !initialized )
		{
			initCache();
		}
		List<Resource> result = new ArrayList();
		for ( Resource resource : resourcesCache.values() )
		{
			for ( Class<?> clazz : classes )
			{
				if ( resource.getClass().isAssignableFrom( clazz ) )
				{
					result.add( resource );
					break;
				}
			}
		}
		return result;
	}

	public static <T extends Resource> List<T> getResources( Criteria criteria )
	{
		if ( !initialized )
			initCache();

		List<T> result = new ArrayList<T>();

		for ( Resource resource : resourcesCache.values() )
			if ( criteria.match( resource ) )
				result.add( ( T ) resource );

		return result;
	}

	public static Resource getFirstResource( Criteria criteria )
	{
		if ( !initialized )
		{
			initCache();
		}

		for ( Resource resource : resourcesCache.values() )
		{
			if ( criteria.match( resource ) )
			{
				return resource;
			}
		}
		return null;
	}

	public static List<Resource> createFilteredResourceList( Resource resource, Criteria criteria )
	{
		if ( !initialized )
		{
			initCache();
		}

		List<Resource> resourceList = new ArrayList();
		getFilteredResourceListAux( resource, resourceList, criteria );
		return resourceList;
	}

	private static void getFilteredResourceListAux( Resource resource, List<Resource> resourceList, Criteria criteria )
	{
		if ( criteria.match( resource ) )
		{
			resourceList.add( resource );
		}
		for ( ResourceAssociation association : resource.getResourceAssociations() )
		{
			Resource child = association.getResource();
			if ( child != null )
			{
				getFilteredResourceListAux( child, resourceList, criteria );
			}
		}
	}

	public static List<Resource> createResourceHierarchy( Resource resource, Criteria criteria )
	{
		List<Resource> resourceList = new ArrayList();

		if ( criteria.match( resource ) )
		{
			resourceList.add( resource );
		}

		createResourceHierarchy( resource, criteria, resourceList );
		return resourceList;
	}

	private static void createResourceHierarchy( Resource resource, Criteria criteria, List<Resource> resources )
	{
		Resource resourceParent = resource.getParentResource();
		if ( resourceParent != null )
		{
			if ( criteria.match( resourceParent ) )
			{
				resources.add( resource.getParentResource() );
			}
			createResourceHierarchy( resourceParent, criteria, resources );
		}
	}

	public static void addResource( Resource resource, String associationType, Long parentResourceId )
	{
		synchronized ( lock )
		{
			Resource parentResource = ( Resource ) resourcesCache.get( parentResourceId );
			if ( parentResource != null )
			{
				resource.setParentResource( parentResource );
				resourcesCache.put( resource.getId(), resource );
				LOG.debug( "Resource {} added to topology cache", resource.toString() );

				createAssociation( resource, parentResource, associationType );
			}
			else
			{
				LOG.warn( "Parent resource with id {} not found", parentResourceId );
			}
		}
	}

	public static void addRootResource( Resource resource )
	{
		synchronized ( lock )
		{
			resourcesCache.put( resource.getId(), resource );
		}
	}

	public static void removeResource( Long resourceId )
	{
		synchronized ( lock )
		{
			Resource resource = ( Resource ) resourcesCache.get( resourceId );
			if ( resource != null )
			{

				Resource parentResource = resource.getParentResource();
				if ( parentResource != null )
				{
					removeAssociation( parentResource, resourceId );
				}

				resourcesCache.remove( resourceId );
			}
		}
	}

	public static void updateResource( Resource updatedResource )
	{
		synchronized ( lock )
		{
			Resource resourceToUpdate = ( Resource ) resourcesCache.get( updatedResource.getId() );
			if ( resourceToUpdate != null )
			{
				resourceToUpdate.update( updatedResource );
			}
		}
	}

	public static void updateResourceAssociation( Long resourceId, Long parentResourceId, String associationType )
	{
		synchronized ( lock )
		{
			Resource resource = ( Resource ) resourcesCache.get( resourceId );
			if ( resource != null )
			{
				Resource formerParent = resource.getParentResource();
				if ( formerParent != null )
				{
					removeAssociation( formerParent, resourceId );
				}

				Resource newParent = ( Resource ) resourcesCache.get( parentResourceId );
				if ( newParent != null )
				{
					resource.setParentResource( newParent );
					createAssociation( resource, newParent, associationType );
				}
			}
		}
	}

	private static void rebuildCache()
	{
		if ( initialized )
		{
			return;
		}
		long startTime = System.currentTimeMillis();

		ResourceDAO<ResourceEntity> resourceDAO = ( ResourceDAO ) ApplicationContextSupport.getBean( "resourceDAO" );
		ResourceEntity systemRootResource = ( ResourceEntity ) resourceDAO.findById( TopologyConstants.SYSTEM_ROOT_ID );
		Resource systemResource = systemRootResource.createDataObject();
		ResourceEntity logicalRootResource = ( ResourceEntity ) resourceDAO.findById( TopologyConstants.LOGICAL_ROOT_ID );
		Resource logicalResource = logicalRootResource.createDataObject();

		ResourceAssociationDAO resourceAssociationDao = ( ResourceAssociationDAO ) ApplicationContextSupport.getBean( "resourceAssociationDAO" );
		List<ResourceAssociationEntity> associationList = resourceAssociationDao.findAllDetached();

		if ( resourcesCache == null )
		{
			int associationsSize = Math.max( 16, associationList.size() );
			resourcesCache = new ConcurrentHashMap( associationsSize );
		}
		resourcesCache.clear();

		resourcesCache.put( systemResource.getId(), systemResource );
		resourcesCache.put( logicalResource.getId(), logicalResource );

		for ( ResourceAssociationEntity association : associationList )
		{
			ResourceEntity secondResourceEntity = association.getSecondResource();

			Resource secondResource = ( Resource ) resourcesCache.get( secondResourceEntity.getId() );
			if ( secondResource == null )
			{
				secondResource = secondResourceEntity.createDataObject();
				resourcesCache.put( secondResourceEntity.getId(), secondResource );
			}

			ResourceEntity firstResourceEntity = association.getFirstResource();

			Resource firstResource = ( Resource ) resourcesCache.get( firstResourceEntity.getId() );
			if ( firstResource == null )
			{
				firstResource = firstResourceEntity.createDataObject();
				resourcesCache.put( firstResourceEntity.getId(), firstResource );
			}

			secondResource.setParentResource( firstResource );

			createAssociation( secondResource, firstResource, association.getType() );
		}

		GroupResourceDAO groupResourceDAO = ( GroupResourceDAO ) ApplicationContextSupport.getBean( "groupResourceDAO" );
		List<GroupEntity> groups = groupResourceDAO.findAllEmptyResourceNodes();

		for ( GroupEntity group : groups )
		{
			if ( ( !TopologyConstants.SYSTEM_ROOT_ID.equals( group.getId() ) ) && ( !TopologyConstants.LOGICAL_ROOT_ID.equals( group.getId() ) ) && ( !resourcesCache.containsKey( group.getId() ) ) )
			{
				resourcesCache.put( group.getId(), group.createDataObject() );
			}
		}

		LOG.info( "Total time to build cache was {} ms", Long.valueOf( System.currentTimeMillis() - startTime ) );
		initialized = true;
	}

	public static void invalidateCache()
	{
		initialized = false;
	}

	private static void createAssociation( Resource childResource, Resource parentResource, String associationType )
	{
		parentResource.createAssociation( childResource, associationType );
	}

	private static void removeAssociation( Resource parentResource, Long childResourceId )
	{
		List<ResourceAssociation> tempList = new ArrayList( parentResource.getResourceAssociations() );
		Iterator<ResourceAssociation> iterator = tempList.iterator();
		while ( iterator.hasNext() )
		{
			ResourceAssociation association = ( ResourceAssociation ) iterator.next();
			if ( association.getResourceId().equals( childResourceId ) )
			{
				iterator.remove();
				break;
			}
		}
		parentResource.setResourceAssociations( tempList );
	}
}

