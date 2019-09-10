package com.marchnetworks.management.topology;

import com.marchnetworks.app.events.AppEvent;
import com.marchnetworks.app.events.AppEventType;
import com.marchnetworks.app.service.OsgiService;
import com.marchnetworks.audit.data.AuditEventNameEnum;
import com.marchnetworks.audit.data.AuditView;
import com.marchnetworks.audit.data.AuditView.Builder;
import com.marchnetworks.audit.events.AuditEvent;
import com.marchnetworks.command.api.initialization.InitializationListener;
import com.marchnetworks.command.api.query.Criteria;
import com.marchnetworks.command.api.query.Restrictions;
import com.marchnetworks.command.api.topology.TopologyCoreService;
import com.marchnetworks.command.api.topology.validators.TopologyValidator;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.HttpUtils;
import com.marchnetworks.command.common.device.data.ChannelState;
import com.marchnetworks.command.common.device.data.ChannelView;
import com.marchnetworks.command.common.device.data.ConnectState;
import com.marchnetworks.command.common.device.data.DeviceView;
import com.marchnetworks.command.common.device.data.MassRegistrationInfo;
import com.marchnetworks.command.common.device.data.RegistrationStatus;
import com.marchnetworks.command.common.topology.ResourceAssociationType;
import com.marchnetworks.command.common.topology.ResourceRootType;
import com.marchnetworks.command.common.topology.TopologyConstants;
import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.topology.TopologyExceptionTypeEnum;
import com.marchnetworks.command.common.topology.data.AlarmSourceResource;
import com.marchnetworks.command.common.topology.data.ChannelLinkResource;
import com.marchnetworks.command.common.topology.data.ChannelResource;
import com.marchnetworks.command.common.topology.data.ContainerItem;
import com.marchnetworks.command.common.topology.data.DefaultRootResource;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.command.common.topology.data.GenericLinkResource;
import com.marchnetworks.command.common.topology.data.GenericResource;
import com.marchnetworks.command.common.topology.data.Group;
import com.marchnetworks.command.common.topology.data.LinkResource;
import com.marchnetworks.command.common.topology.data.MapResource;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.command.common.topology.data.ResourceAssociation;
import com.marchnetworks.command.common.topology.data.ResourceMarkForReplacement;
import com.marchnetworks.command.common.topology.data.ResourcePathNode;
import com.marchnetworks.command.common.topology.data.ViewResource;
import com.marchnetworks.command.common.user.UserException;
import com.marchnetworks.command.common.user.data.MemberView;
import com.marchnetworks.command.common.user.data.RightEnum;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.transport.datamodel.DeviceException;
import com.marchnetworks.common.types.DeviceExceptionTypes;
import com.marchnetworks.common.utils.CommonUtils;
import com.marchnetworks.license.LicenseService;
import com.marchnetworks.management.instrumentation.DeviceRegistry;
import com.marchnetworks.management.instrumentation.DeviceService;
import com.marchnetworks.management.instrumentation.data.RegistrationAuditEnum;
import com.marchnetworks.management.instrumentation.data.RegistrationAuditInfo;
import com.marchnetworks.management.instrumentation.events.AbstractDeviceEvent;
import com.marchnetworks.management.instrumentation.events.DeviceAddedEvent;
import com.marchnetworks.management.instrumentation.events.MassRegistrationEvent;
import com.marchnetworks.management.topology.dao.DefaultRootResourceDAO;
import com.marchnetworks.management.topology.dao.DeviceResourceDAO;
import com.marchnetworks.management.topology.dao.GenericResourceDAO;
import com.marchnetworks.management.topology.dao.LinkResourceDAO;
import com.marchnetworks.management.topology.dao.ResourceAssociationDAO;
import com.marchnetworks.management.topology.dao.ResourceDAO;
import com.marchnetworks.management.topology.data.ResourceType;
import com.marchnetworks.management.topology.events.ResourceAssociationChangedEvent;
import com.marchnetworks.management.topology.events.ResourceCreatedEvent;
import com.marchnetworks.management.topology.events.ResourceRemovedEvent;
import com.marchnetworks.management.topology.events.ResourceUpdatedEvent;
import com.marchnetworks.management.topology.model.DefaultRootResourceEntity;
import com.marchnetworks.management.topology.model.DeviceResourceEntity;
import com.marchnetworks.management.topology.model.GroupEntity;
import com.marchnetworks.management.topology.model.LinkResourceEntity;
import com.marchnetworks.management.topology.model.ResourceAssociationEntity;
import com.marchnetworks.management.topology.model.ResourceEntity;
import com.marchnetworks.management.topology.model.ResourceFactory;
import com.marchnetworks.management.topology.util.TopologyCache;
import com.marchnetworks.management.topology.util.TopologyExceptionTranslator;
import com.marchnetworks.management.user.UserService;
import com.marchnetworks.map.service.MapException;
import com.marchnetworks.map.service.MapService;
import com.marchnetworks.schedule.service.ScheduleService;
import com.marchnetworks.server.event.EventListener;
import com.marchnetworks.server.event.EventRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;

public class ResourceTopologyServiceImpl implements ResourceTopologyServiceIF, TopologyCoreService, EventListener, InitializationListener
{
	private static final Logger LOG = LoggerFactory.getLogger( ResourceTopologyServiceImpl.class );

	private ResourceFactory resourceFactory;

	private ResourceDAO<ResourceEntity> resourceDAO;

	private DefaultRootResourceDAO defaultRootResourceDAO;

	private ResourceAssociationDAO resourceAssociationDAO;
	private DeviceResourceDAO deviceResourceDAO;
	private LinkResourceDAO linkResourceDAO;
	private GenericResourceDAO genericResourceDAO;
	private EventRegistry eventRegistry;
	private DeviceService deviceService;
	private UserService userService;
	private LicenseService licenseService;
	private MapService mapService;
	private ScheduleService scheduleService;
	private OsgiService osgiService;
	private String[] defaultRoots;
	private Map<String, Long> deviceIdsMap = new HashMap();

	public void onAppInitialized()
	{
		for ( String rootId : defaultRoots )
		{
			if ( defaultRootResourceDAO.findById( rootId ) == null )
			{
				LOG.info( "Creating root Group {}.", rootId );
				GroupEntity rootGroup = new GroupEntity();
				rootGroup.setName( rootId );
				resourceDAO.create( rootGroup );

				LOG.info( "Creating root DefaultRootResource {}.", rootId );
				DefaultRootResourceEntity root = new DefaultRootResourceEntity();
				root.setId( rootId );
				root.setResource( rootGroup );
				defaultRootResourceDAO.create( root );
			}
		}

		TopologyCache.initCache();
	}

	public void process( Event event )
	{
		if ( ( event instanceof AppEvent ) )
		{
			AppEvent appEvent = ( AppEvent ) event;
			if ( appEvent.getAppEventType().equals( AppEventType.UNINSTALLED ) )
			{
				String owner = appEvent.getAppID();
				try
				{
					removeGenericResources( owner );
				}
				catch ( TopologyException e )
				{
					LOG.error( "Error while removing GenericResource for App " + owner + ", Exception: " + e.getMessage() );
				}
			}
		}
	}

	public String getListenerName()
	{
		return ResourceTopologyServiceImpl.class.getSimpleName();
	}

	public Resource createResource( Resource resourceData, Long parentResourceId, String associationType ) throws TopologyException
	{
		if ( ( resourceData instanceof LinkResource ) )
		{
			return createLinkResource( ( LinkResource ) resourceData, parentResourceId, associationType );
		}
		return createResourceBase( resourceData, parentResourceId, associationType );
	}

	public Resource createChannelLinkResource( ChannelResource channel, Long parentResourceId, Long deviceResourceId ) throws TopologyException
	{
		ChannelLinkResource channelLink = new ChannelLinkResource();
		channelLink.setChannelId( channel.getChannelId() );
		channelLink.setDeviceResourceId( deviceResourceId );
		channelLink.setName( channel.getName() );
		channelLink.setMetaData( getMetaData( deviceResourceId.toString(), channel.getIdAsString(), channel.getName() ) );
		Long[] linkedResourceIds = new Long[1];
		linkedResourceIds[0] = channel.getId();
		channelLink.setLinkedResourceIds( linkedResourceIds );

		return createResource( channelLink, parentResourceId, ResourceAssociationType.CHANNEL_LINK.name() );
	}

	private Resource createResourceBase( Resource resourceData, Long parentResourceId, String associationType ) throws TopologyException
	{
		ResourceEntity resource = resourceFactory.newResource( resourceData );

		LOG.debug( "Creating resource: {}", resourceData );
		resourceDAO.create( resource );
		resourceFactory.onCreate( resource );

		Resource ret = resource.toDataObject();

		ResourceCreatedEvent createdResource = new ResourceCreatedEvent( ret, parentResourceId, associationType, ret.getAllResourceAssociationIds() );
		eventRegistry.sendEventAfterTransactionCommits( createdResource );

		if ( parentResourceId != null )
		{
			try
			{
				createAssociation( new ResourceAssociation( resource.getId(), parentResourceId, associationType ) );
			}
			catch ( TopologyException te )
			{
				throw new IllegalArgumentException( te.getMessage(), te );
			}
		}

		LOG.debug( "Created resource: {}", ret );
		auditResource( AuditEventNameEnum.TOPOLOGY_CREATE, ret, parentResourceId );
		return ret;
	}

	private Resource createLinkResource( LinkResource resourceData, Long parentResourceId, String associationType ) throws TopologyException
	{
		if ( resourceData.getLinkedResourceIds() == null )
		{
			resourceData.setLinkedResourceIds( new Long[0] );
		}

		validateLinkResource( resourceData );

		if ( parentResourceId == null )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, "parentResourceId has not been set to Linked resource " + resourceData.getName() );
		}

		List<TopologyValidator> validators = getOsgiService().getServices( TopologyValidator.class );
		for ( TopologyValidator validator : validators )
		{
			validator.validateLinkResource( resourceData, parentResourceId );
		}

		ResourceEntity resource = resourceFactory.newResource( resourceData );

		if ( resource.getName() == null )
		{
			resource.setName( resourceData.getName() );
		}

		LOG.debug( "Creating link resource: {}", resourceData );
		resourceDAO.create( resource );
		resourceFactory.onCreate( resource );

		Resource ret = resource.toDataObject();

		ResourceCreatedEvent createdResource = new ResourceCreatedEvent( ret, parentResourceId, associationType, ret.getAllResourceAssociationIds() );
		eventRegistry.sendEventAfterTransactionCommits( createdResource );

		if ( parentResourceId != null )
		{
			try
			{
				createAssociation( new ResourceAssociation( resource.getId(), parentResourceId, associationType ) );
			}
			catch ( TopologyException te )
			{
				throw new IllegalArgumentException( te.getMessage(), te );
			}
		}

		LOG.debug( "Created alarm source link resource: {}", ret );
		auditResource( AuditEventNameEnum.TOPOLOGY_CREATE, ret, parentResourceId );
		return ret;
	}

	public void createResources( Resource[] resources ) throws TopologyException
	{
		LOG.debug( "Started createdResources " + Calendar.getInstance().getTime() );
		if ( resources != null )
		{
			Map<Long, Long> resourceMapList = new HashMap();
			for ( Resource resource : resources )
			{
				List<ResourceAssociation> newResourceAssociationList = resource.getResourceAssociations();
				if ( ( newResourceAssociationList == null ) || ( newResourceAssociationList.isEmpty() ) )
				{
					throw new TopologyException( TopologyExceptionTypeEnum.RESOURCE_ASSOCIATION_NOT_FOUND, "No resource association found/set for resource " + resource.getName() );
				}

				ResourceAssociation newResourceAssociation = ( ResourceAssociation ) newResourceAssociationList.get( 0 );

				Long parentResourceId = newResourceAssociation.getParentResourceId();
				if ( resourceMapList.get( newResourceAssociation.getParentResourceId() ) != null )
				{

					parentResourceId = ( Long ) resourceMapList.get( newResourceAssociation.getParentResourceId() );
				}

				try
				{
					Resource newResource = createResource( resource, parentResourceId, newResourceAssociation.getAssociationType() );

					resourceMapList.put( resource.getId(), newResource.getId() );
				}
				catch ( TopologyException te )
				{
					throw new IllegalArgumentException( te.getMessage(), te );
				}
			}
		}
		LOG.debug( "Finished createdResources " + Calendar.getInstance().getTime() );
	}

	public Resource getResource( Long resourceId ) throws TopologyException
	{
		return getResource( resourceId, -1 );
	}

	public Resource getResource( Long resourceId, int recursionLevel ) throws TopologyException
	{
		if ( resourceId == null )
		{
			return null;
		}

		Resource ret = TopologyCache.getResource( resourceId );

		if ( ret == null )
		{
			LOG.warn( "Resource {} not found.", resourceId );
			throw new TopologyException( TopologyExceptionTypeEnum.RESOURCE_NOT_FOUND, "Resource " + resourceId + " not found." );
		}
		return ret;
	}

	public List<Resource> getResources( Long[] resourceIds, int recursionLevel ) throws TopologyException
	{
		if ( ( resourceIds == null ) || ( resourceIds.length == 0 ) )
		{
			return new ArrayList();
		}

		String authenticatedUser = CommonAppUtils.getUsernameFromSecurityContext();
		MemberView aUser = null;
		try
		{
			aUser = getUserService().getUser( authenticatedUser );
		}
		catch ( UserException e )
		{
			LOG.info( "Failed to obtain data from user {}. Error details: {}", new Object[] {authenticatedUser, e.getMessage()} );
			throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, e.getMessage() );
		}
		if ( aUser == null )
		{
			return new ArrayList();
		}

		Set<Long> usersTerritoryResourceView = getAllResourceIdsFromUsersTerritory( aUser );
		return getResources( resourceIds, recursionLevel, usersTerritoryResourceView );
	}

	public boolean isOnPath( Set<Long> resourceSet1, Set<Long> resourceSet2 )
	{
		for ( Iterator i$ = resourceSet1.iterator(); i$.hasNext(); )
		{
			Long resourceId1 = ( Long ) i$.next();
			for ( Long resourceId2 : resourceSet2 )
			{
				if ( isChild( resourceId1, resourceId2 ) )
					return true;
				if ( isChild( resourceId2, resourceId1 ) )
					return true;
			}
		}
		Long resourceId1;
		return false;
	}

	private Set<Long> getAllResourceIdsFromUsersTerritory( MemberView member ) throws TopologyException
	{
		Set<Long> usersIdList = new HashSet();
		for ( Long userTerritoryRoot : member.getAllRoots( true ) )
		{
			boolean isLogicalRoot = member.getAssembledLogicalRoots().contains( userTerritoryRoot );

			Resource territoryRoot = getResource( userTerritoryRoot );
			List<Resource> childrenResourceList = territoryRoot.createResourceList();
			for ( Resource childResource : childrenResourceList )
			{
				usersIdList.add( childResource.getId() );

				if ( ( isLogicalRoot ) && ( ( childResource instanceof LinkResource ) ) )
				{
					LinkResource linkRes = ( LinkResource ) childResource;
					for ( Long linkedId : linkRes.getLinkedResourceIds() )
						usersIdList.addAll( getResourcePath( linkedId, true, new Class[] {Group.class} ) );
				}
			}
		}

		return usersIdList;
	}

	public List<Resource> getResources( Long[] resourceIds, int recursionLevel, Set<Long> usersResourceView ) throws TopologyException
	{
		List<Resource> result = new ArrayList();

		for ( Long resourceId : resourceIds )
		{
			Resource r = getResource( resourceId, recursionLevel );
			if ( ( r instanceof DeviceResource ) )
			{
				DeviceResource device = ( DeviceResource ) r;
				if ( "1001".equals( device.getDeviceView().getFamily() ) )
				{
					result.add( r );
					continue;
				}
			}

			if ( usersResourceView.contains( resourceId ) )
			{
				result.add( r );

				if ( recursionLevel != 0 )
				{

					result.addAll( getResources( ( Long[] ) r.getResourceAssociationIds().toArray( new Long[0] ), recursionLevel, usersResourceView ) );
				}
			}
		}
		return result;
	}

	public Resource updateResource( Resource resourceData ) throws TopologyException
	{
		LOG.debug( "Updating resource: {}", resourceData );

		ResourceEntity resource = ( ResourceEntity ) resourceDAO.findById( resourceData.getId() );

		if ( resource == null )
		{
			LOG.warn( "Resource {} not found.", resourceData.getId() );
			throw new TopologyException( TopologyExceptionTypeEnum.RESOURCE_NOT_FOUND, "Resource " + resourceData.getId() + " not found." );
		}

		if ( ( resourceData instanceof LinkResource ) )
		{
			LinkResource linkResource = ( LinkResource ) resourceData;
			validateLinkResource( linkResource );
		}

		resource.readFromDataObject( resourceData );

		LOG.debug( "Updated resource: {}", resourceData );
		Resource resourceView = resource.toDataObject();

		Resource cached = TopologyCache.getResource( resource.getId() );
		resourceView.setParentResource( cached.getParentResource() );

		Set<Long> territoryIds = resourceView.getAllResourceAssociationIds();

		List<LinkResource> linkedResources = getLinkResources( resource.getId() );
		for ( LinkResource linkedResource : linkedResources )
		{
			territoryIds.add( linkedResource.getId() );
		}

		ResourceUpdatedEvent resourceUpdated = new ResourceUpdatedEvent( resourceView, territoryIds );

		eventRegistry.sendEventAfterTransactionCommits( resourceUpdated );

		auditResource( AuditEventNameEnum.TOPOLOGY_UPDATE, resourceView, null );
		return resourceView;
	}

	public void unregisterDeviceById( String deviceId ) throws TopologyException
	{
		DeviceResource deviceResource = null;

		deviceResource = getDeviceResourceByDeviceId( deviceId );

		if ( deviceResource == null )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.DEVICE_NOT_FOUND );
		}

		removeResource( deviceResource.getId() );
	}

	public boolean removeResource( Long resourceId ) throws TopologyException
	{
		LOG.debug( "Removing resource {}.", resourceId );

		ResourceEntity resource = ( ResourceEntity ) resourceDAO.findById( resourceId );

		if ( resource != null )
		{
			return removeResource( resource );
		}
		LOG.warn( "Resource not found.", resourceId );
		return false;
	}

	private boolean removeResource( ResourceEntity resource ) throws TopologyException
	{
		checkResourceRemovable( resource );

		List<ResourceEntity> resourcesToRemove = new ArrayList();
		Resource resourceDTO = TopologyCache.getResource( resource.getId() );
		Set<Long> rootResourceIdList = resourceDTO.getAllResourceAssociationIds();
		rootResourceIdList.remove( resource.getId() );

		if ( !rootResourceIdList.isEmpty() )
		{
			for ( Long childResourceId : rootResourceIdList )
			{
				ResourceEntity childResource = ( ResourceEntity ) resourceDAO.findById( childResourceId );
				checkResourceRemovable( childResource );

				if ( childResource != null )
				{
					resourcesToRemove.add( childResource );
				}
			}

			Collections.reverse( resourcesToRemove );
		}
		resourcesToRemove.add( resource );

		List<ResourceAssociationEntity> parentAssociations = resourceAssociationDAO.findParentAssociations( resourcesToRemove );
		for ( ResourceAssociationEntity parentAssociation : parentAssociations )
		{
			resourceAssociationDAO.delete( parentAssociation );
		}

		for ( ResourceEntity resourceToRemove : resourcesToRemove )
		{
			resourceToRemove.getAssociationsMap().clear();

			List<LinkResourceEntity> linkedResources = linkResourceDAO.findAllByLinkedResourceId( resourceToRemove.getId() );
			for ( LinkResourceEntity linkResourceEntity : linkedResources )
			{
				if ( !linkResourceEntity.isContainer() )
				{
					removeResource( linkResourceEntity );
				}
				else
				{
					LinkResource linkResource = ( LinkResource ) linkResourceEntity.toDataObject();
					linkResource.removeLinkedResource( resourceToRemove.getId() );

					if ( ( linkResource instanceof ViewResource ) )
					{
						( ( ViewResource ) linkResource ).removeLinkedResource( resourceToRemove.toDataObject() );
					}
					updateResource( linkResource );
				}
			}

			resourceFactory.onRemove( TopologyCache.getResource( resourceToRemove.getId() ) );

			LOG.debug( "Removing resource id: {}", resourceToRemove.getId() );
			resourceDAO.delete( resourceToRemove );

			Resource resourceData = TopologyCache.getResource( resourceToRemove.getId() );
			eventRegistry.sendEventAfterTransactionCommits( new ResourceRemovedEvent( resourceData, null ) );
			auditResource( AuditEventNameEnum.TOPOLOGY_REMOVE, resourceData, null );
		}

		LOG.debug( "Removed resource {}.", resource.getId() );
		return true;
	}

	public void removeResources( Long[] resourceIds ) throws TopologyException
	{
		removeResources( resourceIds, false );
	}

	public void removeResources( Long[] resourceIds, boolean forceDeletion ) throws TopologyException
	{
		if ( CommonAppUtils.getUsernameFromSecurityContext() != null )
		{

			Class<?>[] singleDeleteResources = {GenericResource.class, DeviceResource.class};
			for ( Long resourceId : resourceIds )
			{
				if ( ( resourceId.equals( TopologyConstants.SYSTEM_ROOT_ID ) ) || ( resourceId.equals( TopologyConstants.LOGICAL_ROOT_ID ) ) )
				{
					throw new TopologyException( TopologyExceptionTypeEnum.CANNOT_DELETE_TOPOLOGY_ROOTS, "Cannot delete System or Logical root folders." );
				}
				Resource resource = TopologyCache.getResource( resourceId );
				if ( ( ( resource instanceof Group ) ) && ( resource.containsResource( singleDeleteResources ) ) )
				{
					throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, "The folder contains one or more single delete Resources. You must delete them individually." );
				}

				if ( resourceIds.length > 1 )
				{
					for ( Class<?> res : singleDeleteResources )
					{
						if ( resource.getClass().equals( res ) )
						{
							throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, "Single delete Resources must be deleted individually." );
						}
					}
				}
			}

			if ( !getScheduleService().checkDeletions( Arrays.asList( resourceIds ), forceDeletion ) )
			{
				if ( !forceDeletion )
				{
					throw new TopologyException( TopologyExceptionTypeEnum.FOLDER_SET_TO_SCHEDULE, "Cannot delete folder because there is a Schedule on this topology path." );
				}

				throw new TopologyException( TopologyExceptionTypeEnum.SINGLE_FOLDER_SET_TO_SCHEDULE, "Cannot delete folder because it is the only folder set to the schedule." );
			}

			if ( !getUserService().checkDeletions( Arrays.asList( resourceIds ) ) )
			{
				throw new TopologyException( TopologyExceptionTypeEnum.FOLDER_SET_AS_USER_TERRITORY, "Cannot delete folder because it is assigned as user territory root." );
			}
		}

		for ( Long resourceId : resourceIds )
		{
			removeResource( resourceId );
		}
	}

	private void checkResourceRemovable( ResourceEntity resource ) throws TopologyException
	{
		if ( ( ( resource instanceof DeviceResourceEntity ) ) && ( !getUserService().hasRight( RightEnum.MANAGE_DEVICES ) ) )
		{
			throw new AccessDeniedException( "User not authorized to delete devices" );
		}
	}

	public void createAssociation( ResourceAssociation association ) throws TopologyException
	{
		LOG.debug( "Creating association: {}", association );

		ResourceEntity resource = ( ResourceEntity ) resourceDAO.findById( association.getResourceId() );
		if ( resource == null )
		{
			LOG.warn( "Resource {} not found.", association.getResourceId() );
			throw new TopologyException( TopologyExceptionTypeEnum.RESOURCE_NOT_FOUND, "Resource " + association.getResourceId() + " not found." );
		}

		ResourceEntity parentResource = ( ResourceEntity ) resourceDAO.findById( association.getParentResourceId() );

		if ( parentResource == null )
		{
			LOG.warn( "Resource {} not found.", association.getParentResourceId() );
			throw new TopologyException( TopologyExceptionTypeEnum.RESOURCE_NOT_FOUND, "Parent resource " + association.getParentResourceId() + " not found." );
		}
		if ( parentResource.equals( resource ) )
		{
			String message = "Resource " + resource.getIdAsString() + " cannot be parent to itself.";
			LOG.warn( message );
			throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, message );
		}

		resourceFactory.onCreateAssociation( resource, parentResource );
		resourceAssociationDAO.create( new ResourceAssociationEntity( parentResource, resource, association.getAssociationType() ) );

		LOG.debug( "Created association: {}", association );
	}

	public void updateAssociation( ResourceAssociation association, ResourceAssociation newAssociation ) throws TopologyException
	{
		LOG.debug( "Updating association {} to association {}", association, newAssociation );

		if ( !association.getResourceId().equals( newAssociation.getResourceId() ) )
		{
			LOG.warn( "Invalid request (different resource ids) to update association {} to association {}", association, newAssociation );
			throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST );
		}

		Resource oldParentResource = TopologyCache.getResource( association.getParentResourceId() );
		if ( oldParentResource == null )
		{
			LOG.warn( "Previous parent resource {} not found when moving topology", association.getParentResourceId() );
			throw new TopologyException( TopologyExceptionTypeEnum.RESOURCE_NOT_FOUND, "Previous parent resource " + association.getParentResourceId() + " not found." );
		}

		createAssociation( newAssociation );

		ResourceAssociationEntity associationEntity = resourceAssociationDAO.findAssociation( oldParentResource.getId(), association.getResourceId(), association.getAssociationType() );
		resourceAssociationDAO.delete( associationEntity );
		LOG.debug( "Removed association: {}", association );

		LOG.debug( "Updated association {} to association {}", association, newAssociation );

		Set<Long> territoryIds = new HashSet();
		Collections.addAll( territoryIds, new Long[] {newAssociation.getParentResourceId(), newAssociation.getResourceId()} );

		eventRegistry.sendEventAfterTransactionCommits( new ResourceAssociationChangedEvent( getResource( newAssociation.getResourceId() ), newAssociation.getParentResourceId(), newAssociation.getAssociationType(), territoryIds ) );

		auditResource( AuditEventNameEnum.TOPOLOGY_MOVE, getResource( newAssociation.getResourceId() ), newAssociation.getParentResourceId() );
	}

	public void updateAssociations( ResourceAssociation[] associations, ResourceAssociation[] newAssociations ) throws TopologyException
	{
		try
		{
			for ( int i = 0; i < associations.length; i++ )
			{
				updateAssociation( associations[i], newAssociations[i] );
			}
		}
		catch ( TopologyException te )
		{
			throw new IllegalArgumentException( te.getMessage(), te );
		}
	}

	public DefaultRootResource getDefaultRootResource( String key )
	{
		DefaultRootResourceEntity root = ( DefaultRootResourceEntity ) defaultRootResourceDAO.findById( key );
		if ( root != null )
		{
			DefaultRootResource ret = root.toDataObject();
			LOG.debug( "Found default root resource: {}", ret );
			return ret;
		}
		LOG.warn( "Default root resource {} not found.", key );
		return null;
	}

	public DefaultRootResource[] getDefaultRootResources()
	{
		List<DefaultRootResourceEntity> roots = defaultRootResourceDAO.findAll();

		List<DefaultRootResource> ret = new ArrayList();

		for ( DefaultRootResourceEntity root : roots )
		{
			LOG.debug( "Found default root resource: {}", root );
			ret.add( root.toDataObject() );
		}

		return ( DefaultRootResource[] ) ret.toArray( new DefaultRootResource[0] );
	}

	public void markForReplacements( ResourceMarkForReplacement[] resourceMarkForReplacements ) throws TopologyException
	{
		for ( ResourceMarkForReplacement resourceMarkForReplacement : resourceMarkForReplacements )
		{
			markForReplacement( resourceMarkForReplacement );
		}
	}

	public void markForReplacement( ResourceMarkForReplacement resourceMarkForReplacement ) throws TopologyException
	{
		DeviceResource deviceResource = null;
		if ( resourceMarkForReplacement.getDeviceResourceId() != null )
		{
			deviceResource = getDeviceResource( resourceMarkForReplacement.getDeviceResourceId() );
		}

		if ( deviceResource == null )
		{
			LOG.warn( "Device resource {} not found when re-registering.", resourceMarkForReplacement.getDeviceResourceId() );
			throw new TopologyException( TopologyExceptionTypeEnum.DEVICE_NOT_FOUND );
		}

		if ( deviceResource.getDeviceView().getConnectState() != ConnectState.OFFLINE )
		{
			LOG.warn( "Device resource {} not Offline when mark for replacement.", resourceMarkForReplacement.getDeviceResourceId() );
			throw new TopologyException( TopologyExceptionTypeEnum.DEVICE_NOT_OFFLINE );
		}

		String deviceId = deviceResource.getDeviceId().toString();
		try
		{
			deviceService.markForReplacement( deviceId, Boolean.valueOf( resourceMarkForReplacement.isMarkForReplacement() ) );
		}
		catch ( DeviceException ex )
		{
			throw TopologyExceptionTranslator.translateDeviceException( ex );
		}

		if ( resourceMarkForReplacement.isMarkForReplacement() )
		{
			auditDeviceResource( AuditEventNameEnum.DEVICE_MARK_FOR_REPLACEMENT, deviceResource, null, null );
		}
		else
		{
			auditDeviceResource( AuditEventNameEnum.DEVICE_UNDO_MARK_FOR_REPLACEMENT, deviceResource, null, null );
		}
	}

	public void retryReplacement( Long deviceResourceId ) throws TopologyException
	{
		DeviceResource deviceResource = null;
		if ( deviceResourceId != null )
		{
			deviceResource = getDeviceResource( deviceResourceId );
		}

		if ( deviceResource == null )
		{
			LOG.warn( "Device resource {} not found when re-registering.", deviceResourceId );
			throw new TopologyException( TopologyExceptionTypeEnum.DEVICE_NOT_FOUND );
		}

		if ( deviceResource.getDeviceView().getRegistrationStatus() != RegistrationStatus.ERROR_REPLACEMENT )
		{
			LOG.warn( "Device resource {} RegistrationStatus is not in ERROR_REPLACEMENT", deviceResourceId );
			throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST );
		}

		String deviceId = deviceResource.getDeviceId().toString();
		try
		{
			deviceService.scheduleRetryReplacement( deviceId );
		}
		catch ( DeviceException ex )
		{
			throw TopologyExceptionTranslator.translateDeviceException( ex );
		}
	}

	public DeviceResource registerDeviceResource( Long groupId, String deviceAddress, String deviceAdmin, String deviceAdminPassword, String stationId ) throws TopologyException
	{
		return registerDeviceResource( groupId, deviceAddress, null, deviceAdmin, deviceAdminPassword, null, stationId );
	}

	public DeviceResource registerDeviceResource( Long groupId, String deviceAddress, String detectedRemoteAddr, String deviceAdmin, String deviceAdminPassword, String deviceSessionId, String stationId ) throws TopologyException
	{
		String remoteIp = CommonAppUtils.getRemoteIpAddressFromSecurityContext();
		String username = CommonAppUtils.getUsernameFromSecurityContext();
		LOG.info( "Registration request, client: " + CommonAppUtils.isCommandClientRequest() + ", remoteIp:" + remoteIp + ", address:" + deviceAddress + ( !CommonAppUtils.isNullOrEmptyString( stationId ) ? ", stationId: " + stationId : "" ) + ( username != null ? ", user:" + username : "" ) );

		validateRegisterDeviceParams( groupId );
		groupId = getParentResource( groupId );

		if ( !CommonUtils.validateIpAddress( deviceAddress ) )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.INVALID_DEVICE_ADDRESS );
		}

		deviceAddress = HttpUtils.setPortOnAddress( deviceAddress, "443" );
		detectedRemoteAddr = HttpUtils.setPortOnAddress( detectedRemoteAddr, HttpUtils.getPortWithoutAddress( deviceAddress ) );

		String upperCaseStationId = null;

		Map<String, Object> deviceRegistrationInfo = new HashMap();

		if ( !CommonAppUtils.isNullOrEmptyString( deviceSessionId ) )
		{
			deviceRegistrationInfo.put( "securityToken", deviceSessionId );
		}
		else
		{
			deviceRegistrationInfo.put( "admin", deviceAdmin );
			deviceRegistrationInfo.put( "adminPassword", deviceAdminPassword );
		}

		if ( !CommonAppUtils.isNullOrEmptyString( stationId ) )
		{
			upperCaseStationId = stationId.toUpperCase();
		}

		if ( detectedRemoteAddr != null )
		{
			deviceRegistrationInfo.put( "autodetectedaddress", detectedRemoteAddr );
		}
		try
		{
			RegistrationAuditInfo registrationAuditInfo = deviceService.registerDeviceResource( deviceAddress, upperCaseStationId, deviceRegistrationInfo );
			DeviceResource deviceResource = getDeviceResourceByDeviceId( registrationAuditInfo.getDeviceId() );
			if ( deviceResource == null )
			{
				deviceResource = new DeviceResource();
				deviceResource.setDeviceId( registrationAuditInfo.getDeviceId() );
				deviceResource = ( DeviceResource ) createResource( deviceResource, groupId, ResourceAssociationType.DEVICE.name() );
			}

			if ( RegistrationAuditEnum.REGISTRATION == registrationAuditInfo.getRegistrationAuditEnum() )
			{
				AbstractDeviceEvent deviceEvent = new DeviceAddedEvent( registrationAuditInfo.getDeviceId(), deviceRegistrationInfo );
				eventRegistry.sendEventAfterTransactionCommits( deviceEvent );

				auditDeviceResource( AuditEventNameEnum.DEVICE_REGISTRATION, deviceResource, deviceAddress, groupId );
			}
			else if ( RegistrationAuditEnum.REPLACEMENT == registrationAuditInfo.getRegistrationAuditEnum() )
			{
				auditDeviceResource( AuditEventNameEnum.DEVICE_REPLACEMENT, deviceResource, deviceAddress, groupId );
			}

			return deviceResource;
		}
		catch ( DeviceException ex )
		{
			throw TopologyExceptionTranslator.translateDeviceException( ex );
		}
	}

	public void reregisterDeviceResource( Long deviceResourceId, String deviceAddress, String detectedRemoteAddr, String deviceAdmin, String deviceAdminPassword, String deviceSessionId, String deviceId ) throws TopologyException
	{
		DeviceResource deviceResource = null;
		if ( deviceResourceId != null )
		{
			deviceResource = getDeviceResource( deviceResourceId );
			deviceId = deviceResource.getDeviceId().toString();
		}
		else
		{
			deviceResource = getDeviceResourceByDeviceId( deviceId );
		}

		deviceAddress = HttpUtils.setPortOnAddress( deviceAddress, "443" );
		detectedRemoteAddr = HttpUtils.setPortOnAddress( detectedRemoteAddr, HttpUtils.getPortWithoutAddress( deviceAddress ) );

		String remoteIp = CommonAppUtils.getRemoteIpAddressFromSecurityContext();
		String username = CommonAppUtils.getUsernameFromSecurityContext();
		LOG.info( "Re-registration request, client: " + CommonAppUtils.isCommandClientRequest() + ", remoteIp:" + remoteIp + ", address:" + deviceAddress + ( username != null ? ", user:" + username : "" ) + ( deviceId != null ? ", deviceId:" + deviceId : "" ) );

		if ( deviceResource == null )
		{
			LOG.warn( "Device resource {} not found when re-registering.", deviceResourceId );
			throw new TopologyException( TopologyExceptionTypeEnum.DEVICE_NOT_FOUND );
		}
		LOG.debug( "Re-registering device {}.", deviceResource.getId() );

		Map<String, Object> deviceRegistrationInfo = new HashMap();

		if ( !CommonAppUtils.isNullOrEmptyString( deviceSessionId ) )
		{
			deviceRegistrationInfo.put( "securityToken", deviceSessionId );
		}
		else
		{
			deviceRegistrationInfo.put( "admin", deviceAdmin );
			deviceRegistrationInfo.put( "adminPassword", deviceAdminPassword );
		}

		if ( detectedRemoteAddr != null )
		{
			deviceRegistrationInfo.put( "autodetectedaddress", detectedRemoteAddr );
		}

		reregisterDeviceResource( deviceResource.getDeviceId(), deviceAddress, deviceRegistrationInfo );
	}

	private void reregisterDeviceResource( String deviceId, String deviceAddress, Map<String, Object> registrationParams ) throws TopologyException
	{
		if ( !CommonUtils.validateIpAddress( deviceAddress ) )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.INVALID_DEVICE_ADDRESS );
		}
		try
		{
			deviceService.retryRegistration( deviceId, deviceAddress, registrationParams );
		}
		catch ( DeviceException ex )
		{
			throw TopologyExceptionTranslator.translateDeviceException( ex );
		}

		LOG.info( "Device {}/{} scheduled for registration retry.", new String[] {deviceAddress, deviceId} );
	}

	public DeviceResource getDeviceResourceByDeviceId( String deviceId )
	{
		if ( deviceId == null )
		{
			throw new IllegalArgumentException( "DeviceId can't be null" );
		}

		Criteria criteria = new Criteria( DeviceResource.class );
		criteria.add( Restrictions.eq( "deviceId", deviceId ) );

		DeviceResource deviceResource = ( DeviceResource ) getFirstResource( criteria );

		if ( deviceResource == null )
		{
			return null;
		}

		return deviceResource;
	}

	public DeviceResource getDeviceResource( Long deviceResourceId )
	{
		Resource res = TopologyCache.getResource( deviceResourceId );
		if ( ( res == null ) || ( !( res instanceof DeviceResource ) ) )
		{
			return null;
		}
		LOG.debug( "Found DeviceResource {}.", deviceResourceId );
		return ( DeviceResource ) res;
	}

	private void validateLinkResource( LinkResource linkResource ) throws TopologyException
	{
		if ( linkResource.getLinkedResourceIds() != null )
		{
			Resource linkedResource;
			for ( long linkedResourceId : linkResource.getLinkedResourceIds() )
			{
				linkedResource = TopologyCache.getResource( linkedResourceId );
				if ( linkedResource == null )
				{
					throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, "Linked resource " + linkedResourceId + " not found." );
				}

				List<ContainerItem> containerItems = linkResource.getContainerItems();
				if ( containerItems != null )
				{
					for ( ContainerItem containerItem : containerItems )
					{
						if ( containerItem.getId().equals( linkedResourceId ) )
						{
							DeviceResource parent = getDeviceResourceFromChildResource( linkedResource );
							if ( parent != null )
							{
								containerItem.setDeviceResourceId( parent.getId() );
							}
							containerItem.setLinktype( linkedResource.getLinkType() );
						}
					}
				}
			}
		}
	}

	private DeviceResource getDeviceResourceFromChildResource( Resource resource )
	{
		if ( resource == null )
		{
			throw new IllegalArgumentException( "Resource can't be null" );
		}
		Resource parent = resource.getParentResource();
		DeviceResource ret = null;

		if ( ( parent instanceof DeviceResource ) )
		{
			DeviceResource parentDevice = ( DeviceResource ) parent;
			if ( !parentDevice.isRootDevice() )
			{
				parent = parent.getParentResource();
			}
			if ( ( parent instanceof DeviceResource ) )
			{
				ret = ( DeviceResource ) parent;
			}
		}
		return ret;
	}

	public MapResource createMap( MapResource resource, Long parentResourceId, byte[] mapData ) throws TopologyException
	{
		try
		{
			Long mapDataId = mapService.create( mapData );
			resource.setMapDataId( mapDataId );
			return ( MapResource ) createResource( resource, parentResourceId, ResourceAssociationType.MAP.name() );
		}
		catch ( MapException e )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, e.getMessage(), e );
		}
	}

	public MapResource updateMap( MapResource resource, byte[] mapData ) throws TopologyException
	{
		try
		{
			Long mapDataId = mapService.update( resource.getId(), resource.getMapDataId(), mapData );
			resource.setMapDataId( mapDataId );
			return ( MapResource ) updateResource( resource );
		}
		catch ( MapException e )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, e.getMessage(), e );
		}
	}

	public ChannelResource getChannelResource( String deviceId, String channelId )
	{
		DeviceResource rootDevice = getDeviceResourceByDeviceId( deviceId );
		return getChannelResource( rootDevice, channelId );
	}

	public ChannelResource getChannelResource( Long deviceResourceId, String channelId )
	{
		DeviceResource device = getDeviceResource( deviceResourceId );
		return getChannelResource( device, channelId );
	}

	public List<ChannelLinkResource> getChannelLinkResources( String deviceResourceId, String channelId )
	{
		if ( ( deviceResourceId == null ) || ( channelId == null ) )
		{
			return null;
		}
		List<Resource> links = TopologyCache.getResources( new Class[] {ChannelLinkResource.class} );
		List<ChannelLinkResource> result = new ArrayList();

		for ( Resource resource : links )
		{
			ChannelLinkResource link = ( ChannelLinkResource ) resource;
			if ( ( channelId.equals( link.getChannelId() ) ) && ( deviceResourceId.equals( String.valueOf( link.getDeviceResourceId() ) ) ) )
			{
				result.add( link );
			}
		}
		return result;
	}

	public List<Resource> getResources( Class<?>... resourceTypeFilter )
	{
		if ( ( resourceTypeFilter == null ) || ( resourceTypeFilter.length == 0 ) )
		{
			return null;
		}
		List<Resource> result = TopologyCache.getResources( resourceTypeFilter );
		return result;
	}

	public Long getResourceIdByDeviceId( String deviceId )
	{
		Long deviceResourceId = ( Long ) deviceIdsMap.get( deviceId );
		if ( deviceResourceId == null )
		{
			Criteria criteria = new Criteria( DeviceResource.class );
			criteria.add( Restrictions.eq( "deviceId", deviceId ) );

			Resource resource = getFirstResource( criteria );
			if ( resource != null )
			{
				deviceResourceId = resource.getId();
			}
			if ( deviceResourceId == null )
			{
				deviceResourceId = deviceResourceDAO.findResourceIdByDeviceId( deviceId );
			}
			if ( deviceResourceId != null )
			{
				deviceIdsMap.put( deviceId, deviceResourceId );
			}
		}
		return deviceResourceId;
	}

	public Long getChannelResourceId( String deviceId, String channelId )
	{
		ChannelResource resource = getChannelResource( deviceId, channelId );
		if ( resource != null )
		{
			return resource.getId();
		}
		return null;
	}

	public boolean isChild( Long rootID, Long childID )
	{
		if ( rootID == null )
			return false;
		if ( childID == null )
		{
			return true;
		}

		if ( rootID.equals( childID ) )
		{
			return true;
		}
		try
		{
			Resource child = getResource( childID );
			Resource parent = child.getParentResource();

			while ( parent != null )
			{
				if ( parent.getId().equals( rootID ) )
				{
					return true;
				}
				parent = parent.getParentResource();
			}
		}
		catch ( TopologyException e )
		{
			LOG.warn( "Error when looking up for associations {}", e );
		}
		return false;
	}

	public List<Long> getDeviceResourcesFromIdSet( Set<Long> topologyIdSet )
	{
		if ( ( topologyIdSet == null ) || ( topologyIdSet.isEmpty() ) )
		{
			return new LinkedList();
		}

		Criteria criteria = new Criteria( DeviceResource.class );
		criteria.add( Restrictions.in( "id", topologyIdSet ) );
		criteria.add( Restrictions.eq( "deviceView.parentDeviceId", null ) );

		List<Resource> resources = getResources( criteria );
		List<Long> resourceIds = new ArrayList( topologyIdSet );
		if ( resources != null )
		{
			for ( Resource resource : resources )
			{
				resourceIds.add( new Long( ( ( DeviceResource ) resource ).getDeviceId() ) );
			}
		}
		LOG.debug( "getDeviceResourcesFromIdSet() - ids found: {}", resourceIds );

		return resourceIds;
	}

	public void updateDeviceResource( DeviceResource deviceResource ) throws TopologyException
	{
		if ( ( deviceResource == null ) || ( deviceResource.getDeviceView() == null ) || ( deviceResource.getDeviceView().getRegistrationAddress() == null ) )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST );
		}
		DeviceResource storedResource = null;
		try
		{
			storedResource = ( DeviceResource ) getResource( deviceResource.getId() );
		}
		catch ( ClassCastException cce )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.DEVICE_NOT_FOUND );
		}
		String newDeviceAddress = deviceResource.getDeviceView().getRegistrationAddress();
		try
		{
			deviceService.startUpdateDeviceAddress( storedResource.getDeviceId(), newDeviceAddress );
		}
		catch ( DeviceException e )
		{
			throw TopologyExceptionTranslator.translateDeviceException( e );
		}

		auditDeviceResource( AuditEventNameEnum.DEVICE_EDIT_ADDRESS, storedResource, newDeviceAddress, storedResource.getParentResourceId() );
	}

	public Resource createPersonalResource( String userName )
	{
		ResourceEntity resource = new GroupEntity();
		resource.setName( userName );

		LOG.debug( "Creating personal root resource: {}", resource );

		resourceDAO.create( resource );
		Resource ret = resource.toDataObject();

		TopologyCache.addRootResource( ret );

		LOG.debug( "Created personal root resource: {}", ret );

		return ret;
	}

	public void removeUserPersonalRoot( Resource personalRootResource ) throws TopologyException
	{
		ResourceEntity personalRootEntity = ( ResourceEntity ) resourceDAO.findById( personalRootResource.getId() );
		if ( personalRootEntity != null )
		{
			Resource resourceDTO = personalRootEntity.toDataObject( -1, false );
			List<Resource> personalRootResourceList = resourceDTO.createResourceList();

			Collections.reverse( personalRootResourceList );
			for ( Resource resource : personalRootResourceList )
			{
				removeResource( resource.getId() );
			}
		}
	}

	public List<ResourcePathNode> getDeviceResourcePath( String deviceId )
	{
		Long deviceResourceId = getResourceIdByDeviceId( deviceId );
		return getResourcePath( deviceResourceId, new Class[] {Group.class} );
	}

	public String getResourcePathString( Long resourceId )
	{
		Resource resource = null;
		try
		{
			resource = getResource( resourceId );
		}
		catch ( TopologyException e )
		{
			LOG.error( "Exception retrieve resource with id: " + resourceId, e );
		}
		return getResourcePathString( resource, resource == null ? null : resource.getParentResourceId() );
	}

	public String getResourcePathString( Resource resource, Long parentResourceId )
	{
		String result = "N/A";
		if ( resource == null )
		{
			return result;
		}
		List<ResourcePathNode> resourcePath = getResourcePath( parentResourceId, new Class[0] );
		resourcePath.add( 0, new ResourcePathNode( resource.getId(), resource.getName() ) );
		Collections.reverse( resourcePath );
		StringBuilder sb = new StringBuilder();
		for ( ResourcePathNode resourcePathNode : resourcePath )
		{
			if ( !CommonAppUtils.isNullOrEmptyString( resourcePathNode.getName() ) )
			{
				sb.append( resourcePathNode.getName() );
				sb.append( "/" );
			}
		}
		result = sb.toString().substring( 0, sb.length() - 1 );
		return result;
	}

	public List<ResourcePathNode> getResourcePath( Long resourceId )
	{
		return getResourcePath( resourceId, new Class[] {Group.class} );
	}

	public List<ResourcePathNode> getResourcePath( Long resourceId, Class<?>... resourceTypeFilter )
	{
		Resource resource = null;
		try
		{
			resource = getResource( resourceId );
		}
		catch ( TopologyException te )
		{
			LOG.debug( "Exception thrown when looking up for resource {}", resourceId );
		}
		if ( resource == null )
		{
			return new ArrayList();
		}
		return getResourcePath( resource, resourceTypeFilter );
	}

	private List<ResourcePathNode> getResourcePath( Resource resource, Class<?>... resourceTypeFilter )
	{
		List<ResourcePathNode> result = new ArrayList( 1 );

		if ( ( resourceTypeFilter != null ) && ( resourceTypeFilter.length > 0 ) )
		{
			for ( Class<?> clazz : resourceTypeFilter )
			{
				if ( clazz.isInstance( resource ) )
				{
					result.add( new ResourcePathNode( resource.getId(), resource.getName() ) );
					break;
				}
			}
		}
		else
		{
			result.add( new ResourcePathNode( resource.getId(), resource.getName() ) );
		}
		if ( resource.getParentResource() != null )
		{
			Resource parentResource = resource.getParentResource();

			LOG.debug( "Resource {} has parent {}", resource.getId(), parentResource.getId() );

			if ( resource.getId().equals( parentResource.getId() ) )
			{
				LOG.warn( "Resource {} is parent to itself", resource.getId() );
			}
			else
			{
				List<ResourcePathNode> parentPath = getResourcePath( parentResource, new Class[0] );
				result.addAll( parentPath );
			}
		}

		return result;
	}

	private List<Long> getResourcePath( Long resourceId, boolean isExclusionFilter, Class<?>... resourceTypeFilter )
	{
		List<Long> pathNodeIdList = new ArrayList( 1 );

		Resource resource = TopologyCache.getResource( resourceId );
		if ( resource == null )
		{
			return pathNodeIdList;
		}

		if ( ( resourceTypeFilter != null ) && ( resourceTypeFilter.length > 0 ) )
		{
			for ( Class<?> clazz : resourceTypeFilter )
			{
				if ( clazz.isInstance( resource ) != isExclusionFilter )
				{
					pathNodeIdList.add( resource.getId() );
					break;
				}
			}
		}
		else
		{
			pathNodeIdList.add( resource.getId() );
		}
		if ( resource.getParentResourceId() != null )
		{
			List<Long> parentPathIdList = getResourcePath( resource.getParentResourceId(), isExclusionFilter, resourceTypeFilter );
			pathNodeIdList.addAll( parentPathIdList );
		}
		return pathNodeIdList;
	}

	public Long getAlarmSourceResourceId( Long alarmSourceId )
	{
		Criteria criteria = new Criteria( AlarmSourceResource.class );
		criteria.add( Restrictions.eq( "alarmSourceId", alarmSourceId.toString() ) );

		AlarmSourceResource alarmSourceResource = ( AlarmSourceResource ) getFirstResource( criteria );
		if ( alarmSourceResource == null )
		{
			return null;
		}
		return alarmSourceResource.getId();
	}

	public List<LinkResource> getLinkResources( Long linkedResourceId )
	{
		if ( linkedResourceId == null )
			return null;

		Criteria criteria = new Criteria( LinkResource.class );
		criteria.add( Restrictions.contains( "linkedResourceIds", linkedResourceId ) );

		return getResources( criteria );
	}

	public List<Long> getLinkResourceIds( Long linkedResourceId )
	{
		List<LinkResource> linkResources = getLinkResources( linkedResourceId );
		if ( linkResources != null )
		{
			List<Long> linkResourceIds = new ArrayList<>( linkResources.size() );
			for ( LinkResource link : linkResources )
			{
				linkResourceIds.add( link.getId() );
			}
			return linkResourceIds;
		}
		return null;
	}

	public List<Resource> getFilteredResourceList( Long id, Criteria criteria ) throws TopologyException
	{
		Resource resource = getResource( id );
		return TopologyCache.createFilteredResourceList( resource, criteria );
	}

	public List<Resource> getRootResources( ResourceRootType resourceRootType ) throws TopologyException
	{
		String username = CommonAppUtils.getUsernameFromSecurityContext();
		List<Long> userResourceRoots = findUserTerritoryRootIds( username, resourceRootType );
		List<Resource> results = new ArrayList( userResourceRoots.size() );
		for ( Long resourceId : userResourceRoots )
		{
			results.add( getResource( resourceId ) );
		}
		return results;
	}

	public List<DeviceResource> getDeviceResources() throws TopologyException
	{
		List<DeviceResource> result = new ArrayList();

		String authenticatedUser = CommonAppUtils.getUsernameFromSecurityContext();
		MemberView aUser = null;
		try
		{
			aUser = getUserService().getUser( authenticatedUser );
		}
		catch ( UserException e )
		{
			LOG.info( "Failed to obtain data from user {}. Error details: {}", new Object[] {authenticatedUser, e.getMessage()} );
			throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, e.getMessage() );
		}
		if ( aUser == null )
		{
			return new ArrayList();
		}
		Set<Long> usersResourceIds = getAllResourceIdsFromUsersTerritory( aUser );

		List<Long> deviceResourceIds = findAllRootDeviceResourcesIds();

		for ( Long deviceResourceId : deviceResourceIds )
		{
			if ( usersResourceIds.contains( deviceResourceId ) )
			{
				result.add( ( DeviceResource ) TopologyCache.getResource( deviceResourceId ) );
			}
		}
		return result;
	}

	public List<Long> findAllRootDeviceResourcesIds()
	{
		Criteria criteria = new Criteria( DeviceResource.class );

		criteria.add( Restrictions.eq( "deviceView.parentDeviceId", null ) );
		criteria.add( Restrictions.in( "deviceView.registrationStatus", RegistrationStatus.getRegisteredStatuses() ) );

		List<Resource> resources = getResources( criteria );
		List<Long> resourceIds = new ArrayList();
		if ( resources != null )
		{
			for ( Resource resource : resources )
			{
				resourceIds.add( resource.getId() );
			}
		}
		Collections.sort( resourceIds );
		return resourceIds;
	}

	public void removeGenericResources( String owner ) throws TopologyException
	{
		List<Long> resources = genericResourceDAO.findIdsByOwner( owner );
		removeResources( ( Long[] ) resources.toArray( new Long[resources.size()] ) );
	}

	public List<Resource> getResourcesForUser( String username, ResourceRootType type, Criteria criteria, boolean followLinks ) throws TopologyException
	{
		List<Resource> results = new ArrayList();
		List<Long> userRootResourceIds = findUserTerritoryRootIds( username, type );
		for ( Long userResourceId : userRootResourceIds )
		{
			Resource resourceRoot = getResource( userResourceId );
			if ( followLinks )
			{
				List<Resource> childResourceList = TopologyCache.createFilteredResourceList( resourceRoot, criteria );
				for ( Resource resource : childResourceList )
				{
					if ( criteria.match( resource ) )
					{
						results.add( resource );
					}
				}

				List<Resource> linkResourceList = TopologyCache.createFilteredResourceList( resourceRoot, new Criteria( LinkResource.class ) );
				for ( Resource resource : linkResourceList )
				{
					Long[] linkedResourceIds = ( ( LinkResource ) resource ).getLinkedResourceIds();
					for ( Long linkedResourceId : linkedResourceIds )
					{
						Resource linkedResource = getResource( linkedResourceId );

						results.addAll( TopologyCache.createResourceHierarchy( linkedResource, criteria ) );
					}
				}
			}
			else
			{
				results.addAll( TopologyCache.createFilteredResourceList( resourceRoot, criteria ) );
			}
		}
		return results;
	}

	public Resource getFirstResourceByRoot( ResourceRootType type, Criteria criteria )
	{
		Resource resourceRoot = null;
		try
		{
			if ( type == ResourceRootType.SYSTEM )
			{
				resourceRoot = getResource( TopologyConstants.SYSTEM_ROOT_ID );
			}
			if ( type == ResourceRootType.LOGICAL )
			{
				resourceRoot = getResource( TopologyConstants.LOGICAL_ROOT_ID );
			}
			List<Resource> results = TopologyCache.createFilteredResourceList( resourceRoot, criteria );
			if ( !results.isEmpty() )
			{
				return ( Resource ) results.get( 0 );
			}
		}
		catch ( TopologyException localTopologyException )
		{
		}

		return null;
	}

	public List<Resource> getResourcesForUser( ResourceRootType type, ResourceType[] resourceTypeFilter ) throws TopologyException
	{
		Class<?>[] typeFilter = new Class[resourceTypeFilter.length];
		if ( ( resourceTypeFilter != null ) && ( resourceTypeFilter.length > 0 ) )
		{
			for ( int i = 0; i < resourceTypeFilter.length; i++ )
			{
				typeFilter[i] = ResourceType.classFromEnum( resourceTypeFilter[i] );
			}
		}
		List<Resource> results = new ArrayList();

		List<Resource> userResources = getRootResources( type );
		for ( Resource resourceRoot : userResources )
		{
			results.addAll( resourceRoot.createFilteredResourceList( typeFilter ) );
		}
		return results;
	}

	public List<Long> findUserTerritoryRootIds( String username, ResourceRootType rootType ) throws TopologyException
	{
		MemberView aUser = null;
		try
		{
			aUser = getUserService().getUser( username );
		}
		catch ( UserException e )
		{
			LOG.info( "Failed to obtain data from user {}. Error details: {}", new Object[] {username, e.getMessage()} );
			throw new TopologyException( TopologyExceptionTypeEnum.INVALID_REQUEST, e.getMessage() );
		}
		if ( aUser == null )
		{
			return new ArrayList();
		}

		List<Long> userTerritoryRoots = new ArrayList();

		if ( rootType == ResourceRootType.SYSTEM )
		{
			userTerritoryRoots.addAll( aUser.getAssembledSystemRoots() );
		}
		else if ( rootType == ResourceRootType.LOGICAL )
		{
			userTerritoryRoots.addAll( aUser.getAssembledLogicalRoots() );
		}
		else if ( rootType == ResourceRootType.SYSTEM_LOGICAL )
		{
			userTerritoryRoots.addAll( aUser.getAllRoots( false ) );
		}
		else if ( rootType == ResourceRootType.PERSONAL )
		{
			userTerritoryRoots.add( aUser.getPersonalRoot() );
		}
		else if ( rootType == ResourceRootType.ALL )
		{
			userTerritoryRoots.addAll( aUser.getAllRoots( true ) );
		}

		return userTerritoryRoots;
	}

	public List<Resource> getGenericResources( ResourceRootType rootType, String appSpecificId, String... type ) throws TopologyException
	{
		Resource systemRoot = getResource( TopologyConstants.SYSTEM_ROOT_ID );
		Resource logicalRoot = getResource( TopologyConstants.LOGICAL_ROOT_ID );

		List<Resource> resourceList = new ArrayList();

		if ( ( rootType == ResourceRootType.SYSTEM_LOGICAL ) || ( rootType == ResourceRootType.SYSTEM ) )
		{
			resourceList.addAll( systemRoot.createFilteredResourceList( new Class[] {GenericResource.class, GenericLinkResource.class} ) );
		}
		else if ( ( rootType == ResourceRootType.SYSTEM_LOGICAL ) || ( rootType == ResourceRootType.LOGICAL ) )
		{
			resourceList.addAll( logicalRoot.createFilteredResourceList( new Class[] {GenericResource.class, GenericLinkResource.class} ) );
		}

		List<Resource> result = new ArrayList();

		for ( Resource resource : resourceList )
		{
			boolean appSpecificIdMatches = true;
			boolean typeMatches = true;

			String resourceAppSpecificId = null;
			String resourceType = null;

			if ( ( resource instanceof GenericResource ) )
			{
				GenericResource genericResource = ( GenericResource ) resource;
				resourceAppSpecificId = genericResource.getAppSpecificId();
				resourceType = genericResource.getType();
			}
			else if ( ( resource instanceof GenericLinkResource ) )
			{
				GenericLinkResource genericLinkResource = ( GenericLinkResource ) resource;
				resourceAppSpecificId = genericLinkResource.getAppSpecificId();
				resourceType = genericLinkResource.getType();
			}

			if ( appSpecificId != null )
			{
				appSpecificIdMatches = appSpecificId.equals( resourceAppSpecificId );
			}
			if ( ( type.length > 0 ) && ( appSpecificIdMatches ) )
			{
				typeMatches = false;
				for ( String genericType : type )
				{
					if ( genericType.equals( resourceType ) )
					{
						typeMatches = true;
						break;
					}
				}
			}
			if ( ( appSpecificIdMatches ) && ( typeMatches ) )
			{
				result.add( resource );
			}
		}
		return result;
	}

	public <T extends Resource> List<T> getResources( Criteria criteria )
	{
		return TopologyCache.getResources( criteria );
	}

	public Resource getFirstResource( Criteria criteria )
	{
		return TopologyCache.getFirstResource( criteria );
	}

	public List<ResourcePathNode> createGroupResources( Long rootResourceId, List<String> groupResourceNames, boolean flatlist ) throws TopologyException
	{
		Resource rootNode = getResource( rootResourceId );
		List<ResourcePathNode> result = new ArrayList( groupResourceNames.size() );
		for ( String folderName : groupResourceNames )
		{
			List<ResourceAssociation> childrenResource = rootNode.getResourceAssociationsByType( ResourceAssociationType.GROUP.name() );
			boolean foundNode = false;
			for ( ResourceAssociation resourceAssociation : childrenResource )
			{
				if ( resourceAssociation.getResource().getName().equalsIgnoreCase( folderName ) )
				{
					result.add( new ResourcePathNode( resourceAssociation.getResource().getId(), folderName ) );
					foundNode = true;

					if ( !flatlist )
						break;
					rootNode = getResource( resourceAssociation.getResource().getId() );
					break;
				}
			}

			if ( !foundNode )
			{
				Resource folder = createResourceBase( new Group( folderName ), rootNode.getId(), ResourceAssociationType.GROUP.name() );
				result.add( new ResourcePathNode( folder.getId(), folder.getName() ) );

				if ( flatlist )
				{
					rootNode = folder;
				}
			}
		}
		return result;
	}

	public boolean removeGenericResource( String appSpecificId, String type ) throws TopologyException
	{
		Criteria criteria = new Criteria( GenericResource.class );
		criteria.add( Restrictions.eq( "appSpecificId", appSpecificId ) );
		criteria.add( Restrictions.eq( "type", type ) );

		Resource resource = getFirstResource( criteria );
		if ( resource == null )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.RESOURCE_NOT_FOUND );
		}

		return removeResource( resource.getId() );
	}

	public boolean hasRootAccess( String username, ResourceRootType rootType ) throws TopologyException
	{
		boolean hasRootAccess = false;
		if ( !CommonAppUtils.isNullOrEmptyString( username ) )
		{
			List<Long> resourceRoots = findUserTerritoryRootIds( username, rootType );
			switch ( rootType )
			{
				case SYSTEM:
					if ( resourceRoots.contains( TopologyConstants.SYSTEM_ROOT_ID ) )
					{
						return true;
					}

				case LOGICAL:
					if ( resourceRoots.contains( TopologyConstants.LOGICAL_ROOT_ID ) )
					{
						return true;
					}

				case SYSTEM_LOGICAL:
					if ( ( resourceRoots.contains( TopologyConstants.SYSTEM_ROOT_ID ) ) && ( resourceRoots.contains( TopologyConstants.LOGICAL_ROOT_ID ) ) )
					{
						return true;
					}
					break;
			}
			return false;
		}

		return hasRootAccess;
	}

	public boolean hasResourceAccess( String username, Long id ) throws TopologyException
	{
		List<Long> userTerritoryIds = findUserTerritoryRootIds( username, ResourceRootType.SYSTEM_LOGICAL );
		return hasResourceAccess( userTerritoryIds, id );
	}

	public boolean hasResourceAccess( List<Long> userTerritoryRootIds, Long id ) throws TopologyException
	{
		Resource resource = getResource( id );

		Criteria criteria = new Criteria( Group.class );
		List<Resource> resourceHierarchy = TopologyCache.createResourceHierarchy( resource, criteria );
		for ( Resource resourceParent : resourceHierarchy )
		{
			if ( userTerritoryRootIds.contains( resourceParent.getId() ) )
			{
				return true;
			}
		}
		return false;
	}

	protected void auditResource( AuditEventNameEnum auditEvent, Resource resource, Long parentResourceId )
	{
		if ( ( CommonAppUtils.getUsernameFromSecurityContext() != null ) && ( !( resource instanceof GenericResource ) ) )
		{
			AuditView.Builder builder = new AuditView.Builder( auditEvent.getName() );
			if ( ( auditEvent.equals( AuditEventNameEnum.TOPOLOGY_CREATE ) ) && ( !( resource instanceof DeviceResource ) ) )
			{
				builder.addResourceToAudit( resource.getId(), false );
				builder.addDetailsPair( "name", resource.getName() );
				builder.addDetailsPair( "resource_path", getResourcePathString( resource, parentResourceId ) );
			}
			else if ( auditEvent.equals( AuditEventNameEnum.TOPOLOGY_UPDATE ) )
			{
				builder.addResourceToAudit( resource.getId() );
				builder.addDetailsPair( "resource_new_path", getResourcePathString( resource.getId() ) );
			}
			else if ( auditEvent.equals( AuditEventNameEnum.TOPOLOGY_MOVE ) )
			{
				builder.addResourceToAudit( resource.getId() );
				builder.addDetailsPair( "resource_new_path", getResourcePathString( resource, parentResourceId ) + "/" + resource.getName() );
			}
			else if ( auditEvent.equals( AuditEventNameEnum.TOPOLOGY_REMOVE ) )
			{
				builder.addResourceToAudit( resource.getId() );
			}

			eventRegistry.sendEventAfterTransactionCommits( new AuditEvent( builder.build() ) );
		}
	}

	protected void auditDeviceResource( AuditEventNameEnum auditEvent, DeviceResource rootDevice, String registrationAddress, Long parentResourceId )
	{
		if ( CommonAppUtils.getUsernameFromSecurityContext() != null )
		{
			AuditView.Builder auditBuilder = new AuditView.Builder( auditEvent.getName() );
			if ( ( auditEvent.equals( AuditEventNameEnum.DEVICE_REGISTRATION ) ) || ( auditEvent.equals( AuditEventNameEnum.DEVICE_REPLACEMENT ) ) )
			{
				auditBuilder.addDetailsPair( "address", registrationAddress );
				auditBuilder.addDetailsPair( "resource_path", getResourcePathString( parentResourceId ) );
				auditBuilder.addResourceId( rootDevice.getId() );
			}
			else if ( auditEvent.equals( AuditEventNameEnum.DEVICE_EDIT_ADDRESS ) )
			{
				auditBuilder.addRootDeviceToAudit( rootDevice.getDeviceId(), true );
				auditBuilder.addDetailsPair( "new_address", registrationAddress );
			}
			else if ( ( auditEvent.equals( AuditEventNameEnum.DEVICE_UNREGISTRATION ) ) || ( auditEvent.equals( AuditEventNameEnum.DEVICE_MARK_FOR_REPLACEMENT ) ) )
			{
				auditBuilder.addRootDeviceToAudit( rootDevice.getDeviceId(), true );
			}
			eventRegistry.sendEventAfterTransactionCommits( new AuditEvent( auditBuilder.build() ) );
		}
	}

	public List<DeviceResource> getAllDeviceResources()
	{
		Criteria criteria = new Criteria( DeviceResource.class );

		criteria.add( Restrictions.eq( "deviceView.parentDeviceId", null ) );
		criteria.add( Restrictions.in( "deviceView.registrationStatus", RegistrationStatus.getRegisteredStatuses() ) );

		List<Resource> resources = getResources( criteria );
		List<DeviceResource> deviceResources = new ArrayList();
		for ( Resource resource : resources )
		{
			deviceResources.add( ( DeviceResource ) resource );
		}

		return deviceResources;
	}

	public List<String> getChannelIdsFromDevice( Long deviceResourceId ) throws TopologyException
	{
		Resource device = getResource( deviceResourceId );
		if ( device == null )
		{
			throw new TopologyException( TopologyExceptionTypeEnum.DEVICE_NOT_FOUND, "Device Resource Id " + deviceResourceId + " not found." );
		}
		List<String> result = new ArrayList();
		for ( Resource res : device.createFilteredResourceList( new Class[] {ChannelResource.class} ) )
		{
			ChannelResource channelResource = ( ChannelResource ) res;
			result.add( channelResource.getChannelId() );
		}
		return result;
	}

	public String getFirstChannelIdFromDevice( String deviceId )
	{
		ChannelResource channelRes = getFirstChannelResourceFromDevice( deviceId );
		return channelRes == null ? null : channelRes.getChannelId();
	}

	public ChannelResource getFirstChannelResourceFromDevice( String deviceId )
	{
		DeviceResource device = getDeviceResourceByDeviceId( deviceId );
		if ( device != null )
		{
			for ( Resource res : device.createFilteredResourceList( new Class[] {ChannelResource.class} ) )
			{
				ChannelResource channelResource = ( ChannelResource ) res;
				if ( channelResource.getChannelView().getChannelState() != ChannelState.DISABLED )
				{
					return channelResource;
				}
			}
		}

		return null;
	}

	public List<Resource> getArchiverResources()
	{
		Criteria criteria = new Criteria( DeviceResource.class );
		criteria.add( Restrictions.eq( "deviceView.family", "1001" ) );

		List<Resource> archivers = TopologyCache.getResources( criteria );

		return archivers;
	}

	private ChannelResource getChannelResource( DeviceResource deviceResource, String channelId )
	{
		if ( deviceResource != null )
		{
			List<Resource> channels = deviceResource.createFilteredResourceList( new Class[] {ChannelResource.class} );
			for ( Resource resource : channels )
			{
				ChannelResource channel = ( ChannelResource ) resource;
				if ( channel.getChannelId().equals( channelId ) )
				{
					LOG.debug( "Found ChannelResource {}.", channelId );
					return channel;
				}
			}
		}
		return null;
	}

	public List<MassRegistrationInfo> massRegister( List<MassRegistrationInfo> registrationInformation, String securityToken, Long rootFolder )
	{
		registrationInformation = prepareMassRegister( registrationInformation, securityToken, rootFolder );
		doDeviceRegister( registrationInformation );

		return registrationInformation;
	}

	public void stopMassRegistration( Long root ) throws TopologyException
	{
		deviceService.stopMassRegistration();

		if ( root == null )
		{
			root = TopologyConstants.SYSTEM_ROOT_ID;
		}

		Resource rootResource = getResource( root );

		Criteria criteria = new Criteria( DeviceResource.class );
		criteria.add( Restrictions.ne( "deviceView.registrationStatus", RegistrationStatus.INITIAL ) );
		criteria.add( Restrictions.eq( "deviceView.parentDeviceId", null ) );

		clearResources( rootResource, criteria );
	}

	public List<MassRegistrationInfo> prepareMassRegister( List<MassRegistrationInfo> registrationInformation, String securityToken, Long rootFolder )
	{
		Resource copiedRootResource;
		Resource rootResource;

		if ( rootFolder == null )
			rootFolder = TopologyConstants.SYSTEM_ROOT_ID;

		try
		{
			rootResource = getResource( rootFolder );
			copiedRootResource = rootResource.copyFolders();
		}
		catch ( TopologyException e )
		{
			LOG.error( "Cannot get system resource " + rootFolder );
			return null;
		}

		for ( MassRegistrationInfo info : registrationInformation )
		{
			info.setSecurityToken( securityToken );
			info.initializeRegistrationInfo();

			if ( isDeviceResourceNameDuplication( rootResource, info ) )
			{
				info.setException( new TopologyException( TopologyExceptionTypeEnum.DEVICE_DISPLAYNAME_COLLISION ) );

			}
			else if ( !CommonUtils.validateIpAddress( info.getAddress() ) )
			{
				LOG.error( "Device address " + info.getAddress() + " is invalid. Continuing remaining registration tasks." );

				TopologyException topologyException = new TopologyException( TopologyExceptionTypeEnum.INVALID_DEVICE_ADDRESS );
				info.setException( topologyException );
			}
			else
			{
				RegistrationAuditInfo registrationAuditInfo = null;
				try
				{
					registrationAuditInfo = deviceService.registerDeviceResource( info.getAddress(), info.getStationId(), info.getDeviceRegistrationInfo() );
				}
				catch ( DeviceException e )
				{
					if ( e.getDetailedErrorType() == DeviceExceptionTypes.DEVICE_ALREADY_REGISTERED_WITH_THIS_SERVER )
					{
						Criteria criteria = new Criteria( DeviceResource.class );
						criteria.add( Restrictions.eq( "deviceView.registrationAddress", info.getAddress() ) );
						Resource resource = getFirstResource( criteria );
						DeviceResource deviceResource = ( DeviceResource ) resource;
						if ( deviceResource.getDeviceView().getRegistrationStatus() == RegistrationStatus.ERROR_REGISTRATION )
						{
							info.setDeviceId( deviceResource.getDeviceId() );
							info.setResourceId( deviceResource.getId() );
							info.addRegistrationInfo( "useTrusted", Boolean.TRUE );
							info.addRegistrationInfo( "deviceAdress", info.getAddress() );
							deviceResource.getDeviceView().setAdditionalDeviceRegistrationInfo( info.getDeviceRegistrationInfo() );
						}
						else
						{
							LOG.error( "Mass register error, Exception: {}", e.getMessage() );
							TopologyException topologyException = TopologyExceptionTranslator.translateDeviceException( e );
							info.setException( topologyException );
						}
					}
				}

				if ( registrationAuditInfo == null )
					continue;

				if ( RegistrationAuditEnum.REGISTRATION == registrationAuditInfo.getRegistrationAuditEnum() )
				{
					try
					{
						info.setDeviceId( registrationAuditInfo.getDeviceId() );
						if ( info.getFolderPath() != null )
						{
							Resource current = copiedRootResource;
							for ( String folder : info.getFolderPath() )
							{
								Resource child = current.getChildResourceByName( folder );
								if ( child == null )
								{
									child = new Group( folder );
									Resource createdResource = createResource( child, current.getId(), ResourceAssociationType.GROUP.name() );
									child.setId( createdResource.getId() );
									current.createAssociation( child, ResourceAssociationType.GROUP.name() );
								}
								current = child;
							}
							info.setParentId( current.getId() );
						}

						DeviceResource deviceResource = new DeviceResource();
						deviceResource.setDeviceId( info.getDeviceId() );
						String displayName = info.getDisplayName();
						if ( displayName != null )
						{
							deviceResource.setName( displayName );
						}
						deviceResource = ( DeviceResource ) createResource( deviceResource, info.getParentId(), ResourceAssociationType.DEVICE.name() );
						deviceResource.getDeviceView().setAdditionalDeviceRegistrationInfo( info.getDeviceRegistrationInfo() );
						info.setResourceId( deviceResource.getId() );
					}
					catch ( TopologyException e )
					{
						LOG.error( "Error creating topology for device " + info.getAddress() + " in mass registration, Exception: {}", e.getMessage() );
						info.setException( e );
					}
				}
			}
		}

		return registrationInformation;
	}

	public void doDeviceRegister( List<MassRegistrationInfo> registrationInformation )
	{
		MassRegistrationEvent event = new MassRegistrationEvent( registrationInformation );
		eventRegistry.sendEventAfterTransactionCommits( event );
	}

	public DeviceResource findDeviceByStationId( String stationId )
	{
		Criteria criteria = new Criteria( DeviceResource.class ).add( Restrictions.eq( "deviceView.parentDeviceId", null ) ).add( Restrictions.eq( "deviceView.stationId", stationId ) );

		Resource resource = getFirstResource( criteria );

		return ( DeviceResource ) resource;
	}

	public boolean isRegisteringDevice()
	{
		Criteria criteria = new Criteria( DeviceResource.class ).add( Restrictions.eq( "deviceView.parentDeviceId", null ) ).add( Restrictions.in( "deviceView.registrationStatus", new RegistrationStatus[] {RegistrationStatus.PENDING_REGISTRATION, RegistrationStatus.PENDING_REPLACEMENT} ) );

		Resource resource = getFirstResource( criteria );

		return resource != null;
	}

	private void clearResources( Resource resource, Criteria criteria ) throws TopologyException
	{
		boolean containsDevices = resource.containsResource( criteria );

		if ( ( !containsDevices ) && ( !TopologyConstants.SYSTEM_ROOT_ID.equals( resource.getId() ) ) )
		{
			removeResource( resource.getId() );
			return;
		}

		if ( ( resource instanceof DeviceResource ) )
		{
			return;
		}

		List<ResourceAssociation> folderResources = resource.getResourceAssociationsByTypes( new String[] {ResourceAssociationType.GROUP.name(), ResourceAssociationType.DEVICE.name()} );

		for ( ResourceAssociation subFolder : folderResources )
		{
			clearResources( subFolder.getResource(), criteria );
		}
	}

	private void validateRegisterDeviceParams( Long groupId ) throws TopologyException
	{
		if ( !( getResource( groupId ) instanceof Group ) )
		{
			LOG.debug( "Group {} not found to register device in.", groupId );
			throw new TopologyException( TopologyExceptionTypeEnum.GROUP_NOT_FOUND );
		}
	}

	private Long getParentResource( Long parentResourceId ) throws TopologyException
	{
		String username = CommonAppUtils.getUsernameFromSecurityContext();
		if ( ( username != null ) && ( !hasResourceAccess( username, parentResourceId ) ) )
		{
			List<Resource> usersResources = getRootResources( ResourceRootType.SYSTEM );
			if ( usersResources.size() > 0 )
			{
				Resource shortestPath = null;
				int pathLen = Integer.MAX_VALUE;
				for ( Resource resource : usersResources )
				{
					List<ResourcePathNode> pathList = getResourcePath( resource.getId() );
					if ( pathList.size() < pathLen )
					{
						shortestPath = resource;
						pathLen = pathList.size();
					}
				}
				parentResourceId = shortestPath.getId();
			}
			else
			{
				throw new AccessDeniedException( "User not authorized to create resource" );
			}
		}
		return parentResourceId;
	}

	private Resource getChildFolderByName( Resource folder, String childFolderName )
	{
		List<ResourceAssociation> resourceAssociations = folder.getResourceAssociationsByType( ResourceAssociationType.GROUP.name() );
		for ( ResourceAssociation association : resourceAssociations )
		{
			Resource res = association.getResource();
			if ( res.getName().equals( childFolderName ) )
			{
				return res;
			}
		}

		return null;
	}

	private boolean isDeviceResourceNameDuplication( Resource rootResource, MassRegistrationInfo info )
	{
		boolean foundDuplication = false;
		if ( ( info.getFolderPath() != null ) && ( info.getDisplayName() != null ) )
		{
			Resource current = rootResource;
			for ( String folder : info.getFolderPath() )
			{
				Resource child = getChildFolderByName( current, folder );
				if ( child == null )
				{
					return false;
				}
				current = child;
			}

			List<ResourceAssociation> resourceAssociations = current.getResourceAssociationsByType( ResourceAssociationType.DEVICE.name() );
			for ( ResourceAssociation association : resourceAssociations )
			{
				Resource res = association.getResource();
				if ( res.getName().equalsIgnoreCase( info.getDisplayName() ) )
				{
					if ( ( ( DeviceResource ) res ).getDeviceId().equals( info.getDeviceId() ) )
					{
						return false;
					}

					foundDuplication = true;
				}
			}
		}

		return foundDuplication;
	}

	public void setDeviceService( DeviceService deviceService )
	{
		this.deviceService = deviceService;
	}

	public void setDeviceRegistry( DeviceRegistry deviceRegistry )
	{
	}

	public UserService getUserService()
	{
		if ( userService == null )
		{
			userService = ( ( UserService ) ApplicationContextSupport.getBean( "userService_internal" ) );
		}
		return userService;
	}

	public void setUserService( UserService userService )
	{
		this.userService = userService;
	}

	public void setLinkResourceDAO( LinkResourceDAO linkResourceDAO )
	{
		this.linkResourceDAO = linkResourceDAO;
	}

	public void setGenericResourceDAO( GenericResourceDAO genericResourceDAO )
	{
		this.genericResourceDAO = genericResourceDAO;
	}

	public LicenseService getLicenseService()
	{
		if ( licenseService == null )
		{
			licenseService = ( ( LicenseService ) ApplicationContextSupport.getBean( "licenseService_internal" ) );
		}
		return licenseService;
	}

	public ScheduleService getScheduleService()
	{
		if ( scheduleService == null )
		{
			scheduleService = ( ( ScheduleService ) ApplicationContextSupport.getBean( "scheduleService" ) );
		}
		return scheduleService;
	}

	public void setMapService( MapService mapService )
	{
		this.mapService = mapService;
	}

	public void setResourceFactory( ResourceFactory resourceFactory )
	{
		this.resourceFactory = resourceFactory;
	}

	public void setEventRegistry( EventRegistry eventRegistry )
	{
		this.eventRegistry = eventRegistry;
	}

	public void setResourceDAO( ResourceDAO<ResourceEntity> resourceDAO )
	{
		this.resourceDAO = resourceDAO;
	}

	public void setResourceAssociationDAO( ResourceAssociationDAO resourceAssociationDAO )
	{
		this.resourceAssociationDAO = resourceAssociationDAO;
	}

	public void setDeviceResourceDAO( DeviceResourceDAO deviceResourceDAO )
	{
		this.deviceResourceDAO = deviceResourceDAO;
	}

	public void setDefaultRootResourceDAO( DefaultRootResourceDAO defaultRootResourceDAO )
	{
		this.defaultRootResourceDAO = defaultRootResourceDAO;
	}

	public String[] getDefaultRoots()
	{
		return defaultRoots;
	}

	public void setDefaultRoots( String[] defaultRoots )
	{
		this.defaultRoots = defaultRoots;
	}

	public OsgiService getOsgiService()
	{
		if ( osgiService == null )
		{
			osgiService = ( ( OsgiService ) ApplicationContextSupport.getBean( "osgiManager" ) );
		}
		return osgiService;
	}

	private String getMetaData( String deviceId, String channelId, String channelName )
	{
		if ( channelName.contains( "<" ) )
		{
			channelName = channelName.replace( "<", "&lt;" );
		}

		if ( channelName.contains( ">" ) )
		{
			channelName = channelName.replace( ">", "&gt;" );
		}

		String xml = "<?xml version=\"1.0\" encoding=\"utf-16\"?><channel deviceId=\"" + deviceId + "\" channelid=\"" + channelId + "\" cacheDisplayChannelName=\"" + channelName + "\" encoderMode=\"AUTOMATIC\" encoderHint=\"HIGH\" " + " isVideoInfoVisible=\"False\" isGPSInfoVisible=\"False\" isTitleBarVisible=\"True\" isStretchVideoDisplay=\"False\" isTextAreaVisible=\"True\" textAreaFontSize=\"Auto\" " + " textAreaDisplayPeriod=\"150000000\" textAreaTop=\"0.02\" textAreaLeft=\"0.02\" textAreaWidth=\"0.96\" textAreaHeight=\"0.96\" cropLeft=\"-1\" cropTop=\"-1\" " + " cropRight=\"-1\" cropBottom=\"-1\" useDewarping=\"False\" viewType=\"-1\" virtualPan=\"0\" virtualTilt=\"0\" virtualZoom=\"0\" metadataVisualizationCategories=\"\"  />";

		return xml;
	}
}

