package com.marchnetworks.casemanagement.service;

import com.marchnetworks.audit.data.AuditEventNameEnum;
import com.marchnetworks.audit.data.AuditView;
import com.marchnetworks.audit.events.AuditEvent;
import com.marchnetworks.casemanagement.dao.LocalGroupDAO;
import com.marchnetworks.casemanagement.model.LocalGroupEntity;
import com.marchnetworks.casemanagementservice.data.LocalGroup;
import com.marchnetworks.casemanagementservice.data.LocalGroupException;
import com.marchnetworks.casemanagementservice.service.CaseManagementService;
import com.marchnetworks.casemanagementservice.service.LocalGroupService;
import com.marchnetworks.command.common.CollectionUtils;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.management.user.events.UserLogoffEvent;
import com.marchnetworks.management.user.events.UserRemovedEvent;
import com.marchnetworks.server.event.EventListener;
import com.marchnetworks.server.event.EventRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class LocalGroupServiceImpl implements LocalGroupService, EventListener
{
	private static final Logger LOG = LoggerFactory.getLogger( LocalGroupServiceImpl.class );

	private LocalGroupDAO localGroupDAO;
	private EventRegistry eventRegistry;
	private CaseManagementService caseService;

	public void process( Event event )
	{
		UserRemovedEvent userRemovedEvent;
		if ( ( event instanceof UserRemovedEvent ) )
		{
			userRemovedEvent = ( UserRemovedEvent ) event;
			List<LocalGroupEntity> findByUser = localGroupDAO.findByUser( userRemovedEvent.getUserName() );
			if ( findByUser != null )
			{
				for ( LocalGroupEntity entity : findByUser )
				{
					List<String> updatedMemberList = entity.getMembers();
					updatedMemberList.remove( userRemovedEvent.getUserName() );
					entity.setMembers( updatedMemberList );
				}
			}
		}
	}

	public String getListenerName()
	{
		return LocalGroupServiceImpl.class.getSimpleName();
	}

	public LocalGroup create( LocalGroup group ) throws LocalGroupException
	{
		if ( CommonAppUtils.isNullOrEmptyString( group.getName() ) )
		{
			throw new LocalGroupException( "New Local Group must have a name.", LocalGroupException.LocalGroupExceptionType.LOCAL_GROUP_NAME_NOT_SET );
		}

		LocalGroupEntity localGroupEntity = localGroupDAO.findLocalGroupByName( group.getName() );

		if ( localGroupEntity != null )
		{
			String error = "The Local Group " + group.getName() + " already exists on DB.";
			LOG.warn( error );
			throw new LocalGroupException( error, LocalGroupException.LocalGroupExceptionType.LOCAL_GROUP_NAME_ALREADY_EXISTS );
		}

		LocalGroupEntity entity = new LocalGroupEntity();
		entity.readFromDataObject( group );

		localGroupDAO.create( entity );
		group.setId( entity.getId() );

		auditLocalGroupOperation( AuditEventNameEnum.LOCAL_GROUP_CREATE, group.getName() );

		logOffUsers( group.getUsersnames() );

		return group;
	}

	public void delete( Long localGroupId ) throws LocalGroupException
	{
		LocalGroupEntity entity = ( LocalGroupEntity ) localGroupDAO.findById( localGroupId );

		if ( entity == null )
		{
			throw new LocalGroupException( "Local Group " + localGroupId + " not found in command server.", LocalGroupException.LocalGroupExceptionType.LOCAL_GROUP_NOT_FOUND );
		}

		logOffUsers( entity.getMembers() );

		localGroupDAO.delete( entity );
		auditLocalGroupOperation( AuditEventNameEnum.LOCAL_GROUP_DELETE, entity.getName() );

		caseService.removeGroupFromCases( localGroupId );
	}

	public List<LocalGroup> getAll()
	{
		List<LocalGroupEntity> entities = localGroupDAO.findAllDetached();
		List<LocalGroup> localGroups = new ArrayList( entities.size() );
		for ( LocalGroupEntity localGroupEntity : entities )
		{
			localGroups.add( localGroupEntity.toDataObject() );
		}
		return localGroups;
	}

	public List<LocalGroup> getAllByIds( List<Long> ids )
	{
		List<LocalGroupEntity> groups = localGroupDAO.findByIds( ids );
		List<LocalGroup> result = new ArrayList( groups.size() );
		for ( LocalGroupEntity group : groups )
		{
			result.add( group.toDataObject() );
		}
		return result;
	}

	public List<LocalGroup> getAllByUser( String username )
	{
		List<LocalGroupEntity> entities = localGroupDAO.findByUser( username );
		List<LocalGroup> localGroups = new ArrayList( entities.size() );
		for ( LocalGroupEntity localGroupEntity : entities )
		{
			localGroups.add( localGroupEntity.toDataObject() );
		}
		return localGroups;
	}

	public List<Long> getAllIdsByUser( String username )
	{
		return localGroupDAO.findIdsByUser( username );
	}

	public LocalGroup update( LocalGroup updatedGroup ) throws LocalGroupException
	{
		Long id = updatedGroup.getId();
		if ( id == null )
		{
			throw new LocalGroupException( "Local Group ID cannot be null", LocalGroupException.LocalGroupExceptionType.LOCAL_GROUP_NOT_FOUND );
		}

		LocalGroupEntity entity = ( LocalGroupEntity ) localGroupDAO.findById( id );
		if ( entity == null )
		{
			throw new LocalGroupException( "Local Group with id  not found in command server.", LocalGroupException.LocalGroupExceptionType.LOCAL_GROUP_NOT_FOUND );
		}
		if ( CommonAppUtils.isNullOrEmptyString( entity.getName() ) )
		{
			throw new LocalGroupException( "Local Group with id " + id + " must have a name.", LocalGroupException.LocalGroupExceptionType.LOCAL_GROUP_NAME_NOT_SET );
		}

		Set<String> affectedUsers = CollectionUtils.exclusiveOR( entity.getMembers(), updatedGroup.getUsersnames() );

		entity.readFromDataObject( updatedGroup );

		logOffUsers( affectedUsers );

		auditLocalGroupOperation( AuditEventNameEnum.LOCAL_GROUP_UPDATE, entity.getName() );
		return entity.toDataObject();
	}

	public boolean isUserMemberOf( String username, List<Long> groupIds )
	{
		if ( ( groupIds == null ) || ( groupIds.isEmpty() ) )
		{
			return false;
		}

		List<Long> userGroups = localGroupDAO.findIdsByUser( username );
		for ( Long id : groupIds )
		{
			if ( userGroups.contains( id ) )
			{
				return true;
			}
		}
		return false;
	}

	private void auditLocalGroupOperation( AuditEventNameEnum auditEvent, String localGroupName )
	{
		if ( CommonAppUtils.getUsernameFromSecurityContext() != null )
		{
			AuditView.Builder auditBuilder = new AuditView.Builder( auditEvent.getName() );
			auditBuilder.addDetailsPair( "name", localGroupName );
			eventRegistry.sendEventAfterTransactionCommits( new AuditEvent( auditBuilder.build() ) );
		}
	}

	private void logOffUsers( Collection<String> usersToLogoff )
	{
		if ( usersToLogoff == null )
		{
			return;
		}
		for ( String username : usersToLogoff )
		{
			UserLogoffEvent userLogoff = new UserLogoffEvent( username );
			eventRegistry.sendEventAfterTransactionCommits( userLogoff );
		}
	}

	public void setCaseService( CaseManagementService caseService )
	{
		this.caseService = caseService;
	}

	public void setLocalGroupDAO( LocalGroupDAO localGroupDAO )
	{
		this.localGroupDAO = localGroupDAO;
	}

	public void setEventRegistry( EventRegistry eventRegistry )
	{
		this.eventRegistry = eventRegistry;
	}
}
