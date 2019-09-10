package com.marchnetworks.casemanagement.service;

import com.marchnetworks.audit.data.AuditEventNameEnum;
import com.marchnetworks.audit.data.AuditView;
import com.marchnetworks.audit.events.AuditEvent;
import com.marchnetworks.casemanagement.dao.CaseDAO;
import com.marchnetworks.casemanagement.dao.CaseNodeDAO;
import com.marchnetworks.casemanagement.model.CaseEntity;
import com.marchnetworks.casemanagement.model.CaseNodeEntity;
import com.marchnetworks.casemanagementservice.common.CaseManagementException;
import com.marchnetworks.casemanagementservice.common.CaseManagementExceptionTypeEnum;
import com.marchnetworks.casemanagementservice.common.CaseNodeType;
import com.marchnetworks.casemanagementservice.data.Case;
import com.marchnetworks.casemanagementservice.data.CaseManagementConstants;
import com.marchnetworks.casemanagementservice.data.CaseNode;
import com.marchnetworks.casemanagementservice.data.LocalGroup;
import com.marchnetworks.casemanagementservice.service.CaseManagementService;
import com.marchnetworks.casemanagementservice.service.LocalGroupService;
import com.marchnetworks.command.api.extractor.BaseExtractionService;
import com.marchnetworks.command.api.extractor.data.MediaDownloadJob;
import com.marchnetworks.command.api.initialization.InitializationListener;
import com.marchnetworks.command.common.CollectionUtils;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.device.DeviceEventsEnum;
import com.marchnetworks.command.common.device.data.ChannelState;
import com.marchnetworks.command.common.device.data.RegistrationStatus;
import com.marchnetworks.command.common.extractor.data.CompletionState;
import com.marchnetworks.command.common.extractor.data.State;
import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.topology.data.ChannelResource;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.command.common.user.UserException;
import com.marchnetworks.common.cache.Cache;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.management.instrumentation.events.DeviceRegistrationEvent;
import com.marchnetworks.management.instrumentation.events.ExtractorJobEvent;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;
import com.marchnetworks.management.topology.events.ResourceRemovedEvent;
import com.marchnetworks.management.topology.events.TopologyEvent;
import com.marchnetworks.management.user.UserService;
import com.marchnetworks.management.user.events.UserRemovedEvent;
import com.marchnetworks.server.event.EventListener;
import com.marchnetworks.server.event.EventRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class CaseManagementServiceImpl implements CaseManagementService, EventListener, InitializationListener
{
	private static final Logger LOG = LoggerFactory.getLogger( CaseManagementServiceImpl.class );

	private CaseDAO caseDAO;
	private CaseNodeDAO caseNodeDAO;
	private Cache<Long, byte[]> caseCache;
	private EventRegistry eventRegistry;
	private UserService userService;
	private LocalGroupService groupService;
	private ResourceTopologyServiceIF topologyService;
	private BaseExtractionService extractionService;

	public void onAppInitialized()
	{
		List<CaseNodeEntity> caseNodes = caseNodeDAO.findAllWithGuid();
		if ( caseNodes.isEmpty() )
		{
			return;
		}
		List<MediaDownloadJob> mediaJobs = new ArrayList( caseNodes.size() );
		for ( CaseNodeEntity nodeEntity : caseNodes )
		{
			MediaDownloadJob job = createMediaDownloadJob( nodeEntity );
			if ( job != null )
			{
				mediaJobs.add( job );
			}
		}
		extractionService.updateMediaJobs( mediaJobs );
	}

	public void process( Event event )
	{
		if ( ( event instanceof ResourceRemovedEvent ) )
		{
			TopologyEvent topologyEvent = ( TopologyEvent ) event;

			List<CaseNodeEntity> entities = caseNodeDAO.findByResourceId( topologyEvent.getResourceId() );

			List<MediaDownloadJob> mediaJobs = new ArrayList( 1 );
			for ( Iterator<CaseNodeEntity> iterator = entities.iterator(); iterator.hasNext(); )
			{
				CaseNodeEntity nodeEntity = ( CaseNodeEntity ) iterator.next();
				caseNodeDAO.deleteById( nodeEntity.getId() );
				if ( nodeEntity.getType() == CaseNodeType.CHANNEL )
				{
					MediaDownloadJob job = new MediaDownloadJob();
					job.setJobId( nodeEntity.getGuid() );
					job.setReferenceId( nodeEntity.getId().toString() );
					job.setExtractorId( nodeEntity.getExtractorId() );
					mediaJobs.add( job );
				}
			}
			extractionService.removeMediaJobs( mediaJobs );
		}
		else if ( ( event instanceof DeviceRegistrationEvent ) )
		{
			DeviceRegistrationEvent registrationEvent = ( DeviceRegistrationEvent ) event;
			if ( registrationEvent.getRegistrationStatus() == RegistrationStatus.REGISTERED )
			{
				DeviceResource deviceResource = topologyService.getDeviceResource( registrationEvent.getResourceId() );
				if ( !extractionService.extractorExists( deviceResource.getId() ) )
				{
					return;
				}

				List<CaseNodeEntity> nodeEntities = caseNodeDAO.findAllBySerial( deviceResource.getDeviceView().getSerial() );
				if ( nodeEntities.isEmpty() )
				{
					return;
				}
				List<MediaDownloadJob> mediaJobs = new ArrayList( nodeEntities.size() );
				for ( CaseNodeEntity nodeEntity : nodeEntities )
				{
					nodeEntity.setExtractorId( deviceResource.getId() );
					MediaDownloadJob job = createMediaDownloadJob( nodeEntity );
					if ( job != null )
					{
						mediaJobs.add( job );
					}
				}
				extractionService.updateMediaJobs( mediaJobs );
			}
		}
		else if ( ( event instanceof UserRemovedEvent ) )
		{
			UserRemovedEvent userEvent = ( UserRemovedEvent ) event;
			List<CaseEntity> cases = caseDAO.getAllCases( userEvent.getUserName(), false );
			for ( CaseEntity caseEntity : cases )
			{
				caseEntity.setMember( null );
			}
		}
		else if ( ( event instanceof ExtractorJobEvent ) )
		{
			ExtractorJobEvent extractorEvent = ( ExtractorJobEvent ) event;
			if ( extractorEvent.getName().equals( DeviceEventsEnum.EXTRACTOR_MEDIA_JOB.getPath() ) )
			{
				CaseNodeEntity caseNodeEntity = caseNodeDAO.findByGuid( extractorEvent.getSource() );
				if ( caseNodeEntity != null )
				{
					State state = State.fromValue( ( String ) extractorEvent.getValue() );
					CompletionState completionState = CompletionState.fromValue( extractorEvent.getInfo( "CompletionState" ) );
					caseNodeEntity.setState( state );
					caseNodeEntity.setCompletionState( completionState );
				}
			}
		}
	}

	public String getListenerName()
	{
		return CaseManagementServiceImpl.class.getSimpleName();
	}

	public Case createCase( Case newCase, String userName ) throws CaseManagementException
	{
		if ( CommonAppUtils.isNullOrEmptyString( newCase.getName() ) )
		{
			throw new CaseManagementException( CaseManagementExceptionTypeEnum.CASE_REQUIRED_FIELD_NOT_SET, "Case name not set" );
		}
		if ( CommonAppUtils.isNullOrEmptyString( newCase.getMember() ) )
		{
			throw new CaseManagementException( CaseManagementExceptionTypeEnum.CASE_REQUIRED_FIELD_NOT_SET, "Case owner not set" );
		}

		List<CaseNode> childNodes = newCase.getCaseNodes();
		for ( CaseNode node : childNodes )
		{
			validateCaseNode( node );
		}

		CaseEntity entity = new CaseEntity();
		entity.readFromDataObject( newCase );

		caseDAO.create( entity );

		List<CaseNodeEntity> childNodeEntities = new ArrayList();
		List<MediaDownloadJob> mediaJobList = new ArrayList( childNodes.size() );

		if ( childNodes != null )
		{
			for ( CaseNode childNode : childNodes )
			{
				CaseNodeEntity childNodeEntity = createCaseNodeEntity( entity, childNode );
				childNodeEntities.add( childNodeEntity );
			}
		}
		entity.getChildNodes().addAll( childNodeEntities );

		newCase.setId( entity.getId() );

		for ( CaseNodeEntity nodeEntity : entity.getChildNodes() )
		{
			for ( CaseNodeEntity childNodeEntity : nodeEntity.getAllCaseNodes() )
			{
				MediaDownloadJob mediaJob = createMediaDownloadJob( childNodeEntity );
				if ( mediaJob != null )
				{
					mediaJobList.add( mediaJob );
				}
			}
		}
		extractionService.updateMediaJobs( mediaJobList );

		auditCaseOperation( AuditEventNameEnum.CASE_CREATE, newCase );

		return newCase;
	}

	public void removeCase( Long caseId, String userName ) throws CaseManagementException
	{
		CaseEntity entity = ( CaseEntity ) caseDAO.findById( caseId );

		if ( entity == null )
		{
			throw new CaseManagementException( CaseManagementExceptionTypeEnum.CASE_NOT_FOUND, "Case with Case ID = " + caseId + " not found in Command Server." );
		}
		try
		{
			if ( !isRemovalAllowed( entity, userName ) )
			{
				throw new CaseManagementException( CaseManagementExceptionTypeEnum.NOT_AUTHORIZED, "Case with Case ID = " + caseId + " does not belong to user " + userName );
			}

			if ( extractionService.extractorExists() )
			{
				List<MediaDownloadJob> jobsToDelete = new ArrayList( 1 );
				for ( CaseNodeEntity caseNodeEntity : entity.getChildNodes() )
				{
					for ( CaseNodeEntity childNodeEntity : caseNodeEntity.getAllCaseNodes() )
					{
						MediaDownloadJob job = new MediaDownloadJob();
						job.setJobId( childNodeEntity.getGuid() );
						job.setReferenceId( childNodeEntity.getId().toString() );
						job.setExtractorId( childNodeEntity.getExtractorId() );
						jobsToDelete.add( job );
					}
				}
				extractionService.removeMediaJobsAsync( jobsToDelete );
			}

			caseDAO.delete( entity );
			auditCaseOperation( AuditEventNameEnum.CASE_DELETE, entity.toDataObject( false ) );

		}
		catch ( UserException e )
		{
			throw new CaseManagementException( e );
		}
	}

	public Case getCase( Long caseId, String userName ) throws CaseManagementException
	{
		CaseEntity entity = ( CaseEntity ) caseDAO.findById( caseId );
		if ( entity == null )
		{
			throw new CaseManagementException( CaseManagementExceptionTypeEnum.CASE_NOT_FOUND, "Case with Case ID = " + caseId + " not found in Command Server." );
		}

		if ( !isAuthorized( userName, entity ) )
		{
			throw new CaseManagementException( CaseManagementExceptionTypeEnum.NOT_AUTHORIZED, "Case with Case ID = " + caseId + " does not belong to user " + userName );
		}

		return entity.toDataObject( true );
	}

	public Case updateCase( Case updatedCase, String userName ) throws CaseManagementException
	{
		if ( CommonAppUtils.isNullOrEmptyString( updatedCase.getName() ) )
		{
			throw new CaseManagementException( CaseManagementExceptionTypeEnum.CASE_REQUIRED_FIELD_NOT_SET, "Case name not set" );
		}

		for ( CaseNode node : updatedCase.getCaseNodes() )
		{
			validateCaseNode( node );
		}

		Long id = updatedCase.getId();
		CaseEntity entity = ( CaseEntity ) caseDAO.findById( id );
		if ( entity == null )
		{
			throw new CaseManagementException( CaseManagementExceptionTypeEnum.CASE_NOT_FOUND, "Case with Case ID = " + id + " not found in Command Server." );
		}

		if ( !isAuthorized( userName, entity ) )
		{
			throw new CaseManagementException( CaseManagementExceptionTypeEnum.NOT_AUTHORIZED, "Case with Case ID = " + id + " cannot be seen by user " + userName );
		}

		entity.readFromDataObject( updatedCase );

		List<MediaDownloadJob> mediaJobsToUpdate = new ArrayList( 1 );
		List<MediaDownloadJob> mediaJobsToDelete = new ArrayList( 1 );

		List<CaseNode> updatedChildren = updatedCase.getCaseNodes();
		CaseNode grandChild;
		if ( updatedChildren != null )
		{
			List<Long> currentNodes = new ArrayList();

			for ( CaseNode caseNode : updatedChildren )
			{
				if ( caseNode.getId() != null )
				{
					currentNodes.add( caseNode.getId() );
				}
			}

			for ( Iterator<CaseNodeEntity> iterator = entity.getChildNodes().iterator(); iterator.hasNext(); )
			{
				CaseNodeEntity caseNodeEntity = ( CaseNodeEntity ) iterator.next();

				if ( !currentNodes.contains( caseNodeEntity.getId() ) )
				{
					iterator.remove();
					if ( caseNodeEntity.getType() == CaseNodeType.CHANNEL )
					{
						MediaDownloadJob mediaJob = new MediaDownloadJob();
						mediaJob.setJobId( caseNodeEntity.getGuid() );
						mediaJob.setExtractorId( caseNodeEntity.getExtractorId() );
						mediaJobsToDelete.add( mediaJob );
					}
				}
			}

			for ( CaseNode node : updatedChildren )
			{
				CaseNodeEntity nodeEntity = node.getId() == null ? null : caseNodeDAO.findCaseNode( node.getId(), true );

				if ( nodeEntity == null )
				{
					nodeEntity = createCaseNodeEntity( entity, node );
					for ( CaseNodeEntity childNodeEntity : nodeEntity.getAllCaseNodes() )
					{
						MediaDownloadJob mediaJob = createMediaDownloadJob( childNodeEntity );
						if ( mediaJob != null )
						{
							mediaJobsToUpdate.add( mediaJob );
						}
					}
				}
				else
				{
					nodeEntity.readMetaDataFromDataObject( node );
					if ( ( node.getChildNodes() != null ) && ( !node.getChildNodes().isEmpty() ) )
					{
						grandChild = node.getFirstChildNodeByType( CaseNodeType.NOTE );
						for ( CaseNodeEntity grandChildEntity : nodeEntity.getChildNodes() )
						{
							if ( grandChildEntity.getId().equals( grandChild.getId() ) )
							{
								grandChildEntity.readMetaDataFromDataObject( grandChild );
								break;
							}
						}
					}
				}
			}
		}
		else
		{
			CaseNodeEntity caseNodeEntity;

			for ( Iterator i$ = entity.getChildNodes().iterator(); i$.hasNext(); )
			{
				caseNodeEntity = ( CaseNodeEntity ) i$.next();
				for ( CaseNodeEntity childNodeEntity : caseNodeEntity.getAllCaseNodes() )
					if ( childNodeEntity.getType() == CaseNodeType.CHANNEL )
					{
						MediaDownloadJob mediaJob = new MediaDownloadJob();
						mediaJob.setJobId( caseNodeEntity.getGuid() );
						mediaJob.setExtractorId( caseNodeEntity.getExtractorId() );
						mediaJobsToDelete.add( mediaJob );
					}
			}

			entity.getChildNodes().clear();
		}

		extractionService.updateMediaJobs( mediaJobsToUpdate );
		extractionService.removeMediaJobsAsync( mediaJobsToDelete );

		auditCaseOperation( AuditEventNameEnum.CASE_UPDATE, updatedCase );

		return updatedCase;
	}

	public List<Case> getAllCases( String userName )
	{
		Set<CaseEntity> caseEntities = new HashSet();

		List<Long> userGroupIds = groupService.getAllIdsByUser( userName );
		caseEntities.addAll( caseDAO.getAllCases( userName, userGroupIds ) );

		if ( userService.isSuperAdmin( userName ) )
		{
			List<CaseEntity> allOrphan = caseDAO.getAllOrphan();
			caseEntities.addAll( allOrphan );
		}

		List<Case> cases = new ArrayList( caseEntities.size() );
		for ( CaseEntity entity : caseEntities )
		{
			cases.add( entity.toDataObject( false ) );
		}

		return cases;
	}

	public byte[] getCaseNodeAttachment( Long caseNodeId, String userName ) throws CaseManagementException
	{
		CaseNodeEntity entity = caseNodeDAO.findCaseNode( caseNodeId, true );
		if ( entity == null )
		{
			throw new CaseManagementException( CaseManagementExceptionTypeEnum.CASENODE_NOT_FOUND, "Case node with Case ID = " + caseNodeId + " not found in Command Server." );
		}

		CaseEntity caseEntity = entity.getCase();
		if ( !isAuthorized( userName, caseEntity ) )
		{
			throw new CaseManagementException( CaseManagementExceptionTypeEnum.NOT_AUTHORIZED, "Case node with does not belong to user " + userName );
		}
		return entity.getAttachment();
	}

	public String getAttachmentTag( Long caseNodeId )
	{
		String tag = caseCache.getTag( caseNodeId );

		if ( tag == null )
		{
			tag = caseCache.createTag( caseNodeId );
		}

		return tag;
	}

	public void removeGroupFromCases( Long groupId )
	{
		List<CaseEntity> allByGroupId = caseDAO.getAllByGroupId( groupId );
		for ( CaseEntity caseEntity : allByGroupId )
		{
			List<Long> caseGroups = caseEntity.getGroups();
			caseGroups.remove( groupId );
			caseEntity.setGroups( caseGroups );
		}
	}

	private CaseNodeEntity createCaseNodeChildren( CaseNodeEntity parentEntity, CaseNode childNodeDataObject )
	{
		CaseNodeEntity childEntity = buildCaseNodeEntity( childNodeDataObject );
		caseNodeDAO.create( childEntity );

		childEntity.setParentNode( parentEntity );
		childNodeDataObject.setId( childEntity.getId() );

		if ( parentEntity != null )
		{
			parentEntity.addChildNode( childEntity );
		}

		if ( childNodeDataObject.getChildNodes() != null )
		{
			for ( CaseNode grandChildNode : childNodeDataObject.getChildNodes() )
			{
				createCaseNodeChildren( childEntity, grandChildNode );
			}
		}

		return childEntity;
	}

	private CaseNodeEntity buildCaseNodeEntity( CaseNode caseNodeDataObject )
	{
		CaseNodeEntity caseNodeEntity = new CaseNodeEntity();
		caseNodeEntity.readMetaDataFromDataObject( caseNodeDataObject );
		if ( caseNodeDataObject.getType() == CaseNodeType.CHANNEL )
		{
			try
			{
				ChannelResource channelResource = ( ChannelResource ) topologyService.getResource( caseNodeDataObject.getAssociatedResource() );
				if ( channelResource.getChannelView().getChannelState() != ChannelState.DISABLED )
				{
					DeviceResource deviceResource = getDeviceResource( channelResource.getParentResourceId() );
					Long extractorId = extractionService.getExtractorIdForDevice( deviceResource.getId() );
					if ( extractorId != null )
					{
						DeviceResource device = getDeviceResource( extractorId );
						caseNodeEntity.setGuid( UUID.randomUUID().toString() );
						caseNodeEntity.setState( State.CREATED );
						caseNodeEntity.setExtractorId( extractorId );
						caseNodeEntity.setExtractorSerial( device.getDeviceView().getSerial() );
					}
				}
			}
			catch ( TopologyException e )
			{
				LOG.debug( "Channel associated to case node {} not found in topology", caseNodeDataObject.getName() );
			}
		}
		return caseNodeEntity;
	}

	private boolean isRemovalAllowed( CaseEntity entity, String userName ) throws UserException
	{
		if ( entity.isOrphan() )
		{
			return userService.isSuperAdmin( userName );
		}
		if ( entity.getMember().equals( userName ) )
		{
			return true;
		}

		return false;
	}

	private boolean isAuthorized( String userName, CaseEntity entity ) throws CaseManagementException
	{
		boolean memberMatched = ( !entity.isOrphan() ) && ( entity.getMember().equals( userName ) );
		return ( memberMatched ) || ( groupService.isUserMemberOf( userName, entity.getGroups() ) ) || ( ( entity.isOrphan() ) && ( userService.isSuperAdmin( userName ) ) );
	}

	private DeviceResource getDeviceResource( Long resourceId )
	{
		DeviceResource deviceResource = topologyService.getDeviceResource( resourceId );
		if ( ( deviceResource != null ) && ( !deviceResource.isRootDevice() ) )
		{
			return topologyService.getDeviceResource( deviceResource.getParentResourceId() );
		}
		return deviceResource;
	}

	private void auditCaseOperation( AuditEventNameEnum auditEvent, Case caseDataObject )
	{
		if ( CommonAppUtils.getUsernameFromSecurityContext() != null )
		{
			AuditView.Builder auditBuilder = new AuditView.Builder( auditEvent.getName() );
			auditBuilder.addDetailsPair( "name", caseDataObject.getName() );
			if ( ( auditEvent == AuditEventNameEnum.CASE_CREATE ) || ( auditEvent == AuditEventNameEnum.CASE_UPDATE ) )
			{
				if ( ( caseDataObject.getGroupIds() != null ) && ( !caseDataObject.getGroupIds().isEmpty() ) )
				{
					List<LocalGroup> groups = groupService.getAllByIds( caseDataObject.getGroupIds() );
					auditBuilder.addDetailsPair( "shared_with", CollectionUtils.getStringFromList( groups, "name" ) );
				}

				List<CaseNode> caseNodes = caseDataObject.getNodesOfType( new CaseNodeType[] {CaseNodeType.NOTE} );
				auditBuilder.addDetailsPair( "notes", CollectionUtils.getStringFromList( caseNodes, "name" ) );

				caseNodes = caseDataObject.getNodesOfType( new CaseNodeType[] {CaseNodeType.SNAPSHOT, CaseNodeType.CHANNEL, CaseNodeType.VIEW} );
				auditBuilder.addDetailsPair( "media", CollectionUtils.getStringFromList( caseNodes, "name" ) );
			}
			eventRegistry.sendEventAfterTransactionCommits( new AuditEvent( auditBuilder.build() ) );
		}
	}

	private CaseNodeEntity createCaseNodeEntity( CaseEntity parentCase, CaseNode caseNode )
	{
		CaseNodeEntity childNodeEntity = createCaseNodeChildren( null, caseNode );
		childNodeEntity.setCaseEntity( parentCase );

		return childNodeEntity;
	}

	private void validateCaseNode( CaseNode caseNode ) throws CaseManagementException
	{
		if ( CommonAppUtils.isNullOrEmptyString( caseNode.getName() ) )
		{
			throw new CaseManagementException( CaseManagementExceptionTypeEnum.CASENODE_REQUIRED_FIELD_NOT_SET, "Case node name not set" );
		}
		if ( caseNode.getType() == null )
		{
			throw new CaseManagementException( CaseManagementExceptionTypeEnum.CASENODE_REQUIRED_FIELD_NOT_SET, "Case type not set" );
		}
	}

	private MediaDownloadJob createMediaDownloadJob( CaseNodeEntity caseNode )
	{
		if ( caseNode.getExtractorId() != null )
		{
			try
			{
				MediaDownloadJob mediaJob = new MediaDownloadJob();
				mediaJob.setJobId( caseNode.getGuid() );
				mediaJob.setState( caseNode.getState() );
				mediaJob.setCompletionState( caseNode.getCompletionState() );
				mediaJob.setReferenceId( caseNode.getId().toString() );
				mediaJob.setExtractorId( caseNode.getExtractorId() );

				ChannelResource channelResource = ( ChannelResource ) topologyService.getResource( caseNode.getAssociatedResourceId() );
				mediaJob.addChannel( channelResource.getChannelId(), caseNode.getSectorId() );
				if ( channelResource.getChannelView().getAssocIds() != null )
				{
					for ( String associatedId : channelResource.getChannelView().getAssocIds() )
					{
						mediaJob.addChannel( associatedId, caseNode.getSectorId() );
					}
				}

				DeviceResource deviceResource = getDeviceResource( channelResource.getParentResourceId() );
				mediaJob.setDeviceId( deviceResource.getId() );

				mediaJob.setStartTime( Long.valueOf( caseNode.getEvidenceStartTime().longValue() - CaseManagementConstants.TICK_AT_EPOCH.longValue() ) );
				mediaJob.setEndTime( Long.valueOf( caseNode.getEvidenceEndTime().longValue() - CaseManagementConstants.TICK_AT_EPOCH.longValue() ) );
				return mediaJob;
			}
			catch ( TopologyException e )
			{
				LOG.info( "Failed to lookup channel in topology. Details:" + e.getMessage() );
			}
		}

		return null;
	}

	public void setTopologyService( ResourceTopologyServiceIF topologyService )
	{
		this.topologyService = topologyService;
	}

	public void setExtractionService( BaseExtractionService extractionService )
	{
		this.extractionService = extractionService;
	}

	public void setCaseDAO( CaseDAO caseDAO )
	{
		this.caseDAO = caseDAO;
	}

	public void setCaseNodeDAO( CaseNodeDAO caseNodeDAO )
	{
		this.caseNodeDAO = caseNodeDAO;
	}

	public void setCaseCache( Cache<Long, byte[]> caseCache )
	{
		this.caseCache = caseCache;
	}

	public void setEventRegistry( EventRegistry eventRegistry )
	{
		this.eventRegistry = eventRegistry;
	}

	public void setUserService( UserService userService )
	{
		this.userService = userService;
	}

	public void setGroupService( LocalGroupService groupService )
	{
		this.groupService = groupService;
	}
}
