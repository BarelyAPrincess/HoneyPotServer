package com.marchnetworks.audit.service;

import com.marchnetworks.audit.common.AuditLogException;
import com.marchnetworks.audit.common.AuditLogExceptionTypeEnum;
import com.marchnetworks.audit.dao.AuditDAO;
import com.marchnetworks.audit.dao.AuditDictionaryDAO;
import com.marchnetworks.audit.data.AuditEventNameEnum;
import com.marchnetworks.audit.data.AuditSearchQuery;
import com.marchnetworks.audit.data.AuditView;
import com.marchnetworks.audit.data.AuditView.Builder;
import com.marchnetworks.audit.data.DeviceAuditView;
import com.marchnetworks.audit.events.AuditEvent;
import com.marchnetworks.audit.model.AuditDictionaryEntity;
import com.marchnetworks.audit.model.AuditEntity;
import com.marchnetworks.audit.model.DeviceAuditEntity;
import com.marchnetworks.audit.model.ServerAuditEntity;
import com.marchnetworks.command.api.audit.AppAuditData;
import com.marchnetworks.command.api.audit.AuditCoreService;
import com.marchnetworks.command.api.audit.UserContext;
import com.marchnetworks.command.api.query.Criteria;
import com.marchnetworks.command.api.query.Restrictions;
import com.marchnetworks.command.common.CollectionUtils;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.device.DeviceEventsEnum;
import com.marchnetworks.command.common.topology.ResourceRootType;
import com.marchnetworks.command.common.topology.TopologyConstants;
import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.topology.data.AlarmSourceResource;
import com.marchnetworks.command.common.topology.data.AudioOutputResource;
import com.marchnetworks.command.common.topology.data.ChannelResource;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.command.common.topology.data.SwitchResource;
import com.marchnetworks.command.common.user.UserException;
import com.marchnetworks.command.common.user.data.MemberView;
import com.marchnetworks.command.common.user.data.ProfileView;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.common.serialization.CoreJsonSerializer;
import com.marchnetworks.common.utils.DateUtils;
import com.marchnetworks.management.instrumentation.events.DeviceRegistrationEvent;
import com.marchnetworks.management.instrumentation.events.GenericDeviceAuditEvent;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;
import com.marchnetworks.management.user.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class AuditLogServiceImpl implements AuditLogService, AuditCoreService
{
	private static final Logger LOG = LoggerFactory.getLogger( AuditLogServiceImpl.class );

	private AuditDAO<ServerAuditEntity> serverAuditDAO;

	private AuditDAO<DeviceAuditEntity> deviceAuditDAO;

	private AuditDictionaryDAO auditDictionaryDAO;

	private UserService userService;

	private ResourceTopologyServiceIF topologyService;

	public List<AuditView> getAuditLogs( AuditSearchQuery auditViewCrit ) throws AuditLogException
	{
		List<AuditView> result = new ArrayList();
		long startTime = System.currentTimeMillis();
		String userName = CommonAppUtils.getUsernameFromSecurityContext();

		String[] allowedUsers = null;

		Builder auditBuilder = new Builder( AuditEventNameEnum.AUDIT_LOGS_SEARCH.getName() );
		AuditView searchAudit = auditBuilder.build();
		logAudit( searchAudit );

		ProfileView profile = null;
		try
		{
			profile = userService.getProfileForUser( userName );
		}
		catch ( UserException ue )
		{
			LOG.info( "Profile not found for user: " + userName );
		}
		try
		{
			if ( ( profile != null ) && ( profile.isSuperAdmin() ) && ( auditViewCrit.getUsernames() == null ) )
			{
				allowedUsers = new String[0];
			}
			else
			{
				allowedUsers = userService.listUserAccess();
			}
		}
		catch ( UserException e )
		{
			throw new AuditLogException( AuditLogExceptionTypeEnum.AUDIT_QUERY_ERROR, "Error when obtaining user information for user. Details: " + e.getMessage() );
		}

		if ( ( auditViewCrit.getUsernames() == null ) || ( auditViewCrit.getUsernames().length == 0 ) )
		{
			auditViewCrit.setUsernames( allowedUsers );
		}
		else
		{
			List<String> usernamesParam = userService.findMembersNames( auditViewCrit.getUsernames() );
			String[] filteredUsers = ( String[] ) CollectionUtils.getIntersectionOfArrays( usernamesParam.toArray( new String[usernamesParam.size()] ), allowedUsers );
			auditViewCrit.setUsernames( filteredUsers );
		}

		List<ServerAuditEntity> auditEntityList = serverAuditDAO.getAudits( auditViewCrit );

		if ( auditEntityList.isEmpty() )
		{
			return result;
		}

		Set<Integer> auditPairsKeys = getKeysFromAudits( auditEntityList );

		Map<Integer, AuditDictionaryEntity> auditPairs = auditDictionaryDAO.findValues( auditPairsKeys );
		for ( ServerAuditEntity auditEntity : auditEntityList )
		{
			AuditView av = auditEntity.toDataObject( auditPairs );
			result.add( av );
		}
		if ( LOG.isDebugEnabled() )
		{
			LOG.info( "{} ms to process query results.", Long.valueOf( System.currentTimeMillis() - startTime ) );
		}
		return result;
	}

	public List<DeviceAuditView> getDeviceAuditLogs( long startTime, long endTime ) throws AuditLogException
	{
		List<DeviceAuditView> result = new ArrayList();
		List<Resource> deviceResources = null;
		String username = CommonAppUtils.getUsernameFromSecurityContext();
		try
		{
			Criteria criteria = new Criteria( DeviceResource.class );
			criteria.add( Restrictions.eq( "deviceView.parentDeviceId", null ) );

			MemberView user = userService.getUser( username );

			if ( user.hasResource( TopologyConstants.SYSTEM_ROOT_ID ) )
			{
				deviceResources = new ArrayList( 0 );
			}
			else
			{
				deviceResources = topologyService.getResourcesForUser( username, ResourceRootType.SYSTEM, criteria, false );

				if ( deviceResources.isEmpty() )
					return result;
			}
		}
		catch ( TopologyException e )
		{
			throw new AuditLogException( AuditLogExceptionTypeEnum.AUDIT_QUERY_ERROR, "Failed to lookup resources in topology for user " + username );
		}
		catch ( UserException e )
		{
			throw new AuditLogException( AuditLogExceptionTypeEnum.AUDIT_QUERY_ERROR, "Could not retrieve resources for user:" + username );
		}
		List<Long> deviceResourceIds = new ArrayList( deviceResources.size() );
		for ( Resource deviceResource : deviceResources )
		{
			deviceResourceIds.add( deviceResource.getId() );
		}
		AuditSearchQuery auditSearchQuery = new AuditSearchQuery();
		auditSearchQuery.setStartTime( startTime );
		auditSearchQuery.setEndTime( endTime );
		auditSearchQuery.setResourceIds( ( Long[] ) deviceResourceIds.toArray( new Long[deviceResourceIds.size()] ) );
		List<DeviceAuditEntity> deviceAudits = deviceAuditDAO.getAudits( auditSearchQuery );

		Set<Integer> auditPairsKeys = getKeysFromAudits( deviceAudits );

		Map<Integer, AuditDictionaryEntity> auditPairs = auditDictionaryDAO.findValues( auditPairsKeys );
		for ( DeviceAuditEntity auditEntity : deviceAudits )
		{
			DeviceAuditView av = auditEntity.toDataObject( auditPairs );
			result.add( av );
		}
		return result;
	}

	public void purgeOldAuditLogs( long days )
	{
		Calendar currentDate = DateUtils.getCurrentUTCTime();
		currentDate.add( 6, ( int ) days * -1 );
		int oldAudits = serverAuditDAO.deleteOldAudits( currentDate.getTimeInMillis() );
		LOG.info( "Purging {} audit entries older than {} days", Integer.valueOf( oldAudits ), Long.valueOf( days ) );

		oldAudits = deviceAuditDAO.deleteOldAudits( currentDate.getTimeInMillis() );
		LOG.info( "Purging {} device audit entries older than {} days", Integer.valueOf( oldAudits ), Long.valueOf( days ) );

		auditDictionaryDAO.deleteUnreferencedKeys();
	}

	public void logAudit( AuditView auditView )
	{
		if ( !validateInput( auditView.getEventName(), auditView.getUsername(), auditView.getStartTime().longValue() ) )
		{
			if ( LOG.isDebugEnabled() )
			{
				LOG.debug( "Audit input doesn't contain the minimum required information (eventName, username or startTime not set)." );
				String serializedInput = CoreJsonSerializer.toJson( auditView );
				LOG.debug( "Input data:{}", serializedInput );
			}
			return;
		}

		if ( CommonAppUtils.isNullOrEmptyString( auditView.getUserRemoteAddress() ) )
		{
			String ipAddress = CommonAppUtils.getRemoteIpAddressFromSecurityContext();
			auditView.setUserRemoteAddress( ipAddress );
		}

		Map<Integer, String> auditViewWords = buildAuditVewWords( auditView.getEventName(), auditView.getUsername(), auditView.getUserRemoteAddress(), auditView.getAppId(), auditView.getEventDetails() );

		updateDictionary( auditViewWords );

		ServerAuditEntity auditEntity = null;
		if ( !CommonAppUtils.isNullOrEmptyString( auditView.getEventTag() ) )
		{
			auditEntity = ( ServerAuditEntity ) serverAuditDAO.findAuditByTag( auditView.getEventTag() );
		}

		if ( auditEntity != null )
		{

			auditEntity.setEndTime( auditView.getEndTime() );
			if ( ( auditView.getEventDetails() != null ) && ( auditView.getEventDetails().size() > 0 ) )
			{
				auditEntity.setDetailsId( Integer.valueOf( auditView.getEventDetailsAsString().hashCode() ) );
			}
		}
		else
		{
			auditEntity = new ServerAuditEntity();
			auditEntity.setEventNameId( Integer.valueOf( auditView.getEventName().hashCode() ) );

			auditEntity.setRemoteAddressId( Integer.valueOf( auditView.getUserRemoteAddress().hashCode() ) );
			auditEntity.setUsernameId( Integer.valueOf( auditView.getUsername().hashCode() ) );
			auditEntity.setStartTime( auditView.getStartTime() );
			auditEntity.setEventTag( auditView.getEventTag() );
			if ( auditView.getAppId() != null )
			{
				auditEntity.setAppId( Integer.valueOf( auditView.getAppId().hashCode() ) );
			}

			if ( ( auditView.getEventDetails() != null ) && ( auditView.getEventDetails().size() > 0 ) )
			{
				auditEntity.setDetailsId( Integer.valueOf( auditView.getEventDetailsAsString().hashCode() ) );
			}
			if ( ( auditView.getResourceIds() != null ) && ( auditView.getResourceIds().size() > 0 ) )
			{
				List<String> idsAsStringList = new ArrayList();
				for ( Long resourceId : auditView.getResourceIds() )
				{
					idsAsStringList.add( resourceId.toString() );
				}
				auditEntity.setResourceIds( CoreJsonSerializer.toJson( idsAsStringList ) );
			}

			serverAuditDAO.create( auditEntity );
		}
	}

	public void logAuditEvent( Event auditEvent )
	{
		AuditView av = null;

		if ( ( auditEvent instanceof GenericDeviceAuditEvent ) )
		{
			GenericDeviceAuditEvent deviceAuditEvent = ( GenericDeviceAuditEvent ) auditEvent;

			if ( !validateInput( deviceAuditEvent.getPathName(), deviceAuditEvent.getUsername(), deviceAuditEvent.getTimestamp() ) )
			{
				if ( LOG.isDebugEnabled() )
				{
					LOG.debug( "Device Audit event doesn't contain the minimum required information (eventName, username or startTime not set)." );
					String serializedInput = CoreJsonSerializer.toJson( deviceAuditEvent );
					LOG.debug( "Input data:{}", serializedInput );
				}
				return;
			}

			if ( deviceAuditEvent.getPathName().equalsIgnoreCase( DeviceEventsEnum.SYSTEM_AUDIT_ENTRY.getPath() ) )
			{
				String auditEventName = deviceAuditEvent.getPairValue( "audit_name" );

				String sourceId = deviceAuditEvent.getSource();

				DeviceAuditView.Builder builder = new DeviceAuditView.Builder( auditEventName, deviceAuditEvent.getUsername(), deviceAuditEvent.getRemoteIpAddress(), Long.valueOf( deviceAuditEvent.getTimestamp() ) );

				builder.addRootDeviceToAudit( deviceAuditEvent.getDeviceId(), true );

				if ( !CommonAppUtils.isNullOrEmptyString( sourceId ) )
				{

					DeviceResource device = topologyService.getDeviceResourceByDeviceId( deviceAuditEvent.getDeviceId() );
					boolean sourceIdAssigned = false;

					List<Resource> channelResources = device.createFilteredResourceList( new Class[] {ChannelResource.class} );

					for ( Resource resource : channelResources )
					{
						ChannelResource channel = ( ChannelResource ) resource;
						if ( channel.getChannelView().getChannelId().equals( sourceId ) )
						{
							sourceId = channel.getChannelView().getChannelName();
							sourceIdAssigned = true;
							break;
						}
					}
					if ( !sourceIdAssigned )
					{
						List<Resource> alarmResources = device.createFilteredResourceList( new Class[] {AlarmSourceResource.class} );
						for ( Resource resource : alarmResources )
						{
							AlarmSourceResource alarm = ( AlarmSourceResource ) resource;
							if ( alarm.getAlarmSource().getDeviceAlarmSourceId().equals( sourceId ) )
							{
								sourceId = alarm.getAlarmSource().getName();
								sourceIdAssigned = true;
								break;
							}
						}
					}
					if ( !sourceIdAssigned )
					{
						List<Resource> switchResources = device.createFilteredResourceList( new Class[] {SwitchResource.class} );
						for ( Resource resource : switchResources )
						{
							SwitchResource switchResource = ( SwitchResource ) resource;
							if ( switchResource.getSwitchView().getSwitchId().equals( sourceId ) )
							{
								sourceId = switchResource.getSwitchView().getName();
								sourceIdAssigned = true;
								break;
							}
						}
					}
					if ( !sourceIdAssigned )
					{
						List<Resource> audioResources = device.createFilteredResourceList( new Class[] {AudioOutputResource.class} );
						for ( Resource resource : audioResources )
						{
							AudioOutputResource audioResource = ( AudioOutputResource ) resource;
							if ( audioResource.getAudioOutputView().getAudioOutputId().equals( sourceId ) )
							{
								sourceId = audioResource.getAudioOutputView().getName();
								sourceIdAssigned = true;
								break;
							}
						}
					}
					builder.addSourceId( sourceId );
				}

				String string = deviceAuditEvent.getPairValue( "resource_names" );
				if ( !CommonAppUtils.isNullOrEmptyString( string ) )
				{
					builder.addDetailsPair( "resource_names", string );
				}
				string = deviceAuditEvent.getPairValue( "details" );
				if ( !CommonAppUtils.isNullOrEmptyString( string ) )
				{
					builder.addDetailsPair( "details", deviceAuditEvent.getPairValue( "details" ) );
				}
				logDeviceAudit( builder.build() );
				return;
			}

			Builder builder = new Builder( deviceAuditEvent.getPathName(), deviceAuditEvent.getUsername(), deviceAuditEvent.getRemoteIpAddress(), Long.valueOf( deviceAuditEvent.getTimestamp() ) );
			builder.addEventTag( deviceAuditEvent.getAuditEntryId() );
			builder.setEndTime( Long.valueOf( deviceAuditEvent.getTimestamp() ) );

			if ( deviceAuditEvent.getPathName().equals( AuditEventNameEnum.PTZ_CONTROL.getName() ) )
			{
				if ( !"inuse".equalsIgnoreCase( deviceAuditEvent.getValue() ) )
				{
					LOG.debug( "PTZ Control with value {} is not audited by CES.", deviceAuditEvent.getValue() );
					return;
				}
				builder.addChannelToAudit( deviceAuditEvent.getDeviceId(), findChannelIdFromPTZId( deviceAuditEvent.getSource() ) );
			}
			else if ( ( deviceAuditEvent.getPathName().equals( AuditEventNameEnum.PTZ_PRESET.getName() ) ) || ( deviceAuditEvent.getPathName().equals( AuditEventNameEnum.PTZ_TOUR.getName() ) ) )
			{
				String presetPairValue = deviceAuditEvent.getValue();
				if ( deviceAuditEvent.hasPair( "name" ) )
				{
					presetPairValue = deviceAuditEvent.getPairValue( "name" );
				}
				builder.addDetailsPair( "preset", presetPairValue );
				builder.addChannelToAudit( deviceAuditEvent.getDeviceId(), findChannelIdFromPTZId( deviceAuditEvent.getSource() ) );
			}
			else if ( ( deviceAuditEvent.getPathName().equals( AuditEventNameEnum.ARCHIVE_VIDEO_REQUEST.getName() ) ) || ( deviceAuditEvent.getPathName().equals( AuditEventNameEnum.ARCHIVE_VIDEO_EXPORT_REQUEST.getName() ) ) || ( deviceAuditEvent.getPathName().equals( AuditEventNameEnum.LIVE_VIDEO_REQUEST.getName() ) ) )
			{

				if ( ( !deviceAuditEvent.getValue().equalsIgnoreCase( "ok" ) ) && ( !deviceAuditEvent.getValue().equalsIgnoreCase( "inprogress" ) ) )
				{
					LOG.debug( "{} event with value {} is not audited by CES.", deviceAuditEvent.getPathName(), deviceAuditEvent.getValue() );
					return;
				}
				builder.addChannelToAudit( deviceAuditEvent.getDeviceId(), deviceAuditEvent.getSource() );
				if ( deviceAuditEvent.hasPair( "earliest_time_accessed" ) )
				{
					builder.addDetailsPair( "earliest_time_accessed", deviceAuditEvent.getPairValue( "earliest_time_accessed" ) );
				}
				if ( deviceAuditEvent.hasPair( "latest_time_accessed" ) )
				{
					builder.addDetailsPair( "latest_time_accessed", deviceAuditEvent.getPairValue( "latest_time_accessed" ) );
				}
				if ( deviceAuditEvent.hasPair( "sector" ) )
				{
					builder.addDetailsPair( "sector", deviceAuditEvent.getPairValue( "sector" ) );
				}
				if ( deviceAuditEvent.hasPair( "duration_sec" ) )
				{
					builder.addDetailsPair( "duration_sec", deviceAuditEvent.getPairValue( "duration_sec" ) );
				}
			}
			else if ( deviceAuditEvent.getPathName().equals( AuditEventNameEnum.TALK_CHANNEL_CONTROL.getName() ) )
			{
				if ( !"inuse".equalsIgnoreCase( deviceAuditEvent.getValue() ) )
				{
					LOG.debug( "Talk Channel Control with value {} is not audited by CES.", deviceAuditEvent.getValue() );
					return;
				}

				builder.addAudioOutputToAudit( deviceAuditEvent.getDeviceId(), deviceAuditEvent.getSource() );
			}
			else if ( deviceAuditEvent.getPathName().equals( AuditEventNameEnum.SWITCH_CONTROL.getName() ) )
			{
				if ( !"manual".equalsIgnoreCase( deviceAuditEvent.getPairValue( "mode" ) ) )
				{
					LOG.debug( "Switch Control with value {} is not audited by CES.", deviceAuditEvent.getValue() );
					return;
				}
				if ( !deviceAuditEvent.getUsername().isEmpty() )
				{
					String[] userValues = deviceAuditEvent.getUsername().split( "@" );
					if ( ( userValues != null ) && ( userValues.length == 2 ) )
					{
						String username = userValues[0];
						String ipAddress = userValues[1];
						builder.setRemoteIpAddress( ipAddress );
						builder.setUsername( username );
					}
				}
				builder.addSwitchToAudit( deviceAuditEvent.getDeviceId(), deviceAuditEvent.getSource() );
				builder.addDetailsPair( "state", deviceAuditEvent.getValue() );
			}
			else if ( deviceAuditEvent.getPathName().equals( AuditEventNameEnum.ALARM_SOURCE_CONTROL.getName() ) )
			{
				if ( CommonAppUtils.isNullOrEmptyString( deviceAuditEvent.getUsername() ) )
				{
					LOG.debug( "Alarm Source Control with no user set is not audited by CES." );
					return;
				}
				builder.addAlarmSourceToAudit( deviceAuditEvent.getDeviceId(), deviceAuditEvent.getSource() );
				builder.addDetailsPair( "state", deviceAuditEvent.getValue() );
			}
			av = builder.build();
		}
		else if ( ( auditEvent instanceof AuditEvent ) )
		{
			av = ( ( AuditEvent ) auditEvent ).getAuditView();
		}

		long start = System.currentTimeMillis();
		logAudit( av );
		long end = System.currentTimeMillis();

		if ( av.getEventName().equals( AuditEventNameEnum.DEVICE_REGISTRATION.getName() ) )
		{
			LOG.debug( "Post-Registration execution time for AuditService: " + ( end - start ) + " ms." );
		}
	}

	public void logAppAudit( AppAuditData data, UserContext context )
	{
		Map<Integer, String> auditViewWords = buildAuditVewWords( data.getEventName(), context.getUserName(), context.getUserRemoteAddress(), data.getAppId(), data.getEventDetails() );
		updateDictionary( auditViewWords );

		ServerAuditEntity auditEntity = new ServerAuditEntity();
		auditEntity.setEventNameId( Integer.valueOf( data.getEventName().hashCode() ) );
		auditEntity.setUsernameId( Integer.valueOf( context.getUserName().hashCode() ) );
		auditEntity.setAppId( Integer.valueOf( data.getAppId().hashCode() ) );
		auditEntity.setRemoteAddressId( Integer.valueOf( context.getUserRemoteAddress().hashCode() ) );
		auditEntity.setStartTime( data.getStartTime() );
		auditEntity.setEndTime( data.getEndTime() );
		if ( ( data.getEventDetails() != null ) && ( !data.getEventDetails().isEmpty() ) )
		{
			auditEntity.setDetailsId( Integer.valueOf( data.getEventDetailsAsString().hashCode() ) );
		}
		serverAuditDAO.create( auditEntity );
	}

	public void processDeviceUnregistration( DeviceRegistrationEvent unregistrationEvent )
	{
		Long resourceId = unregistrationEvent.getResourceId();
		List<DeviceAuditEntity> matchingAudits = deviceAuditDAO.findByResourceId( resourceId );
		for ( DeviceAuditEntity entity : matchingAudits )
		{
			entity.setDeleted( true );
		}
	}

	public void deleteAuditLogsByAppid( String appId )
	{
		serverAuditDAO.deleteByAppId( appId );
	}

	private void logDeviceAudit( DeviceAuditView deviceAuditView )
	{
		DeviceAuditEntity deviceAuditEntity = new DeviceAuditEntity();

		Map<Integer, String> deviceAuditWords = new HashMap();

		int hashCode = 0;
		if ( deviceAuditView.getEventName() != null )
		{
			hashCode = deviceAuditView.getEventName().hashCode();
			deviceAuditWords.put( Integer.valueOf( hashCode ), deviceAuditView.getEventName() );
			deviceAuditEntity.setEventNameId( Integer.valueOf( hashCode ) );
		}
		if ( deviceAuditView.getUsername() != null )
		{
			hashCode = deviceAuditView.getUsername().hashCode();
			deviceAuditWords.put( Integer.valueOf( hashCode ), deviceAuditView.getUsername() );
			deviceAuditEntity.setUsernameId( Integer.valueOf( hashCode ) );
		}
		if ( !CommonAppUtils.isNullOrEmptyString( deviceAuditView.getUserRemoteAddress() ) )
		{
			hashCode = deviceAuditView.getUserRemoteAddress().hashCode();
			deviceAuditWords.put( Integer.valueOf( hashCode ), deviceAuditView.getUserRemoteAddress() );
			deviceAuditEntity.setRemoteAddressId( Integer.valueOf( hashCode ) );
		}
		if ( deviceAuditView.getSourceId() != null )
		{
			hashCode = deviceAuditView.getSourceId().hashCode();
			deviceAuditWords.put( Integer.valueOf( hashCode ), deviceAuditView.getSourceId() );
			deviceAuditEntity.setSourceId( Integer.valueOf( hashCode ) );
		}
		if ( ( deviceAuditView.getEventDetails() != null ) && ( !deviceAuditView.getEventDetails().isEmpty() ) )
		{
			String detailsAsJson = CoreJsonSerializer.toJson( deviceAuditView.getEventDetails() );
			if ( detailsAsJson != null )
			{
				hashCode = detailsAsJson.hashCode();
				deviceAuditWords.put( Integer.valueOf( hashCode ), detailsAsJson );
				deviceAuditEntity.setDetailsId( Integer.valueOf( hashCode ) );
			}
		}
		updateDictionary( deviceAuditWords );

		deviceAuditEntity.setStartTime( deviceAuditView.getTime() );

		if ( ( deviceAuditView.getResourceIds() != null ) && ( deviceAuditView.getResourceIds().size() > 0 ) )
		{
			List<String> idsAsStringList = new ArrayList();
			for ( Long resourceId : deviceAuditView.getResourceIds() )
			{
				idsAsStringList.add( resourceId.toString() );
			}
			deviceAuditEntity.setResourceIds( CoreJsonSerializer.toJson( idsAsStringList ) );
		}
		deviceAuditDAO.create( deviceAuditEntity );
	}

	private void updateDictionary( Map<Integer, String> newEntities )
	{
		Map<Integer, AuditDictionaryEntity> existingEntries = auditDictionaryDAO.findValues( newEntities.keySet() );
		Map<Integer, String> newEntitiesCopy = new HashMap( newEntities );
		Set<Integer> rehashedKeys = new HashSet( 2 );
		Set<Integer> missingKeys = new HashSet( 4 );

		for ( Entry<Integer, String> mapEntry : newEntities.entrySet() )
		{
			Integer key = ( Integer ) mapEntry.getKey();
			String existingValue = existingEntries.containsKey( key ) ? ( ( AuditDictionaryEntity ) existingEntries.get( key ) ).readValue() : null;
			if ( ( existingValue != null ) && ( !( ( String ) mapEntry.getValue() ).equals( existingValue ) ) )
			{
				LOG.warn( "There's an existing entry for the key-value {}:{} existing value {}, will re-hash.", new Object[] {mapEntry.getKey(), mapEntry.getValue(), existingValue} );
				newEntitiesCopy.remove( mapEntry.getKey() );
				String modifiedValue = ( ( String ) mapEntry.getValue() ).concat( "​" );
				newEntitiesCopy.put( Integer.valueOf( modifiedValue.hashCode() ), modifiedValue );
				rehashedKeys.add( Integer.valueOf( modifiedValue.hashCode() ) );
			}
			else if ( existingValue == null )
			{
				missingKeys.add( key );
			}
		}

		if ( !rehashedKeys.isEmpty() )
		{
			missingKeys.addAll( auditDictionaryDAO.findMissingKeys( rehashedKeys ) );
		}

		for ( Integer key : missingKeys )
		{
			AuditDictionaryEntity dictionaryEntry = new AuditDictionaryEntity();
			dictionaryEntry.setKey( key );
			dictionaryEntry.setValue( ( String ) newEntitiesCopy.get( key ) );
			auditDictionaryDAO.create( dictionaryEntry );
		}
	}

	private <T extends AuditEntity> Set<Integer> getKeysFromAudits( List<T> auditEntities )
	{
		Set<Integer> keysFromEntity = new HashSet( auditEntities.size() );
		for ( AuditEntity auditEntity : auditEntities )
		{
			keysFromEntity.addAll( auditEntity.getKeys() );
		}
		return keysFromEntity;
	}

	private String findChannelIdFromPTZId( String ptzId )
	{
		String channelId = null;

		Criteria criteria = new Criteria( ChannelResource.class );
		criteria.add( Restrictions.eq( "channelView.ptzDomeIdentifier", ptzId ) );
		ChannelResource channel = ( ChannelResource ) topologyService.getFirstResource( criteria );
		if ( channel != null )
		{
			channelId = channel.getChannelId();
		}
		return channelId;
	}

	private Map<Integer, String> buildAuditVewWords( String eventName, String userName, String userRemoteAddress, String appId, Map<String, String> eventDetails )
	{
		Map<Integer, String> auditViewWords = new HashMap();
		if ( eventName != null )
		{
			auditViewWords.put( Integer.valueOf( eventName.hashCode() ), eventName );
		}
		if ( userName != null )
		{
			auditViewWords.put( Integer.valueOf( userName.hashCode() ), userName );
		}
		if ( userRemoteAddress != null )
		{
			auditViewWords.put( Integer.valueOf( userRemoteAddress.hashCode() ), userRemoteAddress );
		}

		if ( appId != null )
		{
			auditViewWords.put( Integer.valueOf( appId.hashCode() ), appId );
		}

		if ( ( eventDetails != null ) && ( !eventDetails.isEmpty() ) )
		{
			String detailsAsJson = CoreJsonSerializer.toJson( eventDetails );
			if ( detailsAsJson != null )
			{
				auditViewWords.put( Integer.valueOf( detailsAsJson.hashCode() ), detailsAsJson );
			}
		}
		return auditViewWords;
	}

	private boolean validateInput( String eventName, String username, long timestamp )
	{
		if ( ( CommonAppUtils.isNullOrEmptyString( eventName ) ) || ( CommonAppUtils.isNullOrEmptyString( username ) ) || ( timestamp <= 0L ) )
		{
			return false;
		}

		if ( ( username.equalsIgnoreCase( "_server_" ) ) || ( username.equalsIgnoreCase( "_cloud_" ) ) )
		{
			return false;
		}

		return true;
	}

	public void setAuditDictionaryDAO( AuditDictionaryDAO auditDictionaryDAO )
	{
		this.auditDictionaryDAO = auditDictionaryDAO;
	}

	public void setUserService( UserService userService )
	{
		this.userService = userService;
	}

	public void setServerAuditDAO( AuditDAO<ServerAuditEntity> serverAuditDAO )
	{
		this.serverAuditDAO = serverAuditDAO;
	}

	public void setDeviceAuditDAO( AuditDAO<DeviceAuditEntity> deviceAuditDAO )
	{
		this.deviceAuditDAO = deviceAuditDAO;
	}

	public void setTopologyService( ResourceTopologyServiceIF topologyService )
	{
		this.topologyService = topologyService;
	}
}
