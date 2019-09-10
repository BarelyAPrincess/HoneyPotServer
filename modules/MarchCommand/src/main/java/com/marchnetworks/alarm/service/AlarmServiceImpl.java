package com.marchnetworks.alarm.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.marchnetworks.alarm.alarmdetails.AlarmDetailEnum;
import com.marchnetworks.alarm.dao.AlarmEntryDAO;
import com.marchnetworks.alarm.dao.AlarmSourceDAO;
import com.marchnetworks.alarm.data.AlarmEntryView;
import com.marchnetworks.alarm.events.AlarmDeletedEvent;
import com.marchnetworks.alarm.events.AlarmEntryClosedEvent;
import com.marchnetworks.alarm.events.AlarmEntryEvent;
import com.marchnetworks.alarm.events.AlarmStateEvent;
import com.marchnetworks.alarm.model.AlarmEntryEntity;
import com.marchnetworks.alarm.model.AlarmSourceEntity;
import com.marchnetworks.alarm.model.AlarmSourceMBean;
import com.marchnetworks.alarm.task.AlarmClosureRecordsTask;
import com.marchnetworks.audit.data.AuditEventNameEnum;
import com.marchnetworks.audit.data.AuditView;
import com.marchnetworks.audit.events.AuditEvent;
import com.marchnetworks.command.api.alarm.AlarmCoreService;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.alarm.data.AlarmExtendedState;
import com.marchnetworks.command.common.alarm.data.AlarmSourceView;
import com.marchnetworks.command.common.alarm.data.AlarmState;
import com.marchnetworks.command.common.scheduling.TaskScheduler;
import com.marchnetworks.command.common.topology.ResourceAssociationType;
import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.topology.data.AlarmSourceLinkResource;
import com.marchnetworks.command.common.topology.data.AlarmSourceResource;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.command.common.topology.data.LinkResource;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.command.common.user.data.MemberView;
import com.marchnetworks.common.config.AppConfig;
import com.marchnetworks.common.config.AppConfigImpl;
import com.marchnetworks.common.config.ConfigProperty;
import com.marchnetworks.common.device.DeletedDevice;
import com.marchnetworks.common.serialization.CoreJsonSerializer;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.transport.datamodel.DeviceException;
import com.marchnetworks.common.utils.DateUtils;
import com.marchnetworks.common.utils.ServerUtils;
import com.marchnetworks.management.instrumentation.DeviceService;
import com.marchnetworks.management.instrumentation.events.DeviceAlarmEvent;
import com.marchnetworks.management.instrumentation.events.DeviceAlarmEventType;
import com.marchnetworks.management.instrumentation.pooling.DeferredEvent;
import com.marchnetworks.management.instrumentation.pooling.DeferredEventPool;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;
import com.marchnetworks.management.user.UserService;
import com.marchnetworks.server.communications.transport.datamodel.AlarmSource;
import com.marchnetworks.server.event.EventRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AlarmServiceImpl implements AlarmService, AlarmCoreService
{
	public static final int MAX_QUERY_PARAMETERS = 1000;
	private static final Logger LOG = LoggerFactory.getLogger( AlarmServiceImpl.class );

	private static boolean alarmsEnabled = true;
	private AlarmEntryDAO alarmEntryDAO;
	private AlarmSourceDAO alarmSourceDAO;
	private ResourceTopologyServiceIF topologyService;
	private DeviceService deviceService;
	private EventRegistry eventRegistry;
	private DeferredEventPool deferredEventPool;
	private TaskScheduler taskScheduler;
	private UserService userService;

	static
	{
		AppConfig appConfig = AppConfigImpl.getInstance();
		if ( appConfig.getProperty( ConfigProperty.ALARMS_FEATURE ) != null )
		{
			String configValue = appConfig.getProperty( ConfigProperty.ALARMS_FEATURE );
			if ( "false".equalsIgnoreCase( configValue ) )
			{
				alarmsEnabled = false;
			}
		}
	}

	public AlarmEntryView[] queryAlarmEntries( String userName, String[] alarmSourceIDs, boolean includeOpenEntries, boolean includeClosedEntries, long startTime, long endTime, int maxEntries )
	{
		if ( ( maxEntries > 1000 ) || ( maxEntries <= 0 ) )
		{
			maxEntries = 1000;
		}
		int alarmSourceIdslimit = maxEntries;

		boolean isAlarmHistorySearch = ( !includeOpenEntries ) && ( includeClosedEntries ) && ( ( alarmSourceIDs == null ) || ( alarmSourceIDs.length == 0 ) );

		Set<Long> availableAlarmSources = getAlarmSourcesUnderUserTerritory( userName );

		List<Long> alarmSourcesToSearch;

		if ( ( alarmSourceIDs != null ) && ( alarmSourceIDs.length > 0 ) )
		{
			alarmSourcesToSearch = new ArrayList<Long>();
			for ( String id : alarmSourceIDs )
			{
				alarmSourcesToSearch.add( Long.parseLong( id ) );
			}
			alarmSourceIdslimit = 1000;
		}
		else
		{
			alarmSourcesToSearch = alarmEntryDAO.findReferencedAlarmSources( includeOpenEntries, includeClosedEntries, startTime, endTime );
		}

		alarmSourcesToSearch.retainAll( availableAlarmSources );

		if ( alarmSourcesToSearch.size() > alarmSourceIdslimit )
		{
			alarmSourcesToSearch = alarmSourcesToSearch.subList( 0, alarmSourceIdslimit );
		}

		List<AlarmEntryEntity> entries = alarmEntryDAO.findAllByQuery( alarmSourcesToSearch, includeOpenEntries, includeClosedEntries, startTime, endTime, maxEntries );

		AlarmEntryView[] result = new AlarmEntryView[entries.size()];
		for ( int i = 0; i < entries.size(); i++ )
		{
			result[i] = ( ( AlarmEntryEntity ) entries.get( i ) ).toDataObject();
		}

		if ( isAlarmHistorySearch )
		{
			auditAlarmSearchQuery( alarmSourcesToSearch, includeOpenEntries, includeClosedEntries, startTime, endTime, maxEntries );
		}

		return result;
	}

	public void closeAlarmEntries( String userName, AlarmEntryCloseRecord[] alarmClosures ) throws AlarmException
	{
		if ( ( userName == null ) || ( alarmClosures == null ) )
		{
			throw new IllegalArgumentException( "UserName or AlarmClosure array parameter is null" );
		}

		boolean closedAllEntries = true;
		Multimap<Long, AlarmEntryView> closuresToDevices = ArrayListMultimap.create();

		for ( AlarmEntryCloseRecord closeRecord : alarmClosures )
		{
			Long id = Long.valueOf( Long.parseLong( closeRecord.getAlarmEntryId() ) );
			AlarmEntryEntity alarmEntry = ( AlarmEntryEntity ) alarmEntryDAO.findById( id );

			if ( ( alarmEntry != null ) && ( alarmEntry.isOpen() ) )
			{
				AlarmSourceEntity alarmSource = alarmEntry.getAlarmSource();

				Set<String> handlingUsers = alarmEntry.getHandlingUsers();
				if ( ( handlingUsers != null ) && ( handlingUsers.contains( userName ) ) )
				{
					alarmEntry.setClosedByUser( userName );
					alarmEntry.setClosedText( closeRecord.getClosingComment() );
					alarmEntry.setClosedTime( DateUtils.getCurrentUTCTimeInMicros() );

					Set<Long> territoryInfo = getTerritoryInfoForAlarm( alarmSource );
					AlarmEntryClosedEvent event = new AlarmEntryClosedEvent( territoryInfo, alarmEntry.toDataObject(), false );
					eventRegistry.sendEventAfterTransactionCommits( event );

					auditAlarm( AuditEventNameEnum.ALARM_CLOSE, alarmSource.getDeviceIdAsString(), alarmSource.getDeviceAlarmSourceID(), closeRecord.getClosingComment() );

					closuresToDevices.put( alarmEntry.getAlarmSource().getDeviceId(), alarmEntry.toDataObject() );
				}
				else
				{
					LOG.warn( "Alarm entry to close was not handled by user " + userName + ", alrmEntry id:" + alarmEntry.getId() );
					closedAllEntries = false;
				}
			}
			else
			{
				if ( alarmEntry == null )
				{
					LOG.warn( "Alarm entry to close does not exist, alrmEntry id:" + id );
				}
				else
				{
					LOG.warn( "Alarm entry to close was already closed, alrmEntry id:" + id );
				}
				closedAllEntries = false;
			}
		}

		for ( Long deviceId : closuresToDevices.keySet() )
		{
			scheduleAlarmClosureDispatchTask( deviceId, closuresToDevices.get( deviceId ) );
		}

		if ( !closedAllEntries )
		{
			throw new AlarmException( AlarmExceptionTypeEnum.CLOSE_ENTRIES_ERROR, "Not all alarm entries could be closed (already closed or not handled by same user)" );
		}
	}

	public void setAlarmHandling( String userName, String[] alarmEntryIDs, boolean handling ) throws AlarmException
	{
		boolean handledAllEntries = true;

		for ( String alarmEntryId : alarmEntryIDs )
		{
			AlarmEntryEntity alarmEntry = ( AlarmEntryEntity ) alarmEntryDAO.findById( Long.valueOf( Long.parseLong( alarmEntryId ) ) );

			if ( ( alarmEntry != null ) && ( alarmEntry.isOpen() ) )
			{
				AlarmSourceEntity alarmSource = alarmEntry.getAlarmSource();
				boolean listChanged;
				if ( handling )
				{
					listChanged = alarmEntry.addToHandlingUsers( userName );
				}
				else
				{
					listChanged = alarmEntry.removeFromHandlingUsers( userName );
				}

				if ( listChanged )
				{
					Set<Long> territoryInfo = getTerritoryInfoForAlarm( alarmSource );
					AlarmEntryEvent event = new AlarmEntryEvent( territoryInfo, alarmEntry.toDataObject(), false, false );
					eventRegistry.sendEventAfterTransactionCommits( event );

					auditAlarm( AuditEventNameEnum.ALARM_HANDLING, alarmSource.getDeviceIdAsString(), alarmSource.getDeviceAlarmSourceID(), null );
				}
				else
				{
					LOG.warn( "Alarm entry was already (un)handled for " + userName + ", alrmEntry id:" + alarmEntry.getId() );
					handledAllEntries = false;
				}
			}
			else
			{
				if ( alarmEntry == null )
				{
					LOG.warn( "Alarm entry to handle does not exist, alrmEntry id:" + alarmEntryId );
				}
				else
				{
					LOG.warn( "Alarm entry to handle was already closed, alrmEntry id:" + alarmEntryId );
				}
				handledAllEntries = false;
			}
		}

		if ( !handledAllEntries )
		{
			throw new AlarmException( AlarmExceptionTypeEnum.HANDLE_ENTRIES_ERROR, "Not all alarm entries could be (un)handled (already closed or (un)handled)" );
		}
	}

	public boolean getAlarmsEnabled()
	{
		return alarmsEnabled;
	}

	public void processAlarmEvent( DeviceAlarmEvent alarmEvent )
	{
		DeviceAlarmEventType type = alarmEvent.getType();

		if ( type == DeviceAlarmEventType.ALARM_ENTRY )
		{
			processAlarmEntryEvent( alarmEvent );
		}
		else if ( type == DeviceAlarmEventType.ALARM_CLOSED )
		{
			processAlarmClosedEvent( alarmEvent );
		}
		else if ( type == DeviceAlarmEventType.ALARM_STATE )
		{
			processAlarmStateChangeEvent( alarmEvent );
		}
		else if ( type == DeviceAlarmEventType.ALARM_CONFIG )
		{
			processAlarmSourcesChangedEvent( alarmEvent );
		}
	}

	public AlarmSourceMBean getAlarmSource( String alarmSourceId )
	{
		return ( AlarmSourceMBean ) alarmSourceDAO.findById( Long.valueOf( Long.parseLong( alarmSourceId ) ) );
	}

	public boolean deleteAlarmSource( String alarmSourceId )
	{
		AlarmSourceEntity alarmSource = ( AlarmSourceEntity ) alarmSourceDAO.findById( Long.valueOf( Long.parseLong( alarmSourceId ) ) );
		return deleteAlarmSource( alarmSource, false );
	}

	public void processDeviceUnregistered( String deviceId )
	{
		List<AlarmSourceEntity> alarmSourcesByDevice = alarmSourceDAO.findAllWithDeletedByDeviceId( Long.valueOf( Long.parseLong( deviceId ) ) );

		if ( !alarmSourcesByDevice.isEmpty() )
		{
			for ( AlarmSourceEntity alarmSource : alarmSourcesByDevice )
			{
				alarmSource.setDeviceId( null );

				if ( deleteAlarmSource( alarmSource, true ) )
				{
					DeletedDevice deletedDevice = deviceService.createDeletedDevice( deviceId );
					if ( deletedDevice != null )
					{
						alarmSource.setDeletedDevice( deletedDevice );
					}
				}
			}
		}
	}

	public void processDeviceRegistered( String deviceId )
	{
		refreshDeviceAlarmSources( deviceId, true );
	}

	public void processAlarmReconciliationWithDevice( String deviceId )
	{
		refreshDeviceAlarmSources( deviceId, false );
		sendPendingAlarmClosuresToDevice( deviceId );
	}

	public void processAlarmClosureDispatch( String deviceId )
	{
		sendPendingAlarmClosuresToDevice( deviceId );
	}

	private void sendPendingAlarmClosuresToDevice( String deviceId )
	{
		List<AlarmEntryView> alarmEntriesToReconcile = new ArrayList();
		List<AlarmEntryEntity> closedEntriesNotReconciled = alarmEntryDAO.findClosedNotReconciledByDevice( Long.valueOf( Long.parseLong( deviceId ) ) );
		for ( AlarmEntryEntity alarmEntryEntity : closedEntriesNotReconciled )
		{
			alarmEntriesToReconcile.add( alarmEntryEntity.toDataObject() );
		}

		scheduleAlarmClosureDispatchTask( Long.valueOf( Long.parseLong( deviceId ) ), alarmEntriesToReconcile );
	}

	private void scheduleAlarmClosureDispatchTask( Long deviceId, Collection<AlarmEntryView> closureRecords )
	{
		if ( closureRecords.isEmpty() )
		{
			LOG.debug( "No closure records to send to device {}. Aborting task scheduling.", deviceId );
			return;
		}

		AlarmClosureRecordsTask closureTask = new AlarmClosureRecordsTask( deviceId, closureRecords );
		taskScheduler.executeNow( closureTask );
	}

	public void purgeOldAlarms( long days )
	{
		long micros = DateUtils.getCurrentUTCTimeInMicros() - days * 86400000000L;
		int oldAlarms = alarmEntryDAO.deleteClosedAlarmsByLastInstanceTime( micros );

		List<Long> referencedAlarmSources = alarmEntryDAO.findReferencedDeletedAlarmSourceIds();
		List<AlarmSourceEntity> unreferencedSources = alarmSourceDAO.findAllUnreferencedDeleted( referencedAlarmSources );

		for ( AlarmSourceEntity source : unreferencedSources )
		{
			alarmSourceDAO.delete( source );
		}
		LOG.info( "Purging {} closed alarm entries older than {} days. Purging {} unreferenced deleted alarm sources", new Object[] {Integer.valueOf( oldAlarms ), Long.valueOf( days ), Integer.valueOf( unreferencedSources.size() )} );
	}

	public List<Long> getReferencedDeletedDevices()
	{
		return alarmSourceDAO.findAllDeletedDeviceIds();
	}

	public void markAlarmEntriesReconciled( Collection<AlarmEntryView> alarmEntries )
	{
		for ( AlarmEntryView alarmEntryView : alarmEntries )
		{
			AlarmEntryEntity alarmEntry = ( AlarmEntryEntity ) alarmEntryDAO.findById( Long.valueOf( Long.parseLong( alarmEntryView.getId() ) ) );
			if ( alarmEntry == null )
			{
				LOG.warn( "Alarm Entry with device entry id {} not found when marking it as reconciled.", alarmEntryView.getDeviceAlarmEntryId() );
			}
			else
			{
				alarmEntry.setReconciledWithDevice( true );
			}
		}
	}

	private void processAlarmEntryEvent( DeviceAlarmEvent alarmEvent )
	{
		Long deviceId = Long.valueOf( alarmEvent.getDeviceId() );
		String deviceAlarmSourceId = alarmEvent.getAlarmSourceID();
		String deviceAlarmEntryId = alarmEvent.getValue();

		AlarmSourceEntity alarmSource = alarmSourceDAO.findByDeviceIdAndDeviceAlarmSourceId( deviceId, deviceAlarmSourceId );

		if ( ( alarmSource == null ) && ( deviceAlarmSourceId.contains( "alarm.obstruction" ) ) )
		{
			createAlarmSourceByDeviceAlarmSourceId( deviceId, deviceAlarmSourceId );
			alarmSource = alarmSourceDAO.findByDeviceIdAndDeviceAlarmSourceId( deviceId, deviceAlarmSourceId );
		}

		if ( alarmSource != null )
		{
			Map<String, Object> extraInfo = alarmEvent.getDeviceExtraInfo();
			boolean updateAssociations = extraInfo.get( "assocId" ) != null;

			AlarmEntryEntity alarmEntry = updateAlarmEntryFromEvent( deviceAlarmEntryId, alarmSource, alarmEvent );
			if ( alarmEntry == null )
			{
				return;
			}

			if ( alarmEntry.isOpen() )
			{
				Set<Long> territoryInfo = getTerritoryInfoForAlarm( alarmSource );
				AlarmEntryEvent event = new AlarmEntryEvent( territoryInfo, alarmEntry.toDataObject(), updateAssociations, false );
				eventRegistry.sendEventAfterTransactionCommits( event );
			}

			LOG.debug( "Processed alarm.entry, id: {}, count: {}", new Object[] {alarmEntry.getId(), Integer.valueOf( alarmEntry.getCount() )} );
		}
		else
		{
			LOG.info( "Deferring alarm.entry event for deviceAlarmEntryId {} because alarm source deviceAlarmSourceId {} was not found ", deviceAlarmEntryId, deviceAlarmSourceId );

			DeferredEvent deferredEvent = new DeferredEvent( alarmEvent, "ALARM_SOURCE_" + deviceAlarmSourceId );
			deferredEventPool.add( deviceId.toString(), deferredEvent );
		}
	}

	private void processAlarmClosedEvent( DeviceAlarmEvent alarmEvent )
	{
		Long deviceId = Long.valueOf( alarmEvent.getDeviceId() );
		String deviceAlarmSourceId = alarmEvent.getAlarmSourceID();
		String deviceAlarmEntryId = alarmEvent.getValue();

		AlarmSourceEntity alarmSource = alarmSourceDAO.findByDeviceIdAndDeviceAlarmSourceId( deviceId, deviceAlarmSourceId );

		if ( alarmSource != null )
		{
			Map<String, Object> extraInfo = alarmEvent.getDeviceExtraInfo();

			String closedByUser = ( String ) extraInfo.get( "closedByUser" );
			String closedText = ( String ) extraInfo.get( "text" );
			boolean updateAssociations = extraInfo.get( "assocId" ) != null;

			AlarmEntryEntity alarmEntry = updateAlarmEntryFromEvent( deviceAlarmEntryId, alarmSource, alarmEvent );
			if ( alarmEntry == null )
			{
				return;
			}

			if ( alarmEntry.isOpen() )
			{
				alarmEntry.setClosedByUser( closedByUser );
				alarmEntry.setClosedTime( alarmEvent.getTimestamp() );
				if ( !CommonAppUtils.isNullOrEmptyString( closedText ) )
				{
					alarmEntry.setClosedText( closedText );
				}
				alarmEntry.setReconciledWithDevice( true );

				Set<Long> territoryInfo = getTerritoryInfoForAlarm( alarmSource );
				AlarmEntryClosedEvent event = new AlarmEntryClosedEvent( territoryInfo, alarmEntry.toDataObject(), updateAssociations );
				eventRegistry.sendEventAfterTransactionCommits( event );
			}
		}
		else
		{
			LOG.info( "Deferring alarm.entry.closed event for deviceAlarmEntryId {} because alarm source deviceAlarmSourceId {} was not found ", deviceAlarmEntryId, deviceAlarmSourceId );

			DeferredEvent deferredEvent = new DeferredEvent( alarmEvent, "ALARM_SOURCE_" + deviceAlarmSourceId );
			deferredEventPool.add( deviceId.toString(), deferredEvent );
		}
	}

	private AlarmEntryEntity updateAlarmEntryFromEvent( String deviceAlarmEntryId, AlarmSourceEntity alarmSource, DeviceAlarmEvent alarmEvent )
	{
		Map<String, Object> extraInfo = alarmEvent.getDeviceExtraInfo();

		String countString = ( String ) extraInfo.get( "count" );
		String firstInstanceString = ( String ) extraInfo.get( "first" );
		String lastInstanceString = ( String ) extraInfo.get( "last" );

		long lastInstance;
		long firstInstance;
		int count;

		try
		{
			count = Integer.parseInt( countString );
			firstInstance = Long.parseLong( firstInstanceString );

			if ( lastInstanceString != null )
			{
				lastInstance = Long.parseLong( lastInstanceString );
			}
			else
				lastInstance = alarmEvent.getTimestamp();
		}
		catch ( NumberFormatException e )
		{
			LOG.error( "Invalid alarm count or time, deviceId: {}, alarmSourceId: {}, deviceAlarmEntryId: {}, count: {}, firstInstance: {}, lastInstance: {}", new Object[] {alarmSource.getDeviceId(), alarmSource.getId(), deviceAlarmEntryId, extraInfo.get( "count" ), extraInfo.get( "first" ), extraInfo.get( "last" )} );
			return null;
		}

		AlarmEntryEntity alarmEntry = alarmEntryDAO.findByDeviceEntryIdAndSource( deviceAlarmEntryId, alarmSource );

		if ( alarmEntry == null )
		{
			alarmEntry = new AlarmEntryEntity( deviceAlarmEntryId, alarmSource );
			alarmEntryDAO.create( alarmEntry );

		}
		else if ( alarmEntry.getCount() > count )
		{
			LOG.info( "Ignoring alarm.entry event with older count, deviceAlarmEntryId:{}, deviceAlarmSourceID:{}, count:{}, newcount:{}", new Object[] {deviceAlarmEntryId, alarmSource.getDeviceAlarmSourceID(), Integer.valueOf( alarmEntry.getCount() ), Integer.valueOf( count )} );
			return null;
		}

		alarmEntry.setCount( count );
		alarmEntry.setFirstInstanceTime( firstInstance );
		alarmEntry.setLastInstanceTime( lastInstance );

		Set<String> associations = ( Set ) extraInfo.get( "assocId" );
		if ( associations != null )
		{

			alarmEntry.clearAssociatedChannels();

			for ( String association : associations )
			{
				if ( ( association != null ) && ( !association.isEmpty() ) )
				{
					alarmEntry.addToAssociatedChannels( association );
				}
			}
		}
		else
		{
			Set<String> alarmSourceSet = alarmSource.getAssociatedChannels();
			alarmEntry.setAssociatedChannels( alarmSourceSet );
		}

		return alarmEntry;
	}

	private void processAlarmStateChangeEvent( DeviceAlarmEvent alarmEvent )
	{
		Long deviceId = Long.valueOf( alarmEvent.getDeviceId() );
		String deviceAlarmSourceId = alarmEvent.getAlarmSourceID();
		String state = alarmEvent.getValue();
		AlarmState alarmState = AlarmState.fromValue( state );
		if ( alarmState == null )
		{
			LOG.error( "Invalid alarm state sent for alarm source id:" + deviceAlarmSourceId + ", state:" + state );
			return;
		}

		AlarmSourceEntity alarmSource = alarmSourceDAO.findByDeviceIdAndDeviceAlarmSourceId( deviceId, deviceAlarmSourceId );

		if ( alarmSource != null )
		{

			Map<String, Object> extraInfo = alarmEvent.getDeviceExtraInfo();
			String extState = ( String ) extraInfo.get( "extState" );
			if ( extState != null )
			{
				AlarmExtendedState alarmExtendedState = AlarmExtendedState.fromValue( extState );
				if ( alarmExtendedState == null )
				{
					LOG.error( "Invalid alarm extended state sent for alarm source id:" + alarmSource.getId() + ", state:" + state );
					return;
				}
				alarmSource.setExtendedState( alarmExtendedState );
			}
			else
			{
				alarmSource.setExtendedState( null );
			}

			if ( alarmState != alarmSource.getState() )
			{
				if ( alarmState == AlarmState.DISABLED )
				{
					LOG.info( "Alarm {} is disabled", alarmSource.getId() );
				}
				else if ( alarmSource.getState() == AlarmState.DISABLED )
				{
					LOG.info( "Alarm {} is enabled", alarmSource.getId() );
				}
				alarmSource.setState( alarmState );
			}

			Set<String> associations = ( Set ) extraInfo.get( "assocId" );
			boolean updateAssociations = false;
			if ( associations != null )
			{
				updateAssociations = true;
				alarmSource.clearAssociatedChannels();

				for ( String association : associations )
				{
					if ( ( association != null ) && ( !association.isEmpty() ) )
					{
						alarmSource.addToAssociatedChannels( association );
					}
				}
			}

			Long resourceId = getTopologyService().getAlarmSourceResourceId( alarmSource.getId() );

			Set<Long> territoryInfo = getTerritoryInfoForAlarm( alarmSource );
			Long lastStateChange = alarmSource.getLastStateChangeTime();
			alarmSource.setLastStateChangeTime( Long.valueOf( alarmEvent.getTimestamp() ) );
			AlarmStateEvent event = new AlarmStateEvent( territoryInfo, resourceId, lastStateChange, alarmSource.toDataObject(), alarmEvent.getTimestamp(), updateAssociations );
			eventRegistry.sendEventAfterTransactionCommits( event );

			updateAlarmSourceTopology( resourceId, alarmSource );

		}
		else if ( ( alarmSource == null ) && ( alarmState != AlarmState.DISABLED ) )
		{
			createAlarmSourceByDeviceAlarmSourceId( deviceId, deviceAlarmSourceId );
		}
	}

	private void createAlarmSourceByDeviceAlarmSourceId( Long deviceId, String deviceAlarmSourceId )
	{
		List<AlarmSource> alarmSources;

		try
		{
			alarmSources = deviceService.getAlarmSources( deviceId.toString() );
		}
		catch ( DeviceException e )
		{
			LOG.warn( "Error when requesting alarm sources from device {}. error details {}", new Object[] {deviceId, e.getMessage()} );
			return;
		}

		for ( AlarmSource deviceAlarmSource : alarmSources )
		{
			if ( deviceAlarmSource.getId().equals( deviceAlarmSourceId ) )
			{
				AlarmSourceView alarmSourceView = new AlarmSourceView( null, deviceAlarmSource.getId(), deviceId.toString(), deviceAlarmSource.getType(), deviceAlarmSource.getName(), Arrays.asList( deviceAlarmSource.getAssocIds() ), AlarmState.fromValue( deviceAlarmSource.getState() ), AlarmExtendedState.fromValue( deviceAlarmSource.getExtState() ) );
				createAlarmSource( alarmSourceView, true );
				return;
			}
		}
	}

	private void updateAlarmSourceTopology( Long resourceId, AlarmSourceEntity alarmSource )
	{
		try
		{
			AlarmSourceResource alarmSourceResource = new AlarmSourceResource();

			alarmSourceResource.setId( resourceId );
			alarmSourceResource.setName( alarmSource.getName() );
			getTopologyService().updateResource( alarmSourceResource );
		}
		catch ( TopologyException ex )
		{
			LOG.error( "Error updating alarm source topology, id:" + alarmSource.getId(), ex );
		}
	}

	private void processAlarmSourcesChangedEvent( DeviceAlarmEvent alarmEvent )
	{
		String deviceId = alarmEvent.getDeviceId();
		refreshDeviceAlarmSources( deviceId, false );
	}

	private void refreshDeviceAlarmSources( String deviceId, boolean isDeviceRegistered )
	{
		Map<String, AlarmSource> alarmSourcesFromDevice = new LinkedHashMap();
		try
		{
			List<AlarmSource> alarmSources = deviceService.getAlarmSources( deviceId );
			if ( alarmSources != null )
			{
				for ( AlarmSource alarmSource : alarmSources )
				{
					alarmSourcesFromDevice.put( alarmSource.getId(), alarmSource );
				}
			}
		}
		catch ( DeviceException e )
		{
			LOG.warn( "Error when requesting alarm sources from device {}. error details {}", new Object[] {deviceId, e.getMessage()} );
			return;
		}

		List<AlarmSourceEntity> alarmSources = alarmSourceDAO.findAllByDeviceId( Long.valueOf( Long.parseLong( deviceId ) ) );
		Map<String, AlarmSourceEntity> alarmSourcesFromDatabase = new HashMap();
		for ( AlarmSourceEntity alarmSource : alarmSources )
		{
			alarmSourcesFromDatabase.put( alarmSource.getDeviceAlarmSourceID(), alarmSource );
		}
		Set<String> alarmSourcesFromDatabaseIds = alarmSourcesFromDatabase.keySet();

		Set<String> deviceAlarmSourceIds = new HashSet();
		for ( AlarmSource alarmSource : alarmSourcesFromDevice.values() )
		{
			deviceAlarmSourceIds.add( alarmSource.getId() );
		}

		Sets.SetView<String> removedSources = Sets.difference( alarmSourcesFromDatabaseIds, deviceAlarmSourceIds );
		for ( String sourceId : removedSources )
		{
			AlarmSourceEntity alarmSourceEntity = ( AlarmSourceEntity ) alarmSourcesFromDatabase.get( sourceId );
			deleteAlarmSource( alarmSourceEntity, false );
		}

		Sets.SetView<String> newSources = Sets.difference( deviceAlarmSourceIds, alarmSourcesFromDatabaseIds );
		for ( String newSourceId : newSources )
		{
			AlarmSource deviceAlarmSource = ( AlarmSource ) alarmSourcesFromDevice.get( newSourceId );
			AlarmState state = AlarmState.fromValue( deviceAlarmSource.getState() );
			if ( state != AlarmState.DISABLED )
			{
				AlarmSourceView alarmSource = new AlarmSourceView( null, deviceAlarmSource.getId(), deviceId, deviceAlarmSource.getType(), deviceAlarmSource.getName(), Arrays.asList( deviceAlarmSource.getAssocIds() ), state, AlarmExtendedState.fromValue( deviceAlarmSource.getExtState() ) );
				createAlarmSource( alarmSource, isDeviceRegistered );
			}
		}

		alarmSourcesFromDatabaseIds.retainAll( deviceAlarmSourceIds );
		for ( String deviceAlarmSourceId : alarmSourcesFromDatabaseIds )
		{
			AlarmSource alarmSource = ( AlarmSource ) alarmSourcesFromDevice.get( deviceAlarmSourceId );

			AlarmSourceEntity alarmSourceEntity = ( AlarmSourceEntity ) alarmSourcesFromDatabase.get( deviceAlarmSourceId );

			if ( alarmSourceEntity.readFromTransportObject( alarmSource ) )
			{
				LOG.info( "Updating Alarm Source: " + alarmSourceEntity.getId() );
				Long resourceId = getTopologyService().getAlarmSourceResourceId( alarmSourceEntity.getId() );
				updateAlarmSourceTopology( resourceId, alarmSourceEntity );
			}
		}
	}

	private boolean deleteAlarmSource( AlarmSourceEntity alarmSource, boolean isDeviceDeleted )
	{
		if ( alarmSource == null )
		{
			return false;
		}

		if ( !isDeviceDeleted )
		{
			LOG.info( "Deleting Alarm Source: " + alarmSource.getId() );
		}

		List<AlarmEntryEntity> alarmEntries = Collections.emptyList();
		try
		{
			alarmEntries = alarmEntryDAO.findByAlarmSource( alarmSource );
		}
		catch ( Exception e )
		{
			LOG.warn( "Exception trying to get alarm entries by alarm source " + alarmSource.getDeviceAlarmSourceID(), e );
		}

		boolean hasEntryReferences = !alarmEntries.isEmpty();

		for ( AlarmEntryEntity alarmEntry : alarmEntries )
		{
			if ( alarmEntry.isOpen() )
			{
				alarmEntry.setClosedTime( DateUtils.getCurrentUTCTimeInMicros() );
				alarmEntry.setClosedByUser( "CES (" + ServerUtils.HOSTNAME_CACHED + ")" );
				if ( isDeviceDeleted )
				{
					alarmEntry.setClosedText( "@StringCode_DeviceRemoved" );
				}
				else
				{
					alarmEntry.setClosedText( "@StringCode_AlarmRemoved" );
				}
			}

			alarmEntry.setReconciledWithDevice( true );
			if ( isDeviceDeleted )
			{
				alarmEntry.clearAssociatedChannels();
			}
		}

		if ( alarmSource.isDeleted() )
		{
			return true;
		}

		alarmSource.setDeleted( true );
		alarmSource.clearAssociatedChannels();

		ResourceTopologyServiceIF topologyService = getTopologyService();

		Long resourceId = topologyService.getAlarmSourceResourceId( alarmSource.getId() );

		if ( resourceId != null )
		{
			if ( !isDeviceDeleted )
			{
				try
				{
					topologyService.removeResource( resourceId );
				}
				catch ( TopologyException ex )
				{
					LOG.error( "Error removing AlarmSourceResource id:" + resourceId, ex );
				}
			}

			AlarmDeletedEvent event = new AlarmDeletedEvent( resourceId );
			eventRegistry.sendEventAfterTransactionCommits( event );
		}

		if ( !hasEntryReferences )
		{
			alarmSourceDAO.delete( alarmSource );
			return false;
		}
		return true;
	}

	public void createAlarmSource( AlarmSourceView alarmSource, boolean isCreatedFirstTime )
	{
		AlarmSourceEntity alarmSourceEntity = new AlarmSourceEntity();
		alarmSourceEntity.readFromDataObject( alarmSource );

		alarmSourceDAO.create( alarmSourceEntity );

		if ( !isCreatedFirstTime )
		{
			LOG.info( "Created Alarm Source: {} - {}", alarmSourceEntity.getId(), alarmSourceEntity.getName() );
		}

		AlarmSourceResource alarmSourceResource = new AlarmSourceResource();
		alarmSourceResource.setAlarmSourceId( alarmSourceEntity.getIdAsString() );

		Long parentResourceId = getTopologyService().getResourceIdByDeviceId( alarmSourceEntity.getDeviceIdAsString() );
		try
		{
			getTopologyService().createResource( alarmSourceResource, parentResourceId, ResourceAssociationType.ALARM_SOURCE.name() );
		}
		catch ( TopologyException ex )
		{
			LOG.error( "Error creating AlarmSourceResource for deviceId:" + alarmSourceEntity.getDeviceIdAsString(), ex );
		}

		deferredEventPool.trigger( alarmSourceEntity.getDeviceIdAsString(), "ALARM_SOURCE_" + alarmSourceEntity.getDeviceAlarmSourceID() );
	}

	private Set<Long> getTerritoryInfoForAlarm( AlarmSourceEntity alarmSource )
	{
		Set<Long> result = new HashSet();

		Long alarmResourceId = getTopologyService().getAlarmSourceResourceId( alarmSource.getId() );
		result.add( alarmResourceId );

		List<LinkResource> logicalAlarmSources = getTopologyService().getLinkResources( alarmResourceId );
		if ( logicalAlarmSources == null )
		{
			return null;
		}

		for ( LinkResource logicalAlarmSource : logicalAlarmSources )
		{
			result.add( logicalAlarmSource.getId() );
		}
		return result;
	}

	private List<String> getUserTerritoryIds( String userName )
	{
		List<String> result = new ArrayList();

		MemberView member = getUserService().getMember( userName );

		for ( Long id : member.getAllRoots( false ) )
		{
			result.add( id.toString() );
		}

		return result;
	}

	private Set<Long> getAlarmSourcesUnderUserTerritory( String userName )
	{
		Set<Long> alarmSourceIds = new HashSet();
		List<Long> deviceIds = new ArrayList();
		try
		{
			List<String> territoryIds = getUserTerritoryIds( userName );

			if ( !territoryIds.isEmpty() )
			{
				for ( String territoryId : territoryIds )
				{
					Resource resource = getTopologyService().getResource( Long.valueOf( Long.parseLong( territoryId ) ) );

					List<Resource> list = resource.createResourceList();

					for ( Resource res : list )
					{
						if ( ( res instanceof AlarmSourceResource ) )
						{
							alarmSourceIds.add( Long.valueOf( Long.parseLong( ( ( AlarmSourceResource ) res ).getAlarmSourceId() ) ) );
						}
						else if ( ( res instanceof AlarmSourceLinkResource ) )
						{
							alarmSourceIds.add( Long.valueOf( Long.parseLong( ( ( AlarmSourceLinkResource ) res ).getAlarmSourceId() ) ) );
						}
						else if ( ( res instanceof DeviceResource ) )
						{
							DeviceResource deviceResource = ( DeviceResource ) res;

							if ( deviceResource.isRootDevice() )
							{
								deviceIds.add( Long.valueOf( Long.parseLong( deviceResource.getDeviceId() ) ) );
							}
						}
					}
				}

				if ( !deviceIds.isEmpty() )
				{
					List<Long> deletedAlarmSourceIds = alarmSourceDAO.findAllDeletedAlarmSourceIds( deviceIds );
					alarmSourceIds.addAll( deletedAlarmSourceIds );
				}

				List<Long> deletedDeviceAlarmSourceIds = alarmSourceDAO.findAllDeletedDeviceAlarmSourceIds( territoryIds );
				alarmSourceIds.addAll( deletedDeviceAlarmSourceIds );
			}
		}
		catch ( Exception e )
		{
			LOG.warn( "Error when retrieving territory user information for user {} {}", new Object[] {userName, e.getMessage()} );
		}

		return alarmSourceIds;
	}

	public AlarmSourceView getAlarmSourceData( Long deviceId, String deviceAlarmSourceId )
	{
		if ( ( deviceId == null ) || ( deviceAlarmSourceId == null ) )
		{
			return null;
		}
		AlarmSourceEntity alarmSource = alarmSourceDAO.findByDeviceIdAndDeviceAlarmSourceId( deviceId, deviceAlarmSourceId );
		if ( alarmSource != null )
		{
			return alarmSource.toDataObject();
		}
		return null;
	}

	protected void auditAlarm( AuditEventNameEnum auditEvent, String deviceId, String deviceAlarmSourceId, String comments )
	{
		if ( CommonAppUtils.getUsernameFromSecurityContext() != null )
		{
			AuditView.Builder auditBuilder = new AuditView.Builder( auditEvent.getName() ).addAlarmSourceToAudit( deviceId, deviceAlarmSourceId );

			if ( !CommonAppUtils.isNullOrEmptyString( comments ) )
			{
				auditBuilder.addDetailsPair( "note_presence", comments );
			}

			eventRegistry.sendEventAfterTransactionCommits( new AuditEvent( auditBuilder.build() ) );
		}
	}

	protected void auditAlarmSearchQuery( List<Long> alarmSourcesToSearch, boolean includeOpenEntries, boolean includeClosedEntries, long startTime, long endTime, int maxEntries )
	{
		if ( CommonAppUtils.getUsernameFromSecurityContext() != null )
		{
			Map<String, String> queryParams = new HashMap();
			queryParams.put( "alarmSourceIDs", CoreJsonSerializer.toJson( alarmSourcesToSearch ) );
			queryParams.put( "includeOpenEntries", String.valueOf( includeOpenEntries ) );
			queryParams.put( "includeClosedEntries", String.valueOf( includeClosedEntries ) );
			queryParams.put( "startTime", String.valueOf( startTime ) );
			queryParams.put( "endTime", String.valueOf( endTime ) );
			queryParams.put( "maxEntries", String.valueOf( maxEntries ) );
			AuditView auditView = new AuditView.Builder( AuditEventNameEnum.ALARM_HISTORY_SEARCH.getName() ).addDetailsPair( "query_parameters", CoreJsonSerializer.toJson( queryParams ) ).build();
			eventRegistry.send( new AuditEvent( auditView ) );
		}
	}

	private ResourceTopologyServiceIF getTopologyService()
	{
		if ( topologyService == null )
		{
			topologyService = ( ( ResourceTopologyServiceIF ) ApplicationContextSupport.getBean( "resourceTopologyService_internal" ) );
		}
		return topologyService;
	}

	private UserService getUserService()
	{
		if ( userService == null )
		{
			userService = ( ( UserService ) ApplicationContextSupport.getBean( "userService_internal" ) );
		}
		return userService;
	}

	public void updateAlarmEntryDetails( String username, String alarmEntryId, Set<AlarmDetailEnum> alarmDetails, String note ) throws AlarmException
	{
		Long id = Long.valueOf( alarmEntryId );

		AlarmEntryEntity entity = ( AlarmEntryEntity ) alarmEntryDAO.findById( id );

		if ( entity == null )
		{
			throw new AlarmException( AlarmExceptionTypeEnum.ALARM_NOT_FOUND, "Alarm Entry with ID=" + alarmEntryId + " not found" );
		}

		entity.setAlarmDetails( alarmDetails );
		entity.setClosedText( note );

		Set<Long> territoryInfo = getTerritoryInfoForAlarm( entity.getAlarmSource() );

		AlarmEntryEvent event = new AlarmEntryEvent( territoryInfo, entity.toDataObject(), false, true );
		eventRegistry.sendEventAfterTransactionCommits( event );

		LOG.debug( "Alarm Entry " + id + " has been updated by User: " + username );
	}

	public void setDeviceService( DeviceService deviceService )
	{
		this.deviceService = deviceService;
	}

	public void setAlarmEntryDAO( AlarmEntryDAO alarmEntryDAO )
	{
		this.alarmEntryDAO = alarmEntryDAO;
	}

	public void setAlarmSourceDAO( AlarmSourceDAO alarmSourceDAO )
	{
		this.alarmSourceDAO = alarmSourceDAO;
	}

	public void setEventRegistry( EventRegistry eventRegistry )
	{
		this.eventRegistry = eventRegistry;
	}

	public void setDeferredEventPool( DeferredEventPool deferredEventPool )
	{
		this.deferredEventPool = deferredEventPool;
	}

	public void setTaskScheduler( TaskScheduler taskScheduler )
	{
		this.taskScheduler = taskScheduler;
	}
}
