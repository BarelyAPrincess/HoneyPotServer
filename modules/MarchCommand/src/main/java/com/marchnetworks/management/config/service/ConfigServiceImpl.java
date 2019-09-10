package com.marchnetworks.management.config.service;

import com.marchnetworks.audit.data.AuditEventNameEnum;
import com.marchnetworks.audit.data.AuditView;
import com.marchnetworks.audit.data.AuditView.Builder;
import com.marchnetworks.audit.events.AuditEvent;
import com.marchnetworks.command.api.initialization.InitializationListener;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.device.data.ConnectState;
import com.marchnetworks.command.common.device.data.DeviceView;
import com.marchnetworks.command.common.scheduling.TaskScheduler;
import com.marchnetworks.command.common.topology.data.ChannelResource;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.common.configuration.ConfigSettings;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.transport.datamodel.DeviceException;
import com.marchnetworks.management.config.DeviceConfigTask;
import com.marchnetworks.management.config.DeviceImageState;
import com.marchnetworks.management.config.DeviceSnapshotState;
import com.marchnetworks.management.config.DeviceUnassociateImageTask;
import com.marchnetworks.management.config.dao.ConfigSnapshotDAO;
import com.marchnetworks.management.config.dao.DeviceConfigDAO;
import com.marchnetworks.management.config.dao.DeviceImageDAO;
import com.marchnetworks.management.config.events.ConfigNotificationEvent;
import com.marchnetworks.management.config.events.ConfigNotificationReasonCode;
import com.marchnetworks.management.config.events.ConfigNotificationType;
import com.marchnetworks.management.config.events.ConfigurationAddedEvent;
import com.marchnetworks.management.config.events.ConfigurationRemovedEvent;
import com.marchnetworks.management.config.events.ConfigurationUpdatedEvent;
import com.marchnetworks.management.config.events.DeviceConfigAssociationNotification;
import com.marchnetworks.management.config.model.ConfigSnapshot;
import com.marchnetworks.management.config.model.DeviceConfig;
import com.marchnetworks.management.config.model.DeviceImage;
import com.marchnetworks.management.configmerge.service.ConfigurationMergeService;
import com.marchnetworks.management.firmware.data.Firmware;
import com.marchnetworks.management.firmware.service.FirmwareException;
import com.marchnetworks.management.firmware.service.FirmwareService;
import com.marchnetworks.management.instrumentation.DeviceRegistry;
import com.marchnetworks.management.instrumentation.DeviceService;
import com.marchnetworks.management.instrumentation.model.CompositeDevice;
import com.marchnetworks.management.instrumentation.model.DeviceMBean;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;
import com.marchnetworks.server.communications.transport.datamodel.ConfigurationEnvelope;
import com.marchnetworks.server.event.EventRegistry;
import com.marchnetworks.server.network.settings.NetworkBandwidthService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigServiceImpl implements ConfigService, InitializationListener
{
	private static final Logger LOG = LoggerFactory.getLogger( ConfigServiceImpl.class );
	private DeviceImageDAO imageDAO;
	private DeviceConfigDAO devConfigDAO;
	private ConfigSnapshotDAO snapshotDAO;
	private DeviceRegistry deviceRegistry;
	private DeviceService deviceService;
	private ResourceTopologyServiceIF topologyService;
	private ConfigurationMergeService configurationMergeService;
	private EventRegistry eventRegistry;
	private NetworkBandwidthService networkBandwidthService;
	private TaskScheduler taskScheduler;
	private FirmwareService firmwareService;

	public void onAppInitialized()
	{
		List<DeviceConfig> pendingStateConfigs = devConfigDAO.findAllByAssignState( new DeviceImageState[] {DeviceImageState.PENDING, DeviceImageState.PENDING_OFFLINE, DeviceImageState.PENDING_FIRMWARE} );
		if ( ( pendingStateConfigs != null ) && ( !pendingStateConfigs.isEmpty() ) )
		{
			for ( DeviceConfig deviceConfig : pendingStateConfigs )
			{
				deviceConfig.setAssignState( DeviceImageState.FAILED );
			}
		}
	}

	private void sendConfigEvent( String deviceId, DeviceConfig deviceConfig, ConfigNotificationType type, ConfigNotificationReasonCode reason, Boolean afterCommit )
	{
		DeviceConfigAssociationNotification notifyConfigEvent = new DeviceConfigAssociationNotification( deviceId, deviceConfig.getId(), type );

		notifyConfigEvent.setImageState( deviceConfig.getAssignState() );
		notifyConfigEvent.setSnapshotState( deviceConfig.getSnapshotState() );
		notifyConfigEvent.setReasonCode( reason );
		notifyConfigEvent.setFirmwareVersionInfo( deviceConfig.toDataObject().getFirmwareVersion() );

		ConfigNotificationEvent event = new ConfigNotificationEvent( notifyConfigEvent, getTerritoryInfoForEvent( deviceId ) );
		if ( afterCommit.booleanValue() )
		{
			eventRegistry.sendEventAfterTransactionCommits( event );
		}
		else
			eventRegistry.send( event );
	}

	private void sendConfigEvent( String deviceID, ConfigNotificationType type, ConfigNotificationReasonCode reason )
	{
		DeviceConfigAssociationNotification notifyConfigEvent = new DeviceConfigAssociationNotification( deviceID, null, type );

		notifyConfigEvent.setReasonCode( reason );

		ConfigNotificationEvent event = new ConfigNotificationEvent( notifyConfigEvent, getTerritoryInfoForEvent( deviceID ) );
		eventRegistry.send( event );
	}

	private void notifyConfigFailed( String deviceID, DeviceConfig deviceConfig, ConfigNotificationReasonCode reason )
	{
		sendConfigEvent( deviceID, deviceConfig, ConfigNotificationType.CONFIG_APPLIED_FAILED, reason, Boolean.valueOf( true ) );

		deviceConfig.setAssignState( DeviceImageState.FAILED );
	}

	public DeviceImageDescriptor createImage( String devId, String name, String description ) throws ConfigurationException
	{
		DeviceMBean devBean = deviceRegistry.getDevice( devId );

		DeviceConfig devConfig = devConfigDAO.findByDeviceId( devBean.getDeviceId() );
		if ( devConfig == null )
		{
			ConfigurationException ex = new ConfigurationException( ConfigurationExceptionType.INTERNAL_FAILURE, "Fail to find device identifier " + devId );
			throw ex;
		}
		if ( devConfig.getSnapshotState().equals( DeviceSnapshotState.UNKNOWN ) )
		{
			try
			{
				ConfigSnapshot snapshot = new ConfigSnapshot();
				devConfig.setSnapshotState( DeviceSnapshotState.UNKNOWN );

				ConfigurationEnvelope envelop = deviceService.retrieveConfiguration( devBean.getDeviceId() );
				byte[] doc = envelop.getDocument();
				if ( doc != null )
				{
					snapshot.setConfigData( doc );
					devConfig.setSnapshotState( DeviceSnapshotState.KNOWN );

					snapshot.setHash( envelop.getHash() );
					LOG.debug( "Setting device config snapshot" );
				}
				snapshot.setModel( devBean.getModel() );
				snapshot.setFamily( devBean.getFamily() );
				snapshot.setFirmwareVersion( devBean.getSoftwareVersion() );
				snapshot.setSerial( devBean.getSerial() );
				devConfig.setSnapshot( snapshot );
				snapshotDAO.create( snapshot );
			}
			catch ( DeviceException e )
			{
				LOG.warn( "The current device config state is UNKNOWN, could not retreive config data from device." );
				throw new ConfigurationException( ConfigurationExceptionType.UNKNOWN_SNAPSHOT_STATE, "Device snapshot is UNKNOWN state, cannot create image" );
			}
		}

		DeviceImage image = new DeviceImage();
		image.setName( name );
		image.setDescription( description );
		image.setModel( devBean.getModel() );
		image.setFamily( devBean.getFamily() );
		image.setFirmwareVersion( devBean.getSoftwareVersion() );

		ConfigSnapshot copyConfig = null;
		if ( devConfig.getSnapshot() != null )
		{
			copyConfig = devConfig.getSnapshot().copy();
		}
		snapshotDAO.create( copyConfig );
		image.setSnapshot( copyConfig );
		imageDAO.create( image );

		eventRegistry.sendEventAfterTransactionCommits( new ConfigurationAddedEvent( image.getId().toString() ) );

		devConfig.setImage( image );

		ConfigNotificationType notificationType = null;
		if ( devConfig.getDevice().isR5() )
		{
			devConfig.setAssignState( DeviceImageState.APPLIED_NOT_MONITORING );
			devConfig.setSnapshotState( DeviceSnapshotState.NOTASSOCIATED );
			notificationType = ConfigNotificationType.CONFIG_APPLIED_NOT_MONITORING;
		}
		else
		{
			devConfig.setAssignState( DeviceImageState.APPLIED );
			devConfig.setSnapshotState( DeviceSnapshotState.MATCH );
			notificationType = ConfigNotificationType.CONFIG_APPLIED;
		}

		sendConfigEvent( devId, devConfig, notificationType, ConfigNotificationReasonCode.NO_REASON, Boolean.valueOf( true ) );

		auditConfiguration( AuditEventNameEnum.CONFIGURATION_IMPORT, image, devConfig );

		LOG.info( "Create configuration imageid " + image.getId() + " Name:" + image.getName() + " SnapShotId:" + image.getSnapshot().getId() + " hash:" + image.getSnapshot().getHash() + " FamilyID:" + image.getFamily() + " ModelID:" + image.getModel() );
		return image.toDataObject();
	}

	public DeviceConfigDescriptor getDeviceConfig( String deviceConfigId ) throws ConfigurationException
	{
		DeviceConfigDescriptor deviceConfig = null;
		if ( ( deviceConfigId != null ) && ( !deviceConfigId.isEmpty() ) )
		{
			DeviceConfig devConfig = null;
			devConfig = ( DeviceConfig ) devConfigDAO.findById( Long.valueOf( Long.parseLong( deviceConfigId ) ) );
			if ( devConfig == null )
			{
				throw new ConfigurationException( ConfigurationExceptionType.DEVICE_CONFIG_NOT_FOUND, "DeviceConfig not found" );
			}
			deviceConfig = devConfig.toDataObject();
		}
		return deviceConfig;
	}

	public DeviceConfigDescriptor getDeviceConfigByDeviceId( String deviceId ) throws ConfigurationException
	{
		return devConfigDAO.findByDeviceId( deviceId ).toDataObject();
	}

	public DeviceConfigDescriptor[] getAllDeviceConfig() throws ConfigurationException
	{
		List<DeviceConfig> deviceConfigList = devConfigDAO.findAllDetached();
		List<DeviceConfigDescriptor> descriptorList = new ArrayList();

		for ( DeviceConfig deviceConfig : deviceConfigList )
		{
			DeviceConfigDescriptor aDescriptor = deviceConfig.toDataObject();
			descriptorList.add( aDescriptor );
		}

		return ( DeviceConfigDescriptor[] ) descriptorList.toArray( new DeviceConfigDescriptor[descriptorList.size()] );
	}

	public DeviceImageDescriptor[] listImages()
	{
		try
		{
			List<DeviceImage> images = imageDAO.findAll();
			List<DeviceImageDescriptor> imageDescriptors = new ArrayList();
			if ( images != null )
			{
				for ( DeviceImage deviceImage : images )
				{
					imageDescriptors.add( deviceImage.toDataObject() );
				}
			}

			return ( DeviceImageDescriptor[] ) imageDescriptors.toArray( new DeviceImageDescriptor[imageDescriptors.size()] );
		}
		catch ( Exception ex )
		{
			LOG.warn( "" );
		}
		return null;
	}

	public DeviceImageDescriptor getImage( String imageId )
	{
		DeviceImage deviceImageFound = null;
		try
		{
			deviceImageFound = ( DeviceImage ) imageDAO.findById( Long.valueOf( imageId ) );
			return deviceImageFound.toDataObject();
		}
		catch ( Exception e )
		{
			LOG.warn( "Error retrieving DeviceImage instance: " + imageId, e );
		}
		return null;
	}

	public DeviceImageDescriptor editImage( String imageId, String name, String description ) throws ConfigurationException
	{
		try
		{
			DeviceImage deviceImage = ( DeviceImage ) imageDAO.findById( Long.valueOf( imageId ) );
			if ( deviceImage == null )
			{
				LOG.warn( "DeviceImage {} not found on DB", imageId );
				throw new ConfigurationException( ConfigurationExceptionType.DEVICE_IMAGE_NOT_FOUND, "Device image cannot be found" );
			}
			deviceImage.setName( name );
			deviceImage.setDescription( description );

			eventRegistry.sendEventAfterTransactionCommits( new ConfigurationUpdatedEvent( deviceImage.getId().toString() ) );

			return deviceImage.toDataObject();
		}
		catch ( Exception ex )
		{
			LOG.warn( "Error updating DeviceImage:" + imageId );
		}
		return null;
	}

	public DeviceImageDescriptor deleteImage( String imageId ) throws ConfigurationException
	{
		DeviceImage image = ( DeviceImage ) imageDAO.findById( Long.valueOf( imageId ) );
		if ( image == null )
		{
			LOG.warn( "DeviceImage {} not found on DB", imageId );
			ConfigurationException ex = new ConfigurationException( ConfigurationExceptionType.DEVICE_IMAGE_NOT_FOUND, "Device image cannot be found" );

			throw ex;
		}

		List<DeviceConfig> deviceConfigs = devConfigDAO.findAllByImage( image );
		for ( int i = 0; i < deviceConfigs.size(); i++ )
		{
			if ( ( ( ( DeviceConfig ) deviceConfigs.get( i ) ).getAssignState().equals( DeviceImageState.PENDING ) ) || ( ( ( DeviceConfig ) deviceConfigs.get( i ) ).getAssignState().equals( DeviceImageState.PENDING_OFFLINE ) ) )
			{
				LOG.warn( "DeviceImage {} is currently being applied to a device", imageId );
				ConfigurationException ex = new ConfigurationException( ConfigurationExceptionType.DEVICE_IMAGE_ASSIGN, "Device image is currently being applied to a device" );

				throw ex;
			}
		}

		List<DeviceConfig> list = devConfigDAO.findByImage( image );
		if ( list != null )
		{
			for ( DeviceConfig dc : list )
			{
				unassignImage( dc.getDevice().getDeviceId() );
			}
		}

		try
		{
			ConfigSnapshot snapshot = image.getSnapshot();
			if ( snapshot != null )
			{
				snapshotDAO.delete( snapshot );
			}
			image.setSnapshot( null );
			imageDAO.delete( image );

			eventRegistry.sendEventAfterTransactionCommits( new ConfigurationRemovedEvent( image.getId().toString() ) );

			return image.toDataObject();
		}
		catch ( Exception ex )
		{
			LOG.warn( "Error deleting DeviceImage:" + imageId );
		}
		return null;
	}

	public void unassignImage( String devId ) throws ConfigurationException
	{
		DeviceMBean devBean = deviceRegistry.getDevice( devId );

		DeviceConfig deviceConfig = devConfigDAO.findByDeviceId( devBean.getDeviceId() );
		if ( deviceConfig == null )
		{
			ConfigurationException ex = new ConfigurationException( ConfigurationExceptionType.DEVICE_CONFIG_NOT_FOUND, "Device configuration not found" );

			throw ex;
		}
		if ( deviceConfig.getImage() == null )
		{
			ConfigurationException ex = new ConfigurationException( ConfigurationExceptionType.DEVICE_IMAGE_NOT_FOUND, "There is no image assigned to this device, request not executed" );

			throw ex;
		}
		LOG.info( "Unassign the image id {} for device {}", deviceConfig.getImage().getId(), devId );

		deviceConfig.setImage( null );
		deviceConfig.setAssignState( DeviceImageState.UNASSOCIATED );
		deviceConfig.setSnapshotState( DeviceSnapshotState.KNOWN );
		deviceConfig.setFailureRetryCount( Long.valueOf( 0L ) );

		sendConfigEvent( devId, deviceConfig, ConfigNotificationType.CONFIG_IMAGE_UNASSIGNED, ConfigNotificationReasonCode.NO_REASON, Boolean.valueOf( true ) );
	}

	public void updateConfigSnapShotSerial( CompositeDevice dev ) throws ConfigurationException
	{
		DeviceConfig deviceConfig = devConfigDAO.findByDeviceId( dev.getDeviceId() );
		if ( ( deviceConfig == null ) || ( deviceConfig.getSnapshot() == null ) || ( deviceConfig.getSnapshot().getConfigData() == null ) )
		{
			LOG.error( "Device Config snapshot does not exist for device {}", dev.getDeviceId() );
			throw new ConfigurationException( ConfigurationExceptionType.DEVICE_CONFIG_NOT_FOUND, "Device configuration snapshot not found" );
		}
		deviceConfig.getSnapshot().setSerial( dev.getSerial() );
	}

	public void applyReplacement( String devId ) throws ConfigurationException
	{
		DeviceMBean devBean = deviceRegistry.getDevice( devId );
		if ( devBean == null )
		{
			LOG.warn( "Device {} does not exist", devBean );
			throw new ConfigurationException( ConfigurationExceptionType.DEVICE_NOT_FOUND, "Device not found" );
		}

		DeviceConfig deviceConfig = devConfigDAO.findByDeviceId( devId );
		if ( ( deviceConfig == null ) || ( deviceConfig.getSnapshot() == null ) || ( deviceConfig.getSnapshot().getConfigData() == null ) )
		{
			LOG.error( "Device Config snapshot does not exist for device {}", devId );
			throw new ConfigurationException( ConfigurationExceptionType.DEVICE_CONFIG_NOT_FOUND, "Device configuration snapshot not found" );
		}

		byte[] configData = deviceConfig.getSnapshot().getConfigData();
		try
		{
			deviceService.configure( devId, configData, deviceConfig.getSnapshot().getId().toString() );
		}
		catch ( DeviceException de )
		{
			ConfigNotificationReasonCode reason;

			if ( de.isCommunicationError() )
			{
				reason = ConfigNotificationReasonCode.DEVICE_COMM_ERROR;
			}
			else
			{
				reason = ConfigNotificationReasonCode.DEVICE_GENERAL_ERROR;
			}

			sendConfigEvent( devId, deviceConfig, ConfigNotificationType.CONFIG_CHANGED_FAILED, reason, Boolean.valueOf( true ) );
			LOG.warn( "Failed to apply device configuration for device " + devId, de );
		}
	}

	public void applyImage( String devId, String imageId )
	{
		DeviceConfig deviceConfig = devConfigDAO.findByDeviceId( devId );
		if ( !imageId.equalsIgnoreCase( deviceConfig.getImage().getId().toString() ) )
		{
			LOG.warn( "applyImage to device:" + devId + " ImageID from user " + imageId + " is different with imageID from devconfig: " + deviceConfig.getImage().getId() );
			deviceConfig.setImage( ( DeviceImage ) imageDAO.findById( Long.valueOf( imageId ) ) );
		}
		LOG.debug( "applyImage to device:" + devId + " Image from user:" + imageId );

		if ( ( isAssociatedDeviceConfigurationOnProgress( devId ) ) || ( firmwareService.isUpgradeInProgress( devId ) ) )
		{
			LOG.warn( "Device {} has a pending configuration/firmware upgrade in process. Aborting apply images Task", devId );
			notifyConfigFailed( devId, deviceConfig, ConfigNotificationReasonCode.PENDING_REQUEST_IN_PROGRESS );
			return;
		}

		DeviceView deviceView = getTopologyService().getDeviceResourceByDeviceId( devId ).getDeviceView();

		byte[] configData = null;
		try
		{
			if ( ( deviceConfig.getSnapshot() == null ) || ( deviceConfig.getSnapshot().getConfigData() == null ) )
			{
				ConfigurationEnvelope envelop = deviceService.retrieveConfiguration( deviceView.getDeviceId() );

				ConfigSnapshot snapshot = new ConfigSnapshot();
				if ( envelop != null )
				{
					snapshot.setConfigData( envelop.getDocument() );

					snapshot.setHash( envelop.getHash() );
				}
				snapshot.setModel( deviceView.getModel() );
				snapshot.setFamily( deviceView.getFamily() );
				snapshot.setFirmwareVersion( deviceView.getSoftwareVersion() );
				snapshot.setSerial( deviceView.getSerial() );
				snapshotDAO.create( snapshot );
				LOG.debug( "Setting device config snapshot" );
				deviceConfig.setSnapshot( snapshot );
				deviceConfig.setSnapshotState( DeviceSnapshotState.KNOWN );
			}

			if ( deviceView.isR5() )
			{
				configData = deviceConfig.getImage().getSnapshot().getConfigData();
			}
			else
			{
				configData = configurationMergeService.mergeConfig( deviceView.getFamily(), deviceView.getModel(), deviceConfig.getImage().getSnapshot().getConfigData(), deviceConfig.getSnapshot().getConfigData() );
			}
			String snapShotId = deviceConfig.getImage().getSnapshot().getId().toString();

			if ( configData != null )
			{
				LOG.info( "Send configuration snapShotid: " + snapShotId + " hash:" + deviceConfig.getImage().getSnapshot().getHash() + " to device " + devId );
				deviceService.configure( devId, configData, snapShotId );
			}
		}
		catch ( UnsupportedOperationException uoe )
		{
			LOG.warn( "Unsupported merge configuration request, {}", uoe.getMessage() );
			notifyConfigFailed( devId, deviceConfig, ConfigNotificationReasonCode.UNSUPPORTED_DEVICE_TYPE );
		}
		catch ( DeviceException de )
		{
			LOG.warn( "The current device config state is UNKNOWN, could not retreive config data from device." );
			notifyConfigFailed( devId, deviceConfig, ConfigNotificationReasonCode.CONFIG_APPLY_FAIL_GET_DEVICE );
		}
		catch ( Exception ex )
		{
			LOG.warn( "Error merging configuration for device {}: {}", devId, ex.getMessage() );
			notifyConfigFailed( devId, deviceConfig, ConfigNotificationReasonCode.INVALID_CONFIG_XML );
		}
	}

	public boolean isAssociatedDeviceConfigurationOnProgress( String deviceId )
	{
		LOG.debug( "Check for device {} or associated device configuration is on pending or not  ", deviceId );
		DeviceResource devRes = getTopologyService().getDeviceResourceByDeviceId( deviceId );
		String rootDeviceId = null;
		boolean isRootDevice = false;
		if ( devRes.isRootDevice() )
		{
			isRootDevice = true;
			rootDeviceId = deviceId;
		}
		else
		{
			rootDeviceId = devRes.getDeviceView().getParentDeviceId();
			LOG.debug( "Device {}  is a Camera ans parent device is {} ", deviceId, rootDeviceId );
		}
		List<DeviceConfig> pendingStateConfigs = devConfigDAO.findAllByAssignState( new DeviceImageState[] {DeviceImageState.PENDING_OFFLINE, DeviceImageState.PENDING} );
		if ( ( pendingStateConfigs != null ) && ( !pendingStateConfigs.isEmpty() ) )
		{
			for ( DeviceConfig deviceConfig : pendingStateConfigs )
			{
				if ( deviceConfig != null )
				{

					if ( isRootDevice )
					{
						if ( ( !deviceConfig.getDevice().isRootDevice() ) && ( deviceConfig.getDevice().getParentDeviceId().equals( rootDeviceId ) ) )
						{
							LOG.warn( "Root device: " + rootDeviceId + " child camera: " + deviceConfig.getDevice().getDeviceId() + " is pending and return True" );
							return true;
						}

					}
					else if ( ( deviceConfig.getDevice().isRootDevice() ) && ( deviceConfig.getDevice().getDeviceId().equals( rootDeviceId ) ) )
					{
						LOG.warn( "Root device deviceid: " + deviceConfig.getDevice().getDeviceId() + " is pending and return True" );
						return true;
					}
				}
			}
		}
		return false;
	}

	public void applyImages( ConfigView[] configs )
	{
		List<Firmware> firmwarelist = new ArrayList();

		for ( ConfigView configView : configs )
		{
			String deviceId = configView.getDeviceId();
			DeviceConfig deviceConfig = devConfigDAO.findByDeviceId( deviceId );
			if ( deviceConfig == null )
			{
				LOG.warn( "Device {} configuration does not exist.", deviceId );
				notifyConfigFailed( deviceId, deviceConfig, ConfigNotificationReasonCode.REGISTER_FAIL_GET_DEVCONFIG_DATA );
			}
			else if ( ( deviceConfig.getAssignState() == DeviceImageState.PENDING ) || ( deviceConfig.getAssignState() == DeviceImageState.PENDING_OFFLINE ) || ( deviceConfig.getAssignState() == DeviceImageState.PENDING_FIRMWARE ) )
			{

				LOG.warn( "Device {} has a pending configuration/firmware upgrade in process. Aborting apply images Task", configView.getDeviceId() );
				notifyConfigFailed( deviceId, deviceConfig, ConfigNotificationReasonCode.PENDING_REQUEST_IN_PROGRESS );

			}
			else
			{
				DeviceImage image = null;
				if ( configView.getImageId() != null )
				{
					image = ( DeviceImage ) imageDAO.findById( Long.valueOf( configView.getImageId() ) );
					if ( image == null )
					{
						LOG.warn( "Device {} Image {} does not exist.", deviceId, configView.getImageId() );
						notifyConfigFailed( deviceId, deviceConfig, ConfigNotificationReasonCode.DEVICE_IMAGE_NOT_FOUND );
						continue;
					}
					DeviceResource devRes = getTopologyService().getDeviceResourceByDeviceId( deviceId );
					if ( !image.isFirmwareVersionMatch( devRes.getDeviceView().getSoftwareVersion() ) )
					{
						if ( firmwareService.isUpgradeInProgress( deviceId ) )
						{
							LOG.warn( "The device {} is in the middle of performing firmware upgrading.", deviceId );
							notifyConfigFailed( deviceId, deviceConfig, ConfigNotificationReasonCode.PENDING_REQUEST_IN_PROGRESS );
							sendConfigEvent( deviceId, ConfigNotificationType.CONFIG_APPLIED_FAILED, ConfigNotificationReasonCode.PENDING_REQUEST_IN_PROGRESS );
							continue;
						}
						try
						{
							Firmware fw = firmwareService.getDeviceTargetFirmware( deviceId, image.getFirmwareVersion() );
							firmwarelist.add( fw );
							deviceConfig.setAssignState( DeviceImageState.PENDING_FIRMWARE );
							deviceConfig.setImage( image );
							LOG.debug( "The device {} configuration is pending on firmwire upgrade", deviceId );
							sendConfigEvent( deviceId, deviceConfig, ConfigNotificationType.CONFIG_PENDING_FIRMWARE, ConfigNotificationReasonCode.NO_REASON, Boolean.valueOf( true ) );
						}
						catch ( FirmwareException e )
						{
							LOG.warn( "The firmware file required for upgrading device {} cannot be found.", deviceId );
							notifyConfigFailed( deviceId, deviceConfig, ConfigNotificationReasonCode.NO_FIRMWARE_AVAIL );
							continue;
						}
					}
					else
					{
						LOG.info( "User apply configuration ImageID:" + configView.getImageId() + " SanpshotId:" + image.getSnapshot().getId() + " to device " + deviceId + " and Configuration State is Waiting state " );
						deviceConfig.setAssignState( DeviceImageState.WAITING );
						deviceConfig.setImage( image );
						sendConfigEvent( deviceId, deviceConfig, ConfigNotificationType.CONFIG_WAITING, ConfigNotificationReasonCode.NO_REASON, Boolean.valueOf( true ) );
						DeviceResource deviceResource = null;
						if ( devRes.isRootDevice() )
						{
							LOG.debug( "The configuration is for root device {} ", deviceId );
							deviceResource = devRes;
						}
						else
						{
							deviceResource = getTopologyService().getDeviceResourceByDeviceId( devRes.getDeviceView().getParentDeviceId() );
							LOG.debug( "The configuration is for Camera and the parent device is {} ", devRes.getDeviceView().getParentDeviceId() );
						}
						if ( ConnectState.ONLINE == deviceResource.getDeviceView().getConnectState() )
						{
							LOG.debug( "The device {} is online and try to push configuration to the device ", deviceId );
							performApplyImageTask( deviceId, configView.getImageId(), 1 );
						}
						else
						{
							LOG.info( "The device {} is offline, configuration is Waiting for device back to online", deviceId );
						}
					}
				}
				else
				{
					performUnassociateImageTask( deviceId );
				}

				auditConfiguration( AuditEventNameEnum.CONFIGURATION_APPLY, image, deviceConfig );
			}
		}
		if ( firmwarelist.size() > 0 )
		{
			Firmware[] firmwares = ( Firmware[] ) firmwarelist.toArray( new Firmware[firmwarelist.size()] );
			try
			{
				firmwareService.setDeviceFirmwares( firmwares );
			}
			catch ( FirmwareException e )
			{
				LOG.error( "Error appears during upgrading firmware for configuration upgrade." );
			}
		}
	}

	public void performApplyImageTask( String deviceId, String imageId, int secondsDelay )
	{
		LOG.debug( "perform config task for device {}.", deviceId );
		DeviceConfigTask task = new DeviceConfigTask( deviceId, imageId );
		if ( secondsDelay == 0 )
		{
			taskScheduler.executeNow( task );
		}
		else
		{
			taskScheduler.schedule( task, secondsDelay, TimeUnit.SECONDS );
		}
	}

	private void performUnassociateImageTask( String deviceId )
	{
		LOG.debug( "Perform unassociate image for device {}.", deviceId );
		DeviceUnassociateImageTask task = new DeviceUnassociateImageTask( deviceId );
		taskScheduler.executeNow( task );
	}

	public ConfigSettings getConfigurationSettings()
	{
		return networkBandwidthService.getSettings();
	}

	public void setConfigurationSettings( ConfigSettings settings ) throws ConfigurationException
	{
		networkBandwidthService.updateSettings( settings );
	}

	private Set<Long> getTerritoryInfoForEvent( String deviceId )
	{
		return Collections.singleton( getTopologyService().getResourceIdByDeviceId( deviceId ) );
	}

	public DeviceImageState getDeviceImageStatus( String deviceID )
	{
		DeviceConfig deviceConfig = devConfigDAO.findByDeviceId( deviceID );
		return deviceConfig.getAssignState();
	}

	protected void auditConfiguration( AuditEventNameEnum auditEventName, DeviceImage configImage, DeviceConfig deviceConfig )
	{
		if ( CommonAppUtils.getUsernameFromSecurityContext() != null )
		{
			AuditView.Builder auditBuilder = new AuditView.Builder( auditEventName.getName() );

			String configImageName = "not_monitored";
			if ( configImage != null )
			{
				configImageName = configImage.getName();
			}
			auditBuilder.addDetailsPair( "configuration_name", configImageName );

			DeviceResource deviceResource = getTopologyService().getDeviceResourceByDeviceId( deviceConfig.getDevice().getDeviceId() );

			if ( deviceResource.isRootDevice() )
			{
				auditBuilder.addRootDeviceToAudit( deviceResource.getDeviceId(), true );
			}
			else
			{
				List<Resource> childResources = deviceResource.createFilteredResourceList( new Class[] {ChannelResource.class} );
				if ( childResources.size() == 1 )
				{
					ChannelResource channel = ( ChannelResource ) childResources.get( 0 );
					auditBuilder.addChannelToAudit( deviceResource.getDeviceId(), channel.getChannelId() );
				}
				else
				{
					auditBuilder.addChildDeviceToAudit( deviceResource.getDeviceId(), deviceResource.getDeviceView().getParentDeviceId() );
				}
			}

			eventRegistry.sendEventAfterTransactionCommits( new AuditEvent( auditBuilder.build() ) );
		}
	}

	public void setFirmwareService( FirmwareService firmwareService )
	{
		this.firmwareService = firmwareService;
	}

	public void setImageDAO( DeviceImageDAO dao )
	{
		imageDAO = dao;
	}

	public void setDevConfigDAO( DeviceConfigDAO dao )
	{
		devConfigDAO = dao;
	}

	public void setSnapshotDAO( ConfigSnapshotDAO dao )
	{
		snapshotDAO = dao;
	}

	public void setDeviceRegistry( DeviceRegistry a_deviceRegistry )
	{
		deviceRegistry = a_deviceRegistry;
	}

	public void setDeviceService( DeviceService deviceService )
	{
		this.deviceService = deviceService;
	}

	public void setEventRegistry( EventRegistry eventRegistry )
	{
		this.eventRegistry = eventRegistry;
	}

	public ResourceTopologyServiceIF getTopologyService()
	{
		if ( topologyService == null )
		{
			topologyService = ( ( ResourceTopologyServiceIF ) ApplicationContextSupport.getBean( "resourceTopologyServiceProxy_internal" ) );
		}
		return topologyService;
	}

	public void setConfigurationMergeService( ConfigurationMergeService configurationMergeService )
	{
		this.configurationMergeService = configurationMergeService;
	}

	public void setNetworkBandwidthService( NetworkBandwidthService networkBandwidthService )
	{
		this.networkBandwidthService = networkBandwidthService;
	}

	public void setTaskScheduler( TaskScheduler taskScheduler )
	{
		this.taskScheduler = taskScheduler;
	}

	public boolean isDeviceConfigSnapShotExist( String deviceId ) throws ConfigurationException
	{
		DeviceConfig deviceConfig = devConfigDAO.findByDeviceId( deviceId );
		if ( ( deviceConfig == null ) || ( deviceConfig.getSnapshot() == null ) || ( deviceConfig.getSnapshot().getConfigData() == null ) )
		{
			return false;
		}
		return true;
	}
}
