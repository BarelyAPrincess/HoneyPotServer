package com.marchnetworks.health.comms;

import com.marchnetworks.audit.data.AuditEventNameEnum;
import com.marchnetworks.audit.data.AuditView;
import com.marchnetworks.audit.events.AuditEvent;
import com.marchnetworks.command.api.alert.AlertCategoryEnum;
import com.marchnetworks.command.api.alert.AlertDefinitionEnum;
import com.marchnetworks.command.api.metrics.input.BucketCounterInput;
import com.marchnetworks.command.api.query.Criteria;
import com.marchnetworks.command.api.query.Restrictions;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.device.DeviceEventsEnum;
import com.marchnetworks.command.common.device.data.ConnectState;
import com.marchnetworks.command.common.scheduling.TaskScheduler;
import com.marchnetworks.command.common.topology.ResourceRootType;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.command.common.transport.data.Event;
import com.marchnetworks.command.common.transport.data.Pair;
import com.marchnetworks.command.common.user.data.MemberView;
import com.marchnetworks.common.device.DeletedDevice;
import com.marchnetworks.common.diagnostics.metrics.MetricsTypes;
import com.marchnetworks.common.event.EventTypesEnum;
import com.marchnetworks.common.serialization.CoreJsonSerializer;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.transport.datamodel.DeviceException;
import com.marchnetworks.common.types.AlertThresholdDefinitionEnum;
import com.marchnetworks.common.types.AlertUserStateEnum;
import com.marchnetworks.common.types.DeviceExceptionTypes;
import com.marchnetworks.common.utils.DateUtils;
import com.marchnetworks.health.alerts.AlertConfigEntity;
import com.marchnetworks.health.alerts.AlertEntity;
import com.marchnetworks.health.alerts.AlertEntityFactory;
import com.marchnetworks.health.alerts.DeviceAlertEntity;
import com.marchnetworks.health.alerts.ServerAlertEntity;
import com.marchnetworks.health.dao.AlertConfigDAO;
import com.marchnetworks.health.dao.AlertDAO;
import com.marchnetworks.health.dao.DeviceAlertDAO;
import com.marchnetworks.health.dao.ServerAlertDAO;
import com.marchnetworks.health.data.AlertData;
import com.marchnetworks.health.data.AlertThresholdData;
import com.marchnetworks.health.data.DefaultAlertThresholdData;
import com.marchnetworks.health.data.HealthNotificationSummaryData;
import com.marchnetworks.health.data.HealthSummaryCategoryEnum;
import com.marchnetworks.health.data.HealthSummaryCodeCategoryMapEnum;
import com.marchnetworks.health.event.AlertChangedEvent;
import com.marchnetworks.health.event.AlertCreatedEvent;
import com.marchnetworks.health.event.AlertThresholdsUpdatedEvent;
import com.marchnetworks.health.event.AlertUpdatedEvent;
import com.marchnetworks.health.event.HealthEventEnum;
import com.marchnetworks.health.input.AlertInput;
import com.marchnetworks.health.input.DeviceAlertInput;
import com.marchnetworks.health.input.ServerAlertInput;
import com.marchnetworks.health.search.AlertSearchQuery;
import com.marchnetworks.health.search.AlertSearchResults;
import com.marchnetworks.health.task.AlertClosureTask;
import com.marchnetworks.health.task.AlertQueryTask;
import com.marchnetworks.health.task.AlertThresholdSetTask;
import com.marchnetworks.management.instrumentation.DeviceCapabilityService;
import com.marchnetworks.management.instrumentation.DeviceService;
import com.marchnetworks.management.instrumentation.events.DeviceAlertClosureDispatchEvent;
import com.marchnetworks.management.instrumentation.events.DeviceSetAlertConfigEvent;
import com.marchnetworks.management.instrumentation.pooling.DeferredEvent;
import com.marchnetworks.management.instrumentation.pooling.DeferredEventPool;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;
import com.marchnetworks.management.user.UserService;
import com.marchnetworks.monitoring.metrics.event.MetricsEvent;
import com.marchnetworks.server.communications.transport.datamodel.AlertEntry;
import com.marchnetworks.server.event.EventRegistry;
import com.marchnetworks.server.event.health.HealthFault;
import com.marchnetworks.server.event.health.HealthFaultTypeEnum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AlertManagerImpl implements AlertManagerIF
{
	private static final Logger LOG = LoggerFactory.getLogger( AlertManagerImpl.class );

	private static final int MAX_CLOSED_ALERTS = 1000;

	private DeviceService deviceService;
	private AlertDAO alertDAO;
	private DeviceAlertDAO deviceAlertDAO;
	private ServerAlertDAO serverAlertDAO;
	private ResourceTopologyServiceIF topologyService;
	private EventRegistry eventRegistry;
	private AlertConfigDAO alertConfigDAO;
	private DeferredEventPool deferredEventPool;
	private TaskScheduler taskScheduler;
	private DeviceCapabilityService deviceCapabilityService;
	private UserService userService;

	public void init()
	{
		List<AlertThresholdData> alertConfigThresholds = new ArrayList();
		if ( alertConfigDAO.getRowCount().longValue() > 0L )
		{
			return;
		}

		for ( AlertThresholdDefinitionEnum atd : AlertThresholdDefinitionEnum.values() )
		{
			alertConfigThresholds.add( new AlertThresholdData( atd ) );
		}
		AlertConfigEntity alertConfigEntity = new AlertConfigEntity();
		alertConfigEntity.setConfigDataThresholds( alertConfigThresholds );
		alertConfigDAO.create( alertConfigEntity );
	}

	public boolean isHealthAlert( Event eventModel )
	{
		boolean isAlert = false;
		String alertCode = getAlertCode( eventModel );
		AlertDefinitionEnum def = AlertDefinitionEnum.fromPath( alertCode );
		if ( def != null )
		{
			if ( !def.getTriggerValue().equals( "" ) )
			{
				return def.getTriggerValue().equalsIgnoreCase( eventModel.getValue().getStringValue() );
			}
			isAlert = true;
		}
		return isAlert;
	}

	public DeviceAlertInput createDeviceHealthAlert( String deviceId, Event eventModel, boolean hasCapability )
	{
		String alertId = null;
		String alertSourceId = "";
		String alertCode = getAlertCode( eventModel );
		long alertTime = 0L;
		long lastTime = 0L;
		long resolvedTime = 0L;
		String value = "";
		int count = 0;

		int duration = 0;
		int frequency = 0;
		Pair[] pairs = null;

		if ( hasCapability )
		{
			Pair[] eventPairs = eventModel.getInfo();
			List<Pair> pairList = new ArrayList();

			alertId = eventModel.getSource();

			for ( Pair eventPair : eventPairs )
			{
				String eventPairName = eventPair.getName();
				if ( eventPairName.equals( "source" ) )
				{
					alertSourceId = eventPair.getValue();
				}
				else if ( eventPairName.equals( "value" ) )
				{
					value = eventPair.getValue();
				}
				else if ( eventPairName.equals( "count" ) )
				{
					count = Integer.parseInt( eventPair.getValue() );
				}
				else if ( eventPairName.equals( "durationCount" ) )
				{
					duration = Integer.parseInt( eventPair.getValue() );
				}
				else if ( eventPairName.equals( "frequencyCount" ) )
				{
					frequency = Integer.parseInt( eventPair.getValue() );
				}
				else if ( eventPairName.startsWith( "info" ) )
				{
					String[] nameParts = eventPairName.split( "[.]" );
					pairList.add( new Pair( nameParts[1], eventPair.getValue() ) );
				}
				else if ( eventPairName.startsWith( "first" ) )
				{
					alertTime = Long.parseLong( eventPair.getValue() ) / 1000L;
				}
				else if ( eventPairName.startsWith( "lastResolved" ) )
				{
					resolvedTime = Long.parseLong( eventPair.getValue() ) / 1000L;
				}
				else if ( eventPairName.startsWith( "last" ) )
				{
					lastTime = Long.parseLong( eventPair.getValue() ) / 1000L;
				}
			}

			boolean deviceState = !eventModel.getValue().getStringValue().equals( "resolved" );
			pairs = ( Pair[] ) pairList.toArray( new Pair[pairList.size()] );

			return new DeviceAlertInput( deviceId, alertId, count, alertCode, AlertCategoryEnum.UNKNOWN, alertSourceId, alertTime, lastTime, resolvedTime, AlertInput.pairsToString( pairs ), value, deviceState, duration, frequency );
		}

		AlertDefinitionEnum def = AlertDefinitionEnum.fromPath( alertCode );
		if ( def == null )
		{
			LOG.warn( "Unable to process alert. Unknown AlertCode:{}.", alertCode );
			return null;
		}

		alertSourceId = eventModel.getSource();
		value = eventModel.getValue().convertToString();
		count = 0;

		boolean deviceState = true;
		for ( String clearValue : def.getClearedValue() )
		{
			if ( clearValue.equalsIgnoreCase( value ) )
			{
				deviceState = false;
				break;
			}
		}

		alertTime = eventModel.getTimestamp().getTicks() / 1000L;

		lastTime = eventModel.getTimestamp().getTicks() / 1000L;

		if ( !deviceState )
		{
			resolvedTime = lastTime;
		}

		pairs = eventModel.getInfo();

		return new DeviceAlertInput( deviceId, alertId, count, def, alertSourceId, alertTime, lastTime, resolvedTime, AlertInput.pairsToString( pairs ), value, deviceState, duration, frequency );
	}

	public DeviceAlertInput createDeviceHealthAlert( String deviceId, AlertEntry alertEntry )
	{
		return new DeviceAlertInput( deviceId, alertEntry.getId(), alertEntry.getCount(), alertEntry.getAlertCode(), AlertCategoryEnum.UNKNOWN, alertEntry.getSource(), alertEntry.getFirst().getTicks() / 1000L, alertEntry.getLast().getTicks() / 1000L, alertEntry.getLastResolved().getTicks() / 1000L, AlertInput.pairsToString( alertEntry.getInfo() ), alertEntry.getValue().getStringValue(), !alertEntry.getState().equals( "resolved" ), alertEntry.getDurationCount(), alertEntry.getFrequencyCount() );
	}

	public void processHealthAlerts( List<AlertInput> alertInputs )
	{
		for ( AlertInput alertInput : alertInputs )
		{
			processHealthAlert( alertInput );
		}
	}

	public void processHealthAlert( AlertInput alertInput )
	{
		if ( ( alertInput instanceof DeviceAlertInput ) )
		{
			processDeviceHealthAlert( ( DeviceAlertInput ) alertInput );
		}
		else if ( ( alertInput instanceof ServerAlertInput ) )
		{
			processServerHealthAlert( ( ServerAlertInput ) alertInput );
		}

		eventRegistry.sendEventAfterTransactionCommits( new MetricsEvent( new BucketCounterInput( MetricsTypes.ALERTS.getName(), alertInput.getAlertCode() ) ) );
	}

	public AlertData[] getOpenAlerts( String userName ) throws HealthFault
	{
		List<String> deviceIds = getDevicesUnderUserTerritory( userName );

		List<AlertData> openAlerts = new ArrayList();

		if ( deviceIds.size() > 0 )
		{
			List<DeviceAlertEntity> openAlertsByDevice = deviceAlertDAO.findAllUserOpenAlertsByDevices( deviceIds );
			for ( AlertEntity alertEntity : openAlertsByDevice )
			{
				openAlerts.add( alertEntity.toDataObject() );
			}
		}

		List<ServerAlertEntity> openAlertsByServer = serverAlertDAO.findAllUserOpenAlertsByServer( "1" );
		for ( AlertEntity alertEntity : openAlertsByServer )
		{
			openAlerts.add( alertEntity.toDataObject() );
		}

		AlertData[] results = ( AlertData[] ) openAlerts.toArray( new AlertData[openAlerts.size()] );
		return results;
	}

	public void setAlertUserState( Long ID, AlertUserStateEnum userState ) throws HealthFault
	{
		AlertEntity alert = ( AlertEntity ) alertDAO.findById( ID );

		if ( alert == null )
		{
			LOG.info( "Can not find the alert id=", ID );
			throw new HealthFault( HealthFaultTypeEnum.ALERT_NOT_FOUND );
		}

		List<AlertEntity> alertEntityList = new ArrayList();
		alertEntityList.add( alert );

		setAlertUserStates( alertEntityList, userState );
	}

	public void setAlertUserStates( long[] ids, AlertUserStateEnum userState ) throws HealthFault
	{
		List<AlertEntity> alertEntityList = new ArrayList();
		AlertEntity alert = null;
		for ( long id : ids )
		{
			alert = ( AlertEntity ) alertDAO.findById( Long.valueOf( id ) );
			if ( alert == null )
			{
				LOG.info( "Can not find the alert id=", Long.valueOf( id ) );
				throw new HealthFault( HealthFaultTypeEnum.ALERT_NOT_FOUND );
			}
			alertEntityList.add( alert );
		}

		try
		{
			setAlertUserStates( alertEntityList, userState );
		}
		catch ( HealthFault ex )
		{
			if ( ex.getError() != HealthFaultTypeEnum.ALERT_ALREADY_CLOSED )
			{
				throw ex;
			}
		}
	}

	public AlertData getAlertById( long ID ) throws HealthFault
	{
		AlertEntity alert = null;
		try
		{
			alert = ( AlertEntity ) alertDAO.findById( Long.valueOf( ID ) );
		}
		catch ( Exception e )
		{
			LOG.info( "Fail to query alert ID: " + ID, e );
			throw new HealthFault( HealthFaultTypeEnum.QUERY_FAILED );
		}
		if ( alert == null )
		{
			LOG.info( "Can not find the alert ID: ", Long.valueOf( ID ) );
			throw new HealthFault( HealthFaultTypeEnum.ALERT_NOT_FOUND );
		}

		return alert.toDataObject();
	}

	public void purgeOldAlerts( Long days )
	{
		long a_StopTime = DateUtils.getCurrentUTCTimeInMillis() - days.longValue() * 86400000L;
		int alertsCleaned = alertDAO.deleteClosedAlertsByClosedTime( a_StopTime );
		LOG.info( alertsCleaned + " historical closed alert(s) older than " + days + " days were purged" );
	}

	public AlertSearchResults searchHistoricalClosedAlerts( String userName, AlertSearchQuery searchQuery ) throws HealthFault
	{
		LOG.debug( "searchHistoricalAlerts: {} searchQuery.getTimeField()={}", searchQuery, searchQuery.getTimeField() );

		AlertSearchResults results = new AlertSearchResults( searchQuery );
		LinkedList<AlertData> historicalAlerts = new LinkedList();

		List<String> deviceIds = getDevicesUnderUserTerritory( userName );
		List<String> territoryIds = getUserTerritoryIds( userName );

		List<String> usedDevices = deviceAlertDAO.findAllClosedDeviceIds( searchQuery );

		HashSet<String> deviceSet = new HashSet( usedDevices );
		for ( int i = 0; i < deviceIds.size(); i++ )
		{
			if ( !deviceSet.contains( deviceIds.get( i ) ) )
			{
				deviceIds.remove( i );
				i--;
			}
		}

		if ( deviceIds.size() > 1000 )
		{
			deviceIds = deviceIds.subList( 0, 1000 );
		}
		List<AlertEntity> alerts = alertDAO.findClosedAlertsByQuery( territoryIds, deviceIds, searchQuery, 1000 );

		for ( AlertEntity alert : alerts )
		{
			AlertData alertData = alert.toDataObject();
			historicalAlerts.add( alertData );
		}

		results.setResults( ( AlertData[] ) historicalAlerts.toArray( new AlertData[historicalAlerts.size()] ) );

		return results;
	}

	public void processDeviceRegistered( String deviceId, boolean isMassRegister )
	{
		scheduleAlertThresholdPush( deviceId, true );
		if ( !isMassRegister )
		{
			scheduleAlertQueryDispatchTask( deviceId );
		}
	}

	public void processDeviceUnregistered( String deviceId )
	{
		String rootDeviceId = deviceService.findRootDevice( deviceId );
		if ( rootDeviceId == null )
		{
			LOG.warn( "Root Device not found when processing device.unregistered for device: ", deviceId );
			return;
		}

		List<String> channelIds = deviceService.findChannelIdsFromDevice( deviceId );

		if ( ( channelIds != null ) && ( !channelIds.isEmpty() ) )
		{
			List<DeviceAlertEntity> alertsBySourceDevice = deviceAlertDAO.findAllAlertsByRootDeviceAndSourceIdList( rootDeviceId, channelIds );

			for ( DeviceAlertEntity alertEntity : alertsBySourceDevice )
			{
				String channelName = deviceService.findChannelNameFromId( deviceId, alertEntity.getSourceId() );
				if ( channelName != null )
				{
					alertEntity.setChannelName( channelName );
				}
			}
		}

		DeletedDevice deletedDevice;
		if ( deviceId.equals( rootDeviceId ) )
		{
			List<DeviceAlertEntity> alertsByDevice = deviceAlertDAO.findAllAlertsByDevice( deviceId );

			if ( !alertsByDevice.isEmpty() )
			{
				deletedDevice = deviceService.createDeletedDevice( deviceId );
				if ( deletedDevice != null )
				{
					for ( DeviceAlertEntity alertEntity : alertsByDevice )
					{
						alertEntity.setUserState( AlertUserStateEnum.CLOSED );
						alertEntity.setLastUserStateChangedTime( System.currentTimeMillis() );
						alertEntity.setDeletedDevice( deletedDevice );
						alertEntity.setDeviceId( null );
					}
				}
			}
		}
	}

	public void processDeviceChannelRemoved( String deviceId, String channelId )
	{
		String rootDeviceId = deviceService.findRootDevice( deviceId );
		if ( rootDeviceId == null )
		{
			LOG.warn( "Root Device not found when processing channel.removed. Channel: ", channelId );
			return;
		}

		List<DeviceAlertEntity> alertsByDevice = deviceAlertDAO.findAllAlertsByRootDeviceAndSourceId( rootDeviceId, channelId );
		String channelName;
		if ( !alertsByDevice.isEmpty() )
		{
			channelName = deviceService.findChannelNameFromId( deviceId, channelId );

			for ( DeviceAlertEntity alertEntity : alertsByDevice )
			{
				alertEntity.setUserState( AlertUserStateEnum.CLOSED );
				alertEntity.setChannelName( channelName );
			}
		}
	}

	public void processDeviceReconciliation( String deviceId )
	{
		scheduleAlertThresholdPush( deviceId, false );
		scheduleAlertQueryDispatchTask( deviceId );
		sendAlertClosuresToDevice( deviceId, null );
	}

	public void processAlertClosureDispatch( String deviceId )
	{
		sendAlertClosuresToDevice( deviceId, null );
	}

	public void processSetAlertConfig( String deviceId )
	{
		scheduleAlertThresholdPush( deviceId, true );
	}

	private void scheduleAlertThresholdPush( String deviceId, boolean forceUpdate )
	{
		DeviceResource deviceResource = getTopologyService().getDeviceResourceByDeviceId( deviceId );
		if ( ( deviceResource != null ) && ( deviceResource.isRootDevice() ) )
		{
			AlertThresholdSetTask setThresholdTask = new AlertThresholdSetTask( Long.valueOf( Long.parseLong( deviceId ) ), alertConfigDAO.getAlertConfig().getAlertConfig(), forceUpdate );
			taskScheduler.executeFixedPoolSerial( setThresholdTask, deviceId );
		}
	}

	public void processDeviceAlertClosure( String deviceId, String deviceAlertId )
	{
		DeviceAlertEntity deviceAlert = deviceAlertDAO.findAlert( deviceId, deviceAlertId );
		if ( deviceAlert == null )
		{
			LOG.warn( "Can not find the device alert id=", deviceAlertId );
			return;
		}
		if ( deviceAlert.getUserState() == AlertUserStateEnum.CLOSED )
		{
			LOG.warn( "Not closing device alert id=" + deviceAlert.getId() + " on device id=" + deviceAlert.getDeviceId() + ". Alert already closed" );
			return;
		}

		deviceAlert.setUserState( AlertUserStateEnum.CLOSED );
		deviceAlert.setReconciledWithDevice( Boolean.valueOf( true ) );
		Set<Long> territoryInfo = getTerritoryInfoForEvent( deviceAlert.getDeviceId() );
		AlertChangedEvent event = new AlertChangedEvent( HealthEventEnum.DEVICE_ALERT_USER_STATE_CHANGE.name(), territoryInfo, EventTypesEnum.HEALTH_CLOSED, String.valueOf( deviceAlert.getId() ) );
		eventRegistry.sendEventAfterTransactionCommits( event );
	}

	private void processDeviceHealthAlert( DeviceAlertInput deviceAlertInput )
	{
		AlertEntity alert = null;

		boolean supportsThresholding = deviceCapabilityService.isCapabilityEnabled( Long.parseLong( deviceAlertInput.getDeviceId() ), "alert", false );
		if ( ( supportsThresholding ) && ( !CommonAppUtils.isNullOrEmptyString( deviceAlertInput.getAlertId() ) ) )
		{
			alert = deviceAlertDAO.findAlert( deviceAlertInput.getDeviceId(), deviceAlertInput.getAlertId() );
			if ( alert == null )
			{
				alert = AlertEntityFactory.createAlertEntity( deviceAlertInput );
				deviceAlertDAO.create( ( DeviceAlertEntity ) alert );

				if ( deviceAlertInput.getCount() > 0 )
				{
					alert.setCount( Long.valueOf( deviceAlertInput.getCount() ) );
				}

				if ( LOG.isDebugEnabled() )
				{
					LOG.debug( "Creating new alert for device {} with source {} and alertId {} ", new Object[] {deviceAlertInput.getDeviceId(), deviceAlertInput.getSourceId(), deviceAlertInput.getAlertId()} );
				}

				Set<Long> territoryInfo = getTerritoryInfoForEvent( deviceAlertInput.getDeviceId() );

				eventRegistry.sendEventAfterTransactionCommits( new AlertCreatedEvent( HealthEventEnum.DEVICE_ALERT.name(), territoryInfo, alert.toDataObject() ) );
			}
			else
			{
				DeviceAlertEntity deviceAlert = ( DeviceAlertEntity ) alert;

				if ( ( deviceAlert.getCount().intValue() != deviceAlertInput.getCount() ) || ( deviceAlert.getDeviceState() != deviceAlertInput.isDeviceState() ) || ( !deviceAlert.getSourceDesc().equals( deviceAlertInput.getValue() ) ) || ( !deviceAlert.getInfo().equals( deviceAlertInput.getInfo() ) ) )
				{
					String newDescription = null;
					String newInfo = null;
					if ( AlertEntityFactory.updateAlertEntity( deviceAlert, deviceAlertInput ) )
					{
						newDescription = deviceAlert.getSourceDesc();
						newInfo = deviceAlert.getInfo();
					}

					deviceAlert.setUserState( AlertUserStateEnum.OPEN );

					if ( LOG.isDebugEnabled() )
					{
						LOG.debug( "Updating alert {} for device {} with source {} and alertId {} ", new Object[] {Long.valueOf( deviceAlert.getId() ), deviceAlertInput.getDeviceId(), deviceAlertInput.getSourceId(), deviceAlertInput.getAlertId()} );
					}

					Set<Long> territoryInfo = getTerritoryInfoForEvent( deviceAlertInput.getDeviceId() );
					AlertChangedEvent event = new AlertUpdatedEvent( territoryInfo, String.valueOf( deviceAlert.getId() ), deviceAlert.getCount().longValue(), deviceAlert.getLastInstanceTime().longValue(), newDescription, newInfo, Integer.valueOf( deviceAlertInput.getThresholdDuration() ), Integer.valueOf( deviceAlertInput.getThresholdFrequency() ), Boolean.valueOf( deviceAlertInput.isDeviceState() ) );

					eventRegistry.sendEventAfterTransactionCommits( event );
				}
			}

			return;
		}

		String sourceId = deviceAlertInput.getSourceId();
		String deviceId = deviceAlertInput.getDeviceId();
		String territoryDeviceId = deviceAlertInput.getDeviceId();
		int alertCount = deviceAlertInput.getCount();
		boolean isRaised = deviceAlertInput.isDeviceState();
		String alertCode = deviceAlertInput.getAlertCode();

		if ( isRaised )
		{
			Set<Long> territoryInfo = getTerritoryInfoForEvent( territoryDeviceId );
			AlertChangedEvent event = null;

			alert = deviceAlertDAO.findUserOpenAlertByIdentifiers( alertCode, deviceId, sourceId );
			if ( alert == null )
			{
				alert = AlertEntityFactory.createAlertEntity( deviceAlertInput );
				deviceAlertDAO.create( ( DeviceAlertEntity ) alert );

				if ( alertCount > 0 )
				{
					alert.setCount( Long.valueOf( alertCount ) );
				}

				event = new AlertCreatedEvent( HealthEventEnum.DEVICE_ALERT.name(), territoryInfo, alert.toDataObject() );
			}
			else
			{
				String newDescription = null;
				String newInfo = null;
				if ( AlertEntityFactory.updateAlertEntity( ( DeviceAlertEntity ) alert, deviceAlertInput ) )
				{
					newDescription = alert.getSourceDesc();
					newInfo = alert.getInfo();
				}
				alert.setDeviceState( true );
				event = new AlertUpdatedEvent( territoryInfo, String.valueOf( alert.getId() ), alert.getCount().longValue(), alert.getLastInstanceTime().longValue(), newDescription, newInfo, Integer.valueOf( deviceAlertInput.getThresholdDuration() ), Integer.valueOf( deviceAlertInput.getThresholdFrequency() ), Boolean.valueOf( true ) );

				eventRegistry.sendEventAfterTransactionCommits( event );
			}
		}
		else
		{
			List<DeviceAlertEntity> deviceAlertsToClear = deviceAlertDAO.findUnresolvedAlertsByIdentifiers( alertCode, deviceId, sourceId );
			for ( DeviceAlertEntity deviceAlertEntity : deviceAlertsToClear )
			{
				deviceAlertEntity.setAlertClear( deviceAlertInput.getResolvedTime() );
				deviceAlertEntity.setDeviceState( false );

				long alertId = deviceAlertEntity.getId();
				Set<Long> territoryInfo = getTerritoryInfoForEvent( territoryDeviceId );

				AlertChangedEvent event = new AlertChangedEvent( HealthEventEnum.DEVICE_ALERT.name(), territoryInfo, EventTypesEnum.HEALTH_CLEARED, String.valueOf( alertId ) );
				eventRegistry.sendEventAfterTransactionCommits( event );
			}
		}
	}

	private void processServerHealthAlert( ServerAlertInput serverAlertInput )
	{
		String sourceId = serverAlertInput.getSourceId();
		boolean isRaised = serverAlertInput.isDeviceState();
		String alertCode = serverAlertInput.getAlertCode();

		if ( isRaised )
		{
			AlertEntity alert = serverAlertDAO.findUserOpenAlertByIdentifiers( alertCode, serverAlertInput.getServerId(), sourceId );
			AlertChangedEvent event = null;
			if ( alert == null )
			{
				alert = AlertEntityFactory.createAlertEntity( serverAlertInput );
				serverAlertDAO.create( ( ServerAlertEntity ) alert );

				event = new AlertCreatedEvent( HealthEventEnum.DEVICE_ALERT.name(), null, alert.toDataObject() );
			}
			else
			{
				String newDescription = null;
				String newInfo = null;
				if ( AlertEntityFactory.updateAlertEntity( alert, serverAlertInput ) )
				{
					newDescription = alert.getSourceDesc();
					newInfo = alert.getInfo();
				}
				event = new AlertUpdatedEvent( null, String.valueOf( alert.getId() ), alert.getCount().longValue(), alert.getLastInstanceTime().longValue(), newDescription, newInfo );
				eventRegistry.sendEventAfterTransactionCommits( event );
			}
		}
		else
		{
			List<ServerAlertEntity> serverAlerts = serverAlertDAO.findUnresolvedAlertsByIdentifiers( alertCode, serverAlertInput.getServerId(), sourceId );
			for ( ServerAlertEntity serverAlertEntity : serverAlerts )
			{
				serverAlertEntity.setAlertClear( serverAlertInput.getResolvedTime() );
				serverAlertEntity.setDeviceState( false );

				AlertChangedEvent event = new AlertChangedEvent( HealthEventEnum.DEVICE_ALERT.name(), null, EventTypesEnum.HEALTH_CLEARED, String.valueOf( serverAlertEntity.getId() ) );
				eventRegistry.sendEventAfterTransactionCommits( event );
			}
		}
	}

	private void scheduleAlertClosureDispatchTask( String deviceId, List<String> closeAlertIds )
	{
		if ( closeAlertIds.isEmpty() )
		{
			LOG.debug( "No closure records to send to device {}. Aborting task scheduling.", deviceId );
			return;
		}

		AlertClosureTask closureTask = new AlertClosureTask( deviceId, closeAlertIds );
		taskScheduler.executeFixedPoolSerial( closureTask, deviceId );
	}

	private void scheduleAlertQueryDispatchTask( String deviceId )
	{
		DeviceResource deviceResource = getTopologyService().getDeviceResourceByDeviceId( deviceId );
		if ( ( deviceResource != null ) && ( deviceResource.isRootDevice() ) )
		{
			AlertQueryTask alertQueryTask = new AlertQueryTask( deviceId );
			taskScheduler.executeFixedPoolSerial( alertQueryTask, deviceId );
		}
	}

	public List<Long> getReferencedDeletedDevices()
	{
		return deviceAlertDAO.findAllDeletedDeviceIds();
	}

	private Set<Long> getTerritoryInfoForEvent( String deviceId )
	{
		return Collections.singleton( getTopologyService().getResourceIdByDeviceId( deviceId ) );
	}

	private List<String> getDevicesUnderUserTerritory( String userName )
	{
		List<String> deviceIds = new ArrayList();
		try
		{
			List<Resource> resources = getTopologyService().getResourcesForUser( userName, ResourceRootType.SYSTEM, new Criteria( DeviceResource.class ), false );
			for ( Resource res : resources )
			{
				DeviceResource deviceResource = ( DeviceResource ) res;

				if ( deviceResource.isRootDevice() )
				{
					deviceIds.add( deviceResource.getDeviceId() );
				}
			}
		}
		catch ( Exception e )
		{
			LOG.warn( "Error when retrieving territory user information for user {} {}", new Object[] {userName, e.getMessage()} );
		}

		return deviceIds;
	}

	private List<String> getUserTerritoryIds( String userName )
	{
		List<String> result = new ArrayList();

		MemberView member = getUserService().getMember( userName );

		for ( Long systemId : member.getAssembledSystemRoots() )
		{
			result.add( systemId.toString() );
		}

		return result;
	}

	protected void auditAlert( AuditEventNameEnum auditEvent, AlertEntity alert )
	{
		if ( CommonAppUtils.getUsernameFromSecurityContext() != null )
		{
			AuditView.Builder builder = new AuditView.Builder( auditEvent.getName() ).addDetailsPair( "alert_code", alert.getAlertCode() ).addDetailsPair( "alert_source", alert.getSourceDesc() );

			if ( ( alert instanceof DeviceAlertEntity ) )
			{
				DeviceAlertEntity deviceAlert = ( DeviceAlertEntity ) alert;

				if ( ( !CommonAppUtils.isNullOrEmptyString( deviceAlert.getSourceId() ) ) && ( !CommonAppUtils.isNullOrEmptyString( deviceAlert.getSourceDesc() ) ) )
				{
					builder.addChannelToAudit( deviceAlert.getDeviceId(), deviceAlert.getSourceId() );
				}
				else
				{
					builder.addRootDeviceToAudit( deviceAlert.getDeviceId(), true );
				}
			}
			eventRegistry.sendEventAfterTransactionCommits( new AuditEvent( builder.build() ) );
		}
	}

	private void auditThresholds( AlertThresholdData[] thresholds )
	{
		AuditView.Builder builder = new AuditView.Builder( AuditEventNameEnum.ALERT_THRESHOLD.getName() );

		for ( AlertThresholdData threshold : thresholds )
		{
			builder.addDetailsPair( threshold.getAlertCode(), CoreJsonSerializer.toJson( threshold ) );
		}

		AuditEvent event = new AuditEvent( builder.build() );
		eventRegistry.sendEventAfterTransactionCommits( event );
	}

	public AlertThresholdData[] getAlertThresholds() throws HealthFault
	{
		AlertConfigEntity alertConfigEntity = alertConfigDAO.getAlertConfig();
		List<AlertThresholdData> thresholds = alertConfigEntity.getConfigDataThresholds();
		return ( AlertThresholdData[] ) thresholds.toArray( new AlertThresholdData[thresholds.size()] );
	}

	public DefaultAlertThresholdData[] getDefaultAlertThresholds() throws HealthFault
	{
		int defualtAlertThresholdsSize = AlertThresholdDefinitionEnum.values().length;
		DefaultAlertThresholdData[] result = new DefaultAlertThresholdData[defualtAlertThresholdsSize];

		int i = 0;
		for ( AlertThresholdDefinitionEnum thresholdDefinition : AlertThresholdDefinitionEnum.values() )
		{
			result[i] = new DefaultAlertThresholdData( thresholdDefinition );
			i++;
		}

		return result;
	}

	public void setAlertThresholds( AlertThresholdData[] alertThresholds ) throws HealthFault
	{
		HashMap<AlertThresholdDefinitionEnum, AlertThresholdData> thresholdMap = new HashMap();

		for ( AlertThresholdData data : getAlertThresholds() )
		{
			thresholdMap.put( AlertThresholdDefinitionEnum.fromPath( data.getAlertCode() ), data );
		}

		for ( AlertThresholdData newData : alertThresholds )
		{
			AlertThresholdDefinitionEnum key = AlertThresholdDefinitionEnum.fromPath( newData.getAlertCode() );

			thresholdMap.put( key, newData );
		}

		AlertConfigEntity targetConfig = alertConfigDAO.getAlertConfig();

		List<AlertThresholdData> setToInsert = new ArrayList( thresholdMap.values() );
		targetConfig.setConfigDataThresholds( setToInsert );

		eventRegistry.sendEventAfterTransactionCommits( new AlertThresholdsUpdatedEvent() );

		List<String> rootDeviceIds = getAllRootDeviceIds();

		DeferredEvent deferredEvent = null;
		for ( String rootDeviceId : rootDeviceIds )
		{

			deferredEvent = new DeferredEvent( new DeviceSetAlertConfigEvent( rootDeviceId ), ConnectState.ONLINE.toString(), 172800000L );
			deferredEventPool.add( rootDeviceId, deferredEvent );
		}

		auditThresholds( alertThresholds );
	}

	public void sendAlertClosuresToDevice( String deviceId, List<String> closedAlertEntries )
	{
		if ( closedAlertEntries == null )
		{
			closedAlertEntries = deviceAlertDAO.findClosedNotReconciledAlertIdsByDeviceId( deviceId );
		}
		if ( closedAlertEntries.isEmpty() )
		{
			return;
		}
		try
		{
			deviceService.closeAlerts( deviceId, closedAlertEntries );
			List<DeviceAlertEntity> alertsToUpdate = deviceAlertDAO.findClosedAlertsByDeviceIds( deviceId, closedAlertEntries );
			if ( !alertsToUpdate.isEmpty() )
			{
				for ( DeviceAlertEntity deviceAlertEntity : alertsToUpdate )
				{
					deviceAlertEntity.setReconciledWithDevice( Boolean.valueOf( true ) );
				}
			}
		}
		catch ( DeviceException e )
		{
			LOG.warn( "Error when trying to send alert closures to device {} . Cause : {}", new Object[] {deviceId, e.getMessage()} );
			if ( e.getDetailedErrorType() != DeviceExceptionTypes.FEATURE_NOT_SUPPORTED )
			{
				DeferredEvent de = new DeferredEvent( new DeviceAlertClosureDispatchEvent( deviceId.toString() ), ConnectState.ONLINE.toString(), 86400000L );
				deferredEventPool.add( deviceId.toString(), de );
			}
		}
	}

	private Pair getPairbyName( Pair[] pairs, String name )
	{
		for ( Pair infoPair : pairs )
		{
			if ( infoPair.getName().equals( name ) )
			{
				return infoPair;
			}
		}
		return null;
	}

	private List<String> getAllRootDeviceIds()
	{
		List<String> result = new ArrayList();

		Criteria crit = new Criteria( DeviceResource.class );
		crit.add( Restrictions.eq( "deviceView.parentDeviceId", null ) );
		List<Resource> resources = topologyService.getResources( crit );
		for ( Resource resource : resources )
		{
			result.add( ( ( DeviceResource ) resource ).getDeviceId() );
		}
		return result;
	}

	private String getAlertCode( Event eventModel )
	{
		String eventName = eventModel.getName();
		if ( eventName.startsWith( DeviceEventsEnum.ALERT.getPath() ) )
		{
			Pair[] infoPairs = eventModel.getInfo();

			Pair result = getPairbyName( infoPairs, "path" );
			if ( result != null )
			{
				return result.getValue();
			}

			LOG.warn( "Unable to find path infoPair in event. {}", eventName );
			return "";
		}

		return eventModel.getName();
	}

	private void setAlertUserStates( List<AlertEntity> alertEntities, AlertUserStateEnum userState ) throws HealthFault
	{
		Map<String, List<String>> deviceAlertEntitiesToClose = new HashMap();
		boolean closedAllAlerts = true;
		for ( AlertEntity alert : alertEntities )
		{
			AlertUserStateEnum old = alert.getUserState();
			if ( old != userState )
			{
				alert.setUserState( userState );
				alert.setLastUserStateChangedTime( System.currentTimeMillis() );

				Set<Long> territoryInfo = null;

				if ( ( alert instanceof DeviceAlertEntity ) )
				{
					DeviceAlertEntity deviceAlert = ( DeviceAlertEntity ) alert;
					territoryInfo = getTerritoryInfoForEvent( deviceAlert.getDeviceId() );

					if ( userState == AlertUserStateEnum.CLOSED )
					{
						if ( deviceAlertEntitiesToClose.containsKey( deviceAlert.getDeviceId() ) )
						{
							( ( List ) deviceAlertEntitiesToClose.get( deviceAlert.getDeviceId() ) ).add( deviceAlert.getDeviceAlertId() );
						}
						else
						{
							List<String> deviceAlertIdList = new ArrayList();
							deviceAlertIdList.add( deviceAlert.getDeviceAlertId() );
							deviceAlertEntitiesToClose.put( deviceAlert.getDeviceId(), deviceAlertIdList );
						}
					}
				}

				AlertChangedEvent event = new AlertChangedEvent( HealthEventEnum.DEVICE_ALERT_USER_STATE_CHANGE.name(), territoryInfo, EventTypesEnum.HEALTH_CLOSED, String.valueOf( alert.getId() ) );

				eventRegistry.sendEventAfterTransactionCommits( event );

				auditAlert( AuditEventNameEnum.ALERT_CLOSE, alert );
			}
			else if ( userState == AlertUserStateEnum.CLOSED )
			{
				LOG.warn( "Alert id=" + alert.getId() + " is already closed." );
				closedAllAlerts = false;
			}
		}

		for ( String deviceIdKey : deviceAlertEntitiesToClose.keySet() )
		{
			scheduleAlertClosureDispatchTask( deviceIdKey, ( List ) deviceAlertEntitiesToClose.get( deviceIdKey ) );
		}

		if ( !closedAllAlerts )
		{
			throw new HealthFault( HealthFaultTypeEnum.ALERT_ALREADY_CLOSED, "Not all alerts could be closed (already closed)" );
		}
	}

	private HealthNotificationSummaryData getDeviceHealthSummaryData( List<DeviceAlertEntity> entityList )
	{
		HealthNotificationSummaryData deviceData = new HealthNotificationSummaryData();
		long firstOccurenceTime = 0L;
		long lastOccurenceTime = 0L;
		for ( AlertEntity alertEntity : entityList )
		{
			AlertData alertData = alertEntity.toDataObject();
			long instanceTime = alertData.getLastInstanceTime();

			if ( ( firstOccurenceTime == 0L ) || ( firstOccurenceTime > instanceTime ) )
			{
				firstOccurenceTime = instanceTime;
			}

			if ( lastOccurenceTime < instanceTime )
			{
				lastOccurenceTime = instanceTime;
			}

			deviceData.incrementTotalIssues();

			String alertCode = alertData.getAlertCode();
			HealthSummaryCategoryEnum mapCategory = HealthSummaryCodeCategoryMapEnum.getCategoryFromPath( alertCode );

			if ( mapCategory == null )
			{
				LOG.warn( "getDeviceHealthSummaryData: cannot find mapCategory for alertCode: {}.", alertCode );
			}
			else
			{
				switch ( mapCategory )
				{
					case DRIVE:
						deviceData.incrementDriveIssues();
						break;
					case UNIT:
						deviceData.incrementUnitIssues();
						break;
					case NETWORK:
						deviceData.incrementNetworkIssue();
						break;
					case VIDEO:
						deviceData.incrementVideoIssue();
						break;
					case POWER:
						deviceData.incrementPowerIssue();
						break;
					case PERIPHERAL:
						deviceData.incrementPeripheralIssue();
						break;

					default:
						LOG.warn( "Caanot find the mapped summary category for alert code: {}", alertCode );
				}
			}
		}
		if ( deviceData.getTotalIssues() == 0 )
		{
			return null;
		}
		deviceData.setFirstOccurence( firstOccurenceTime );
		deviceData.setLastOccurence( lastOccurenceTime );
		return deviceData;
	}

	public HealthNotificationSummaryData[] getAllAlertsByUser( String userName, long startTime, long endTime )
	{
		List<String> deviceIds = getDevicesUnderUserTerritory( userName );
		List<HealthNotificationSummaryData> summaryDataByDevices = new ArrayList();

		List<DeviceAlertEntity> alertsByDevices = deviceAlertDAO.findTimeRestrictedAlertsByDevices( deviceIds, startTime, endTime );
		Map<String, List<DeviceAlertEntity>> alertsMap = new HashMap();
		for ( DeviceAlertEntity entity : alertsByDevices )
		{
			List<DeviceAlertEntity> list = ( List ) alertsMap.get( entity.getDeviceId() );
			if ( list == null )
			{
				list = new ArrayList();
				alertsMap.put( entity.getDeviceId(), list );
			}
			list.add( entity );
		}

		for ( String deviceId : deviceIds )
		{
			List<DeviceAlertEntity> alertsByDevice = ( List ) alertsMap.get( deviceId );
			if ( alertsByDevice.size() > 0 )
			{
				HealthNotificationSummaryData summaryData = getDeviceHealthSummaryData( alertsByDevice );
				summaryData.setDeviceName( getTopologyService().getDeviceResourceByDeviceId( deviceId ).getName() );
				summaryDataByDevices.add( summaryData );
			}
		}

		if ( summaryDataByDevices.size() == 0 )
		{
			return null;
		}
		HealthNotificationSummaryData[] results = ( HealthNotificationSummaryData[] ) summaryDataByDevices.toArray( new HealthNotificationSummaryData[summaryDataByDevices.size()] );
		return results;
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

	public void setDeviceService( DeviceService deviceService )
	{
		this.deviceService = deviceService;
	}

	public void setAlertDAO( AlertDAO alertDAO )
	{
		this.alertDAO = alertDAO;
	}

	public void setEventRegistry( EventRegistry eventRegistry )
	{
		this.eventRegistry = eventRegistry;
	}

	public void setDeviceAlertDAO( DeviceAlertDAO deviceAlertDAO )
	{
		this.deviceAlertDAO = deviceAlertDAO;
	}

	public void setServerAlertDAO( ServerAlertDAO serverAlertDAO )
	{
		this.serverAlertDAO = serverAlertDAO;
	}

	public void setAlertConfigDAO( AlertConfigDAO alertConfigDAO )
	{
		this.alertConfigDAO = alertConfigDAO;
	}

	public void setDeviceCapabilityService( DeviceCapabilityService deviceCapabilityService )
	{
		this.deviceCapabilityService = deviceCapabilityService;
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
