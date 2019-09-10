package com.marchnetworks.management.config;

import com.marchnetworks.command.common.device.data.ChannelState;
import com.marchnetworks.command.common.device.data.ChannelView;
import com.marchnetworks.command.common.device.data.ConnectState;
import com.marchnetworks.command.common.device.data.DeviceView;
import com.marchnetworks.command.common.device.data.RegistrationStatus;
import com.marchnetworks.command.common.topology.data.ChannelResource;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.common.event.EventTypesEnum;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.transport.datamodel.DeviceException;
import com.marchnetworks.common.utils.CommonUtils;
import com.marchnetworks.management.config.dao.ConfigSnapshotDAO;
import com.marchnetworks.management.config.dao.DeviceConfigDAO;
import com.marchnetworks.management.config.events.ConfigNotificationReasonCode;
import com.marchnetworks.management.config.events.ConfigNotificationType;
import com.marchnetworks.management.config.model.ConfigSnapshot;
import com.marchnetworks.management.config.model.DeviceConfig;
import com.marchnetworks.management.config.model.DeviceImage;
import com.marchnetworks.management.config.service.ConfigService;
import com.marchnetworks.management.firmware.event.FirmwareUpgradeEvent;
import com.marchnetworks.management.instrumentation.DeviceRegistry;
import com.marchnetworks.management.instrumentation.DeviceService;
import com.marchnetworks.management.instrumentation.events.AbstractDeviceConfigurationEvent;
import com.marchnetworks.management.instrumentation.events.AbstractDeviceEvent;
import com.marchnetworks.management.instrumentation.events.ChildDeviceRegistrationEvent;
import com.marchnetworks.management.instrumentation.events.DeviceChannelChangedEvent;
import com.marchnetworks.management.instrumentation.events.DeviceConfigRetrieveEvent;
import com.marchnetworks.management.instrumentation.events.DeviceConfigurationEventType;
import com.marchnetworks.management.instrumentation.events.DeviceConnectionStateChangeEvent;
import com.marchnetworks.management.instrumentation.events.DeviceRegistrationEvent;
import com.marchnetworks.management.instrumentation.events.DeviceSystemChangedEvent;
import com.marchnetworks.management.instrumentation.model.DeviceMBean;
import com.marchnetworks.management.instrumentation.pooling.DeferredEvent;
import com.marchnetworks.management.instrumentation.pooling.DeferredEventPool;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;
import com.marchnetworks.server.communications.transport.datamodel.ConfigurationEnvelope;
import com.marchnetworks.server.event.EventListener;
import com.marchnetworks.server.event.EventRegistry;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceConfigRegistrationHandler extends BaseConfigHandler implements EventListener
{
	private DeviceRegistry deviceRegistry;
	private DeviceConfigDAO devConfigDAO;
	private ConfigSnapshotDAO snapshotDAO;
	private DeviceService deviceService;
	private ConfigService configService;
	private ResourceTopologyServiceIF topologyService;
	private DeferredEventPool deferredEventPool;
	private static final Logger LOG = LoggerFactory.getLogger( DeviceConfigRegistrationHandler.class );

	public String getListenerName()
	{
		return DeviceConfigRegistrationHandler.class.getSimpleName();
	}

	public void process( Event event )
	{
		try
		{
			processEvent( event );
		}
		catch ( ProcessDeviceException pde )
		{
			LOG.debug( "Process Device Exception: {}", pde.getMessage() );
		}
	}

	private void processEvent( Event event ) throws ProcessDeviceException
	{
		if ( ( event instanceof AbstractDeviceEvent ) )
		{
			LOG.debug( "Processing event {} ", event.getClass().toString() );
			AbstractDeviceEvent deviceEvent = ( AbstractDeviceEvent ) event;

			if ( ( deviceEvent instanceof DeviceRegistrationEvent ) )
			{
				DeviceRegistrationEvent regEvent = ( DeviceRegistrationEvent ) deviceEvent;
				RegistrationStatus regStatus = regEvent.getRegistrationStatus();
				boolean isMassRegistration = regEvent.isMassRegistration();
				processDeviceRegistrationStatus( regStatus, deviceEvent.getDeviceId(), isMassRegistration );
				return;
			}

			if ( ( deviceEvent instanceof ChildDeviceRegistrationEvent ) )
			{
				RegistrationStatus regStatus = ( ( ChildDeviceRegistrationEvent ) deviceEvent ).getRegistrationStatus();
				processDeviceRegistrationStatus( regStatus, deviceEvent.getDeviceId(), false );
				return;
			}

			Long deviceResourceId = getTopologyService().getResourceIdByDeviceId( deviceEvent.getDeviceId() );
			DeviceResource deviceRes = getTopologyService().getDeviceResource( deviceResourceId );
			if ( deviceRes == null )
			{
				LOG.error( "Did not find device {} from topology service", deviceEvent.getDeviceId() );
				return;
			}

			if ( ( event instanceof FirmwareUpgradeEvent ) )
			{
				FirmwareUpgradeEvent firmwareEvent = ( FirmwareUpgradeEvent ) event;
				LOG.debug( "DU: DeviceConfigRegistrationHandler processing FirmwareUpgradeEvent, device: {}. type: {}", firmwareEvent.getDeviceId(), firmwareEvent.getEventNotificationType() );

				handleFirmwareUpgradeEvent( deviceRes.getDeviceView(), firmwareEvent.getEventNotificationType() );
			}
			else if ( ( deviceEvent instanceof DeviceConfigRetrieveEvent ) )
			{
				handleAsyncRetrieveDeviceConfig( deviceRes.getDeviceView() );
			}
			else if ( ( deviceEvent instanceof AbstractDeviceConfigurationEvent ) )
			{
				AbstractDeviceConfigurationEvent deviceConfigEvent = ( AbstractDeviceConfigurationEvent ) deviceEvent;
				LOG.debug( "device {} configuration event {}.", deviceConfigEvent.getDeviceId(), deviceConfigEvent.getDeviceEventType() );

				if ( deviceConfigEvent.getReason() != null )
				{
					LOG.debug( "device configuration reason {}.", deviceConfigEvent.getReason() );
				}
				if ( deviceConfigEvent.getDeviceEventType().equals( DeviceConfigurationEventType.CONFIG_CHANGED ) )
				{
					handleConfigurationChangedEvent( deviceRes.getDeviceView(), deviceConfigEvent );
				}
				else if ( deviceConfigEvent.getDeviceEventType().equals( DeviceConfigurationEventType.CONFIG_APPLIED_INTERNAL ) )
				{
					handleConfigurationAppliedEvent( deviceRes.getDeviceView(), deviceConfigEvent );
				}
				else if ( ( deviceConfigEvent.getDeviceEventType().equals( DeviceConfigurationEventType.CONFIG_FAILED ) ) || ( deviceConfigEvent.getDeviceEventType().equals( DeviceConfigurationEventType.CONFIG_FAILED_FROM_DEVICE ) ) )
				{
					handleConfigurationFailedEvent( deviceRes.getDeviceView(), deviceConfigEvent.getDeviceEventType(), deviceConfigEvent.getReason() );
				}
				else if ( deviceConfigEvent.getDeviceEventType().equals( DeviceConfigurationEventType.SCHEDULE_CONFIG_APPLY ) )
				{
					handleScheduleConfigApply( deviceRes.getDeviceView() );
				}
				else if ( deviceConfigEvent.getDeviceEventType().equals( DeviceConfigurationEventType.CONFIG_PENDING ) )
				{
					handleConfigPendingEvent( deviceRes.getDeviceView() );
				}
			}
			else if ( ( ( deviceEvent instanceof DeviceSystemChangedEvent ) ) || ( ( deviceEvent instanceof DeviceChannelChangedEvent ) ) )
			{
				handleDeviceChanged( deviceRes.getDeviceView() );
			}
			else if ( ( deviceEvent instanceof DeviceConnectionStateChangeEvent ) )
			{
				DeviceConnectionStateChangeEvent deviceConnectionEvent = ( DeviceConnectionStateChangeEvent ) deviceEvent;
				handleDeviceConnectionEvent( deviceRes.getDeviceView(), deviceConnectionEvent );
			}
			else
			{
				LOG.warn( "{} event type is not supported", event.getEventType().toString() );
			}
		}
	}

	private void processDeviceRegistrationStatus( RegistrationStatus status, String deviceId, boolean isMassRegistration )
	{
		if ( status.equals( RegistrationStatus.REGISTERED ) )
		{
			handleDeviceRegistrationEvent( deviceId, isMassRegistration );
		}
		else if ( status.equals( RegistrationStatus.ERROR_REGISTRATION ) )
		{
			handleDeviceRegistrationError( deviceId );
		}
		else if ( status.equals( RegistrationStatus.UNREGISTERED ) )
		{
			handleDeviceUnregisteredEvent( deviceId );
		}
		else
		{
			LOG.debug( "unhandled  DeviceRegistrationStatus Event {} ", status.toString() );
		}
	}

	private void handleDeviceRegistrationEvent( String deviceId, boolean isMassRegistration )
	{
		LOG.debug( "Processing Device Registered Event" );

		DeviceConfig devConfig = devConfigDAO.findByDeviceId( deviceId );

		if ( devConfig == null )
		{
			DeviceMBean dev = deviceRegistry.getDevice( deviceId );
			if ( dev == null )
			{
				return;
			}
			devConfig = new DeviceConfig( dev );
			devConfig.setSnapshotState( DeviceSnapshotState.UNKNOWN );
			devConfigDAO.create( devConfig );

			if ( !isMassRegistration )
			{
				DeviceConfigRetrieveEvent regEvent = new DeviceConfigRetrieveEvent( dev.getDeviceId(), RegistrationStatus.REGISTERED );
				eventRegistry.sendEventAfterTransactionCommits( regEvent );
			}
		}
	}

	public void handleAsyncRetrieveDeviceConfig( DeviceView dev ) throws ProcessDeviceException
	{
		LOG.debug( "Processing Async Retrieve Config for " + dev.getDeviceId() );
		if ( ( CommonUtils.isReplaceableModel( dev.getFamily(), dev.getModel() ) ) && ( !RegistrationStatus.REGISTERED.equals( dev.getRegistrationStatus() ) ) )
		{
			LOG.warn( "DeviceConfigRegistrationHandler: ignore the async retrieve device config event for device {} ", dev.getDeviceId() );

			return;
		}
		ConfigNotificationType notifyType = null;
		ConfigNotificationReasonCode reason = ConfigNotificationReasonCode.NO_REASON;
		DeviceConfig devConfig = devConfigDAO.findByDeviceId( dev.getDeviceId() );
		if ( ( devConfig != null ) && ( ( devConfig.getSnapshot() == null ) || ( devConfig.getSnapshot().getConfigData() == null ) ) )
		{
			ConfigSnapshot snapshot = new ConfigSnapshot();
			try
			{
				ConfigurationEnvelope envelop = deviceService.retrieveConfiguration( dev.getDeviceId() );

				if ( devConfig.getSnapshot() != null )
				{
					ConfigSnapshot oldSnapshot = devConfig.getSnapshot();
					devConfig.setSnapshot( null );
					snapshotDAO.delete( oldSnapshot );
				}
				byte[] doc = envelop.getDocument();
				if ( doc != null )
				{
					snapshot.setConfigData( doc );

					snapshot.setHash( envelop.getHash() );
					LOG.debug( "Setting device config snapshot" );
				}
				snapshot.setModel( dev.getModel() );
				snapshot.setFamily( dev.getFamily() );
				snapshot.setFirmwareVersion( dev.getSoftwareVersion() );
				snapshot.setSerial( dev.getSerial() );
				snapshotDAO.create( snapshot );
				devConfig.setSnapshotState( DeviceSnapshotState.KNOWN );
				devConfig.setSnapshot( snapshot );

				notifyType = ConfigNotificationType.CONFIG_REGISTERED;

			}
			catch ( DeviceException e )
			{

				LOG.warn( "Fail to retrieve config data for device {} @ {}, continue with device config persistence", dev.getDeviceId(), dev.getRegistrationAddress() );

				LOG.debug( "Fail to retrieve device config data.", e );
				throw new ProcessDeviceException( e );
			}
		}
		else if ( devConfig == null )
		{
			try
			{
				DeviceMBean devBean = deviceRegistry.getDevice( dev.getDeviceId() );
				if ( devBean == null )
				{
					LOG.error( "Did not find deviceid {} from deviceRegistry", dev.getDeviceId() );
					return;
				}
				devConfig = new DeviceConfig( devBean );
				setSnapShotToDevConfig( dev, devConfig );
				devConfigDAO.create( devConfig );
			}
			catch ( DeviceException de )
			{
				LOG.error( "Error retrieving device {} configuration: {}", dev.getDeviceId(), de.getMessage() );
				throw new ProcessDeviceException( de );
			}
		}

		if ( notifyType != null )
		{
			super.sendConfigAssociationNotificationEvent( dev.getDeviceId(), devConfig, notifyType, reason );
		}
	}

	private void handleConfigurationAppliedEvent( DeviceView dev, AbstractDeviceConfigurationEvent event ) throws ProcessDeviceException
	{
		LOG.debug( "Processing Config Snapshot Applied Event" );

		if ( ( CommonUtils.isReplaceableModel( dev.getFamily(), dev.getModel() ) ) && ( dev.getRegistrationStatus() == RegistrationStatus.PENDING_REPLACEMENT ) )
		{
			LOG.debug( "Apply replacement config_apply for device {}  ", dev.getDeviceId() );
			try
			{
				DeviceMBean devBean = deviceRegistry.getDevice( dev.getDeviceId() );
				if ( devBean == null )
				{
					LOG.error( "Did not find deviceid {} from deviceRegistry", dev.getDeviceId() );
					return;
				}
				deviceService.replacementConfigApplied( devBean );
			}
			catch ( DeviceException de )
			{
				LOG.warn( "Fail to call method replacementConfigApplied of deviceService with deviceMBean {} ", dev.toString() );

				throw new ProcessDeviceException( de );
			}
		}

		DeviceConfig devConfig = devConfigDAO.findByDeviceId( dev.getDeviceId() );

		if ( dev.getParentDeviceId() != null )
		{
			ChannelState channelState = getTopologyService().getFirstChannelResourceFromDevice( dev.getDeviceId() ).getChannelView().getChannelState();

			if ( ChannelState.OFFLINE == channelState )
			{
				if ( event.getDeferred() )
				{
					LOG.warn( "Device {} is still offline. Configuration change failed.", dev.getDeviceId() );
					devConfig.setAssignState( DeviceImageState.FAILED );
					devConfig.setFailureRetryCount( Long.valueOf( 0L ) );
					super.sendConfigAppliedNotificationEvent( devConfig.getDevice(), devConfig, ConfigNotificationType.CONFIG_APPLIED_FAILED, ConfigNotificationReasonCode.DEVICE_GENERAL_ERROR );
				}
				else
				{
					LOG.warn( "Device {} is currently offline. Post DeviceDeferredUpgradeEvent in maximum 5 minutes.", dev.getDeviceId() );
					event.setDeferred( true );
					DeferredEvent deferredEvent = new DeferredEvent( event, ChannelState.ONLINE.toString(), 300000L, true );
					deferredEventPool.set( dev.getDeviceId(), deferredEvent );
				}
				return;
			}
		}

		if ( ( devConfig != null ) && ( ( DeviceImageState.PENDING == devConfig.getAssignState() ) || ( DeviceImageState.PENDING_OFFLINE == devConfig.getAssignState() ) ) )
		{
			LOG.debug( "Device {} Current DeviceImageState is {}", dev.getDeviceId(), devConfig.getAssignState() );
			LOG.debug( "Device {} current Hash={}", event.getDeviceId(), event.getConfigHash() );
			LOG.debug( "Device {} Config Hash={}", dev.getDeviceId(), devConfig.getSnapshot().getHash() );

			try
			{
				ConfigurationEnvelope envelop = deviceService.retrieveConfiguration( dev.getDeviceId() );

				ConfigSnapshot delSnapshot = devConfig.getSnapshot();
				if ( delSnapshot != null )
				{
					devConfig.setSnapshot( null );
					snapshotDAO.delete( delSnapshot );
				}

				ConfigNotificationType notifyType = null;

				ConfigSnapshot newSnapshot = new ConfigSnapshot();
				newSnapshot.setConfigData( envelop.getDocument() );
				newSnapshot.setHash( envelop.getHash() );
				newSnapshot.setModel( dev.getModel() );
				newSnapshot.setFamily( dev.getFamily() );
				newSnapshot.setFirmwareVersion( dev.getSoftwareVersion() );
				newSnapshot.setSerial( dev.getSerial() );
				snapshotDAO.create( newSnapshot );
				devConfig.setSnapshot( newSnapshot );
				if ( !devConfig.getAssignState().equals( DeviceImageState.UNASSOCIATED ) )
				{
					if ( devConfig.getDevice().isR5() )
					{
						devConfig.setSnapshotState( DeviceSnapshotState.NOTASSOCIATED );
						devConfig.setAssignState( DeviceImageState.APPLIED_NOT_MONITORING );
						notifyType = ConfigNotificationType.CONFIG_APPLIED_NOT_MONITORING;
					}
					else
					{
						devConfig.setSnapshotState( DeviceSnapshotState.MATCH );
						devConfig.setAssignState( DeviceImageState.APPLIED );
						notifyType = ConfigNotificationType.CONFIG_APPLIED;
					}
				}
				devConfig.setFailureRetryCount( Long.valueOf( 0L ) );
				super.sendConfigAppliedNotificationEvent( devConfig.getDevice(), devConfig, notifyType, ConfigNotificationReasonCode.NO_REASON );
				LOG.info( "Device " + dev.getDeviceId() + " configuration change is completed, the snapshotID is " + newSnapshot.getId() + " and hash is: " + newSnapshot.getHash() );
			}
			catch ( DeviceException de )
			{
				devConfig.setAssignState( DeviceImageState.FAILED );
				devConfig.setFailureRetryCount( Long.valueOf( 0L ) );
				super.sendConfigAppliedNotificationEvent( devConfig.getDevice(), devConfig, ConfigNotificationType.CONFIG_APPLIED_FAILED, ConfigNotificationReasonCode.DEVICE_GENERAL_ERROR );

				String message = "Fail to request configuration from device " + dev.getDeviceId() + " after config apply due to: " + de.getMessage();

				LOG.warn( message );
				throw new ProcessDeviceException( message, de );
			}
		}
	}

	private void handleConfigurationFailedEvent( DeviceView dev, DeviceConfigurationEventType configFailedType, String reason )
	{
		LOG.info( "Processing Config Snapshot failed to apply event for device {}, reason of failure: {}.", dev.getDeviceId(), reason );
		boolean isReplacementFail = false;
		if ( ( CommonUtils.isReplaceableModel( dev.getFamily(), dev.getModel() ) ) && ( dev.getRegistrationStatus() == RegistrationStatus.PENDING_REPLACEMENT ) )
		{
			deviceService.replacementConfigFailed( dev.getDeviceId() );
			isReplacementFail = true;
		}

		DeviceConfig devConfig = devConfigDAO.findByDeviceId( dev.getDeviceId() );

		if ( ( devConfig != null ) && ( ( DeviceImageState.PENDING == devConfig.getAssignState() ) || ( DeviceImageState.PENDING_OFFLINE == devConfig.getAssignState() ) ) )
		{
			long retryCount = devConfig.getFailureRetryCount().longValue();
			LOG.debug( " Device {}  DeviceImageState is {}", devConfig.getDevice().getDeviceId(), devConfig.getAssignState() );
			LOG.debug( " Device {}  RetryCount is {}", devConfig.getDevice().getDeviceId(), Long.valueOf( retryCount ) );

			DeviceResource devRes = getTopologyService().getDeviceResourceByDeviceId( dev.getDeviceId() );

			if ( !isReplacementFail )
			{
				ConnectState connectStateCheck = null;
				if ( devRes.isRootDevice() )
				{
					connectStateCheck = dev.getConnectState();
				}
				else
				{
					connectStateCheck = getTopologyService().getDeviceResourceByDeviceId( devRes.getDeviceView().getParentDeviceId() ).getDeviceView().getConnectState();
				}

				if ( ( ( configFailedType == DeviceConfigurationEventType.CONFIG_FAILED_FROM_DEVICE ) && ( "busy".equalsIgnoreCase( reason ) ) && ( retryCount < 3L ) ) || ( ( retryCount < 3L ) && ( "Communication_Error".equalsIgnoreCase( reason ) ) ) )
				{

					if ( ConnectState.ONLINE == connectStateCheck )
					{
						LOG.info( "Try one more time send configuration to Device {}", devConfig.getDevice().getDeviceId() );
						devConfig.setFailureRetryCount( Long.valueOf( retryCount + 1L ) );
						configService.performApplyImageTask( devConfig.getDevice().getDeviceId(), devConfig.getImage().getId().toString(), 180 );
					}

					LOG.info( "Set configuration DeviceImageState to Waiting for device {}", devConfig.getDevice().getDeviceId() );
					devConfig.setAssignState( DeviceImageState.WAITING );
					super.sendConfigFailedNotificationEvent( devConfig.getDevice(), devConfig, ConfigNotificationType.CONFIG_WAITING, ConfigNotificationReasonCode.NO_REASON );

					return;
				}
			}

			LOG.info( "Device {}  Configuration failed ", devConfig.getDevice().getDeviceId() );
			devConfig.setAssignState( DeviceImageState.FAILED );
			devConfig.setFailureRetryCount( Long.valueOf( 0L ) );
			super.sendConfigFailedNotificationEvent( devConfig.getDevice(), devConfig, ConfigNotificationType.CONFIG_FAILED, ConfigNotificationReasonCode.DEVICE_CONFIG_APPLY_FAILED );
		}
	}

	private void handleConfigPendingEvent( DeviceView dev )
	{
		LOG.info( "Processing Config pending event for device {} .", dev.getDeviceId() );
		DeviceConfig devConfig = devConfigDAO.findByDeviceId( dev.getDeviceId() );
		if ( ( devConfig != null ) && ( DeviceImageState.WAITING == devConfig.getAssignState() ) )
		{
			devConfig.setAssignState( DeviceImageState.PENDING );
			super.sendConfigAppliedNotificationEvent( devConfig.getDevice(), devConfig, ConfigNotificationType.CONFIG_APPLY_PENDING, ConfigNotificationReasonCode.NO_REASON );
		}
		else
		{
			LOG.warn( "Handle Config pending event for device {} but devconfig state is already in {} ", dev.getDeviceId(), devConfig.getAssignState() );
		}
	}

	private void handleDeviceConnectionEvent( DeviceView dev, DeviceConnectionStateChangeEvent event )
	{
		LOG.debug( "Process Device online/offline evevt for device {}", dev.getDeviceId() );
		List<DeviceConfig> pendingOrWaitingStateConfigs = devConfigDAO.findAllByAssignState( new DeviceImageState[] {DeviceImageState.PENDING_OFFLINE, DeviceImageState.WAITING, DeviceImageState.PENDING} );
		if ( ( pendingOrWaitingStateConfigs != null ) && ( !pendingOrWaitingStateConfigs.isEmpty() ) )
		{
			for ( DeviceConfig deviceConfig : pendingOrWaitingStateConfigs )
			{
				String deviceIdFromDeviceConfig = null;
				if ( deviceConfig != null )
				{

					LOG.debug( "deviceConfig deviceid  {}", deviceConfig.getDevice().getDeviceId() );
					LOG.debug( "deviceConfig state  {}", deviceConfig.getAssignState() );
					LOG.debug( "deviceConfig ID  {}", deviceConfig.getId() );

					if ( deviceConfig.getDevice().isRootDevice() )
					{
						deviceIdFromDeviceConfig = deviceConfig.getDevice().getDeviceId();
					}
					else
					{
						deviceIdFromDeviceConfig = deviceConfig.getDevice().getParentDeviceId();
					}

					if ( deviceIdFromDeviceConfig.equals( dev.getDeviceId() ) )
					{
						if ( event.getConnectState().equals( ConnectState.ONLINE ) )
						{
							if ( DeviceImageState.PENDING_OFFLINE == deviceConfig.getAssignState() )
							{
								LOG.info( "Processing online Event to device {} deviceConfig in Pending Offline state", deviceConfig.getDevice().getDeviceId() );
								deviceConfig.setAssignState( DeviceImageState.PENDING );
								super.sendConfigAppliedNotificationEvent( deviceConfig.getDevice(), deviceConfig, ConfigNotificationType.CONFIG_APPLY_PENDING, ConfigNotificationReasonCode.NO_REASON );
							}

							long retryCount = deviceConfig.getFailureRetryCount().longValue();
							if ( ( retryCount < 3L ) && ( DeviceImageState.WAITING == deviceConfig.getAssignState() ) )
							{
								LOG.info( "Processing online Event to device {} configuration in waiting state, try to push configuration one more time", deviceConfig.getDevice().getDeviceId() );
								deviceConfig.setFailureRetryCount( Long.valueOf( retryCount + 1L ) );
								configService.performApplyImageTask( deviceConfig.getDevice().getDeviceId(), deviceConfig.getImage().getId().toString(), 0 );
							}
						}
						else if ( DeviceImageState.PENDING == deviceConfig.getAssignState() )
						{
							LOG.info( "Processing offline Event to device {} Configuration in Pending state", deviceConfig.getDevice().getDeviceId() );
							deviceConfig.setAssignState( DeviceImageState.PENDING_OFFLINE );
							super.sendConfigAppliedNotificationEvent( deviceConfig.getDevice(), deviceConfig, ConfigNotificationType.CONFIG_PENDING_OFFLINE, ConfigNotificationReasonCode.NO_REASON );
						}
					}
				}
			}
		}
	}

	private void handleConfigurationChangedEvent( DeviceView dev, AbstractDeviceConfigurationEvent event ) throws ProcessDeviceException
	{
		LOG.debug( "Processing Config Snapshot Changed Event" );
		if ( ( CommonUtils.isReplaceableModel( dev.getFamily(), dev.getModel() ) ) && ( !RegistrationStatus.REGISTERED.equals( dev.getRegistrationStatus() ) ) )
		{
			LOG.warn( "DeviceConfigRegistrationHandler: ignore the Configuration Changed Event for device {} ", dev.getDeviceId() );

			return;
		}

		ConfigNotificationType notifyType = null;
		ConfigNotificationReasonCode reason = ConfigNotificationReasonCode.NO_REASON;
		DeviceConfig devConfig = devConfigDAO.findByDeviceId( dev.getDeviceId() );
		if ( devConfig != null )
		{
			if ( ( devConfig.getDevice().isR5() ) && ( ( DeviceImageState.PENDING == devConfig.getAssignState() ) || ( DeviceImageState.PENDING_OFFLINE == devConfig.getAssignState() ) ) )
			{
				LOG.warn( "Ignore the Configuration Changed Event for device {}  since it is in pending state", dev.getDeviceId() );
				return;
			}

			try
			{
				LOG.debug( "Device ID = {}", dev.getDeviceId() );
				if ( devConfig.getSnapshot() != null )
				{
					LOG.debug( "DeviceConfig Hash={}", devConfig.getSnapshot().getHash() );
					LOG.debug( "Device Hash={}", event.getConfigHash() );
					if ( ( devConfig.getImage() != null ) && ( devConfig.getImage().getSnapshot().getHash().equals( event.getConfigHash() ) ) )
					{
						ConfigurationEnvelope envelop = deviceService.retrieveConfiguration( dev.getDeviceId() );
						byte[] doc = envelop.getDocument();
						ConfigSnapshot snapshot = new ConfigSnapshot();
						snapshot.setConfigData( doc );
						if ( ( !devConfig.getAssignState().equals( DeviceImageState.UNASSOCIATED ) ) && ( !devConfig.getAssignState().equals( DeviceImageState.PENDING ) ) && ( !devConfig.getDevice().isR5() ) )
						{

							devConfig.setSnapshotState( DeviceSnapshotState.MATCH );

							notifyType = ConfigNotificationType.CONFIG_CHANGED;
						}

						snapshot.setHash( envelop.getHash() );
						LOG.debug( "Setting device config snapshot Hash={}", snapshot.getHash() );

						ConfigSnapshot oldSnapshot = devConfig.getSnapshot();
						devConfig.setSnapshot( null );
						snapshotDAO.delete( oldSnapshot );
						snapshot.setModel( dev.getModel() );
						snapshot.setFamily( dev.getFamily() );
						snapshot.setFirmwareVersion( dev.getSoftwareVersion() );
						snapshot.setSerial( dev.getSerial() );
						devConfig.setSnapshot( snapshot );
						snapshotDAO.create( snapshot );
						LOG.info( "Update device " + devConfig.getDevice().getDeviceId() + " configsnapshotID " + snapshot.getId() + " with Hash: " + snapshot.getHash() );
					}
					else if ( ( devConfig.getSnapshot().getHash() == null ) || ( ( devConfig.getSnapshot().getHash() != null ) && ( !devConfig.getSnapshot().getHash().equals( event.getConfigHash() ) ) ) )
					{
						ConfigurationEnvelope envelop = deviceService.retrieveConfiguration( dev.getDeviceId() );

						if ( ( devConfig.getSnapshot().getHash() == null ) || ( ( devConfig.getSnapshot().getHash() != null ) && ( !devConfig.getSnapshot().getHash().equals( envelop.getHash() ) ) ) )
						{
							byte[] doc = envelop.getDocument();
							ConfigSnapshot snapshot = new ConfigSnapshot();
							snapshot.setConfigData( doc );
							if ( ( !devConfig.getAssignState().equals( DeviceImageState.UNASSOCIATED ) ) && ( !devConfig.getAssignState().equals( DeviceImageState.PENDING ) ) && ( !devConfig.getDevice().isR5() ) )
							{

								devConfig.setSnapshotState( DeviceSnapshotState.MISMATCH );

								notifyType = ConfigNotificationType.CONFIG_CHANGED;
							}

							snapshot.setHash( envelop.getHash() );
							LOG.debug( "Setting device config snapshot Hash={}", snapshot.getHash() );

							ConfigSnapshot oldSnapshot = devConfig.getSnapshot();
							devConfig.setSnapshot( null );
							snapshotDAO.delete( oldSnapshot );
							snapshot.setModel( dev.getModel() );

							snapshot.setFamily( dev.getFamily() );
							snapshot.setFirmwareVersion( dev.getSoftwareVersion() );
							snapshot.setSerial( dev.getSerial() );
							devConfig.setSnapshot( snapshot );
							snapshotDAO.create( snapshot );
							LOG.info( "Update device " + devConfig.getDevice().getDeviceId() + " configsnapshotID " + snapshot.getId() + " with Hash: " + snapshot.getHash() );
						}
					}
				}
				else
				{
					setSnapShotToDevConfig( dev, devConfig );

					notifyType = ConfigNotificationType.CONFIG_REGISTERED;
				}
			}
			catch ( DeviceException de )
			{
				String message = "Error retrieving device " + dev.getDeviceId() + " configuration: " + de.getMessage();
				LOG.warn( message );
				throw new ProcessDeviceException( message, de );
			}

			if ( notifyType != null )
			{
				super.sendConfigAssociationNotificationEvent( dev.getDeviceId(), devConfig, notifyType, reason );
			}
		}
		else
		{
			LOG.debug( "There is no DeviceConfig associated with DeviceMBean id={}", dev.getDeviceId() );
			try
			{
				DeviceMBean devBean = deviceRegistry.getDevice( dev.getDeviceId() );
				if ( devBean == null )
				{
					LOG.error( "Did not find deviceid {} from deviceRegistry", dev.getDeviceId() );
					return;
				}
				devConfig = new DeviceConfig( devBean );
				setSnapShotToDevConfig( dev, devConfig );
				devConfigDAO.create( devConfig );
			}
			catch ( DeviceException de )
			{
				String message = "Error retrieving device " + dev.getDeviceId() + " configuration: " + de.getMessage();
				LOG.warn( message );
				throw new ProcessDeviceException( message, de );
			}
		}
	}

	private void setSnapShotToDevConfig( DeviceView dev, DeviceConfig devConfig ) throws DeviceException
	{
		ConfigurationEnvelope envelop = deviceService.retrieveConfiguration( dev.getDeviceId() );
		ConfigSnapshot snapshot = new ConfigSnapshot();
		byte[] doc = envelop.getDocument();
		if ( doc != null )
		{
			snapshot.setConfigData( doc );

			snapshot.setHash( envelop.getHash() );
			LOG.debug( "Setting device config snapshot" );
		}
		snapshot.setModel( dev.getModel() );
		snapshot.setFamily( dev.getFamily() );
		snapshot.setFirmwareVersion( dev.getSoftwareVersion() );
		snapshot.setSerial( dev.getSerial() );
		snapshotDAO.create( snapshot );
		devConfig.setSnapshotState( DeviceSnapshotState.KNOWN );
		devConfig.setSnapshot( snapshot );
	}

	private void handleDeviceUnregisteredEvent( String deviceId )
	{
		LOG.debug( "Processing device removed event" );

		DeviceConfig devConfig = devConfigDAO.findByDeviceId( deviceId );
		unassociateDeviceConfiguration( devConfig );
	}

	private void unassociateDeviceConfiguration( DeviceConfig devConfig )
	{
		if ( devConfig != null )
		{
			if ( devConfig.getImage() != null )
			{

				devConfig.setImage( null );
			}
			if ( devConfig.getSnapshot() != null )
			{
				snapshotDAO.delete( devConfig.getSnapshot() );
			}
			devConfig.setSnapshot( null );
			devConfig.setDevice( null );
			devConfigDAO.delete( devConfig );
		}
	}

	private void handleScheduleConfigApply( DeviceView device )
	{
		DeviceConfig devConfig = devConfigDAO.findByDeviceId( device.getDeviceId() );
		if ( devConfig == null )
		{
			LOG.debug( "Configuration for device {} could not be found. Aborting config apply schedule", device.getDeviceId() );

			return;
		}
		configService.performApplyImageTask( device.getDeviceId(), devConfig.getImage().getId().toString(), 0 );
	}

	private void handleDeviceRegistrationError( String deviceId )
	{
		LOG.debug( "Error registering device {}", deviceId );
		DeviceConfig devConfig = devConfigDAO.findByDeviceId( deviceId );
		if ( devConfig != null )
		{
			super.sendConfigAssociationNotificationEvent( deviceId, devConfig, ConfigNotificationType.CONFIG_REGISTERED_FAILED, ConfigNotificationReasonCode.REGISTRATION_FAILURE );
		}
	}

	private void handleFirmwareUpgradeEvent( DeviceView device, String type )
	{
		LOG.debug( "received Firmware Upgrade Event {} for device {} ", type, device.getDeviceId() );
		DeviceConfig devConfig = devConfigDAO.findByDeviceId( device.getDeviceId() );
		if ( devConfig == null )
		{
			LOG.info( "Configuration for device {} could not be found. Aborting config apply schedule", device.getDeviceId() );

			return;
		}
		if ( devConfig.getAssignState() == DeviceImageState.PENDING_FIRMWARE )
		{
			if ( type.equals( EventTypesEnum.FIRMWARE_UPGRADE_COMPLETED.getFullPathEventName() ) )
			{
				devConfig.setAssignState( DeviceImageState.WAITING );
				configService.performApplyImageTask( device.getDeviceId(), devConfig.getImage().getId().toString(), 60 );
				super.sendConfigAssociationNotificationEvent( device.getDeviceId(), devConfig, ConfigNotificationType.CONFIG_WAITING, ConfigNotificationReasonCode.NO_REASON );
			}
			else if ( type.equals( EventTypesEnum.FIRMWARE_UPGRADE_FAILED.getFullPathEventName() ) )
			{
				devConfig.setAssignState( DeviceImageState.FAILED );
				super.sendConfigAssociationNotificationEvent( device.getDeviceId(), devConfig, ConfigNotificationType.CONFIG_FAILED, ConfigNotificationReasonCode.NO_REASON );
			}
		}

		if ( ( type.equals( EventTypesEnum.FIRMWARE_UPGRADE_COMPLETED.getFullPathEventName() ) ) && ( CommonUtils.isReplaceableModel( device.getFamily(), device.getModel() ) ) && ( RegistrationStatus.PENDING_REPLACEMENT == device.getRegistrationStatus() ) )
		{

			LOG.info( " Firmware upgrade completed, resume replacment process for device {} ", device.getDeviceId() );
			DeviceMBean devBean = deviceRegistry.getDevice( device.getDeviceId() );
			if ( devBean == null )
			{
				LOG.error( "Did not find deviceid {} from deviceRegistry", device.getDeviceId() );
				return;
			}
			deviceService.resumeReplacement( devBean );
		}
		else if ( ( type.equals( EventTypesEnum.FIRMWARE_UPGRADE_FAILED.getFullPathEventName() ) ) && ( CommonUtils.isReplaceableModel( device.getFamily(), device.getModel() ) ) && ( RegistrationStatus.PENDING_REPLACEMENT == device.getRegistrationStatus() ) )
		{

			LOG.info( " Firmware upgrade failed, handle replacment firmeare chceck failure for device {} ", device.getDeviceId() );

			DeviceMBean devBean = deviceRegistry.getDevice( device.getDeviceId() );
			if ( devBean == null )
			{
				LOG.error( "Did not find deviceid {} from deviceRegistry", device.getDeviceId() );
				return;
			}
			deviceService.handleReplaceFirmwareCheckFailure( devBean );
		}
	}

	private void handleDeviceChanged( DeviceView dev )
	{
		LOG.debug( "handleDeviceChanged Device Changed event for device {}", dev.getDeviceId() );
		if ( ( CommonUtils.isReplaceableModel( dev.getFamily(), dev.getModel() ) ) && ( !RegistrationStatus.REGISTERED.equals( dev.getRegistrationStatus() ) ) )
		{
			LOG.warn( "DeviceConfigRegistrationHandler : ignore the Device Changed event for device {} ", dev.getDeviceId() );

			return;
		}
		DeviceConfig devConfig = devConfigDAO.findByDeviceId( dev.getDeviceId() );
		if ( devConfig != null )
		{

			ConfigSnapshot snapshot = devConfig.getSnapshot();
			if ( snapshot != null )
			{
				if ( !snapshot.getFirmwareVersion().equals( dev.getSoftwareVersion() ) )
				{
					LOG.debug( "Updating firmware version from {} to {}", snapshot.getFirmwareVersion(), dev.getSoftwareVersion() );

					snapshot.setFirmwareVersion( dev.getSoftwareVersion() );
					if ( ( !devConfig.getSnapshotState().equals( DeviceSnapshotState.PENDING ) ) && ( !devConfig.getSnapshotState().equals( DeviceSnapshotState.INPROGRESS ) ) && ( !devConfig.getDevice().isR5() ) )
					{

						devConfig.setSnapshotState( DeviceSnapshotState.MISMATCH );
					}
					super.sendConfigAssociationNotificationEvent( dev.getDeviceId(), devConfig, ConfigNotificationType.SNAPSHOT_CHANGED, ConfigNotificationReasonCode.FIRMWARE_CHANGED );
				}
				else
				{
					LOG.debug( "No change in firmware version, image={}, device={}", snapshot.getFirmwareVersion(), dev.getSoftwareVersion() );
				}

			}
			else
			{
				LOG.debug( "No DeviceImage associated, ignoring DEVICE_CHANGED event" );
			}
		}
	}

	public void setDeviceRegistry( DeviceRegistry a_deviceRegistry )
	{
		LOG.debug( "Setting DeviceRegistry property" );
		deviceRegistry = a_deviceRegistry;
	}

	public void setDevConfigDAO( DeviceConfigDAO dao )
	{
		devConfigDAO = dao;
	}

	public void setSnapshotDAO( ConfigSnapshotDAO dao )
	{
		snapshotDAO = dao;
	}

	public void setDeviceService( DeviceService service )
	{
		deviceService = service;
	}

	public void setConfigService( ConfigService configService )
	{
		this.configService = configService;
	}

	public void setDeferredEventPool( DeferredEventPool deferredEventPool )
	{
		this.deferredEventPool = deferredEventPool;
	}

	public ResourceTopologyServiceIF getTopologyService()
	{
		if ( topologyService == null )
		{
			topologyService = ( ( ResourceTopologyServiceIF ) ApplicationContextSupport.getBean( "resourceTopologyServiceProxy_internal" ) );
		}
		return topologyService;
	}
}
