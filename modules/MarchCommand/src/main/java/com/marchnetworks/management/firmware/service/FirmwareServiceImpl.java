package com.marchnetworks.management.firmware.service;

import com.google.gson.reflect.TypeToken;
import com.marchnetworks.audit.data.AuditEventNameEnum;
import com.marchnetworks.audit.data.AuditView;
import com.marchnetworks.audit.data.AuditView.Builder;
import com.marchnetworks.audit.events.AuditEvent;
import com.marchnetworks.command.api.initialization.InitializationListener;
import com.marchnetworks.command.api.rest.DeviceManagementConstants;
import com.marchnetworks.command.api.schedule.ScheduleConsumerService;
import com.marchnetworks.command.api.schedule.ScheduleException;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.device.DeviceEventsEnum;
import com.marchnetworks.command.common.device.data.ConnectState;
import com.marchnetworks.command.common.device.data.DeviceView;
import com.marchnetworks.command.common.device.data.RegistrationStatus;
import com.marchnetworks.command.common.scheduling.TaskScheduler;
import com.marchnetworks.command.common.topology.data.ChannelResource;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.command.common.transport.data.GenericValue;
import com.marchnetworks.common.config.ConfigProperty;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.common.event.EventTypesEnum;
import com.marchnetworks.common.serialization.CoreJsonSerializer;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.transport.datamodel.DeviceException;
import com.marchnetworks.common.utils.CommonUtils;
import com.marchnetworks.management.config.DeviceImageState;
import com.marchnetworks.management.config.service.ConfigService;
import com.marchnetworks.management.config.service.ConfigurationException;
import com.marchnetworks.management.config.service.DeviceConfigDescriptor;
import com.marchnetworks.management.data.ChannelDeviceModel;
import com.marchnetworks.management.data.FileObject;
import com.marchnetworks.management.data.FileStorageView;
import com.marchnetworks.management.data.FirmwareFileTypeEnum;
import com.marchnetworks.management.data.UpdFileInfo;
import com.marchnetworks.management.data.UpgFileInfo;
import com.marchnetworks.management.file.model.FileStorageMBean;
import com.marchnetworks.management.file.service.FileStorageException;
import com.marchnetworks.management.file.service.FileStorageExceptionType;
import com.marchnetworks.management.file.service.FileStorageService;
import com.marchnetworks.management.firmware.dao.FirmwareDAO;
import com.marchnetworks.management.firmware.dao.GroupFirmwareDAO;
import com.marchnetworks.management.firmware.data.ChannelGroupFirmware;
import com.marchnetworks.management.firmware.data.Firmware;
import com.marchnetworks.management.firmware.data.FirmwareGroupEnum;
import com.marchnetworks.management.firmware.data.GroupFirmware;
import com.marchnetworks.management.firmware.data.UpdateStateEnum;
import com.marchnetworks.management.firmware.data.UpdateTypeEnum;
import com.marchnetworks.management.firmware.data.UpgradeTaskInfo;
import com.marchnetworks.management.firmware.event.FirmwareUpgradeEvent;
import com.marchnetworks.management.firmware.event.GroupFirmwareUpgradeEvent;
import com.marchnetworks.management.firmware.model.FirmwareEntity;
import com.marchnetworks.management.firmware.model.GroupFirmwareEntity;
import com.marchnetworks.management.firmware.task.ChannelFirmwareUpgradeTask;
import com.marchnetworks.management.firmware.task.FirmwareUpgradeTask;
import com.marchnetworks.management.instrumentation.DeviceCapabilityService;
import com.marchnetworks.management.instrumentation.DeviceService;
import com.marchnetworks.management.instrumentation.events.AbstractDeviceConfigurationEvent;
import com.marchnetworks.management.instrumentation.events.DeviceConfigurationEventType;
import com.marchnetworks.management.instrumentation.events.DeviceConnectionStateChangeEvent;
import com.marchnetworks.management.instrumentation.events.DeviceDeferredUpgradeEvent;
import com.marchnetworks.management.instrumentation.events.DeviceRegistrationEvent;
import com.marchnetworks.management.instrumentation.pooling.DeferredEvent;
import com.marchnetworks.management.instrumentation.pooling.DeferredEventPool;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;
import com.marchnetworks.schedule.events.ScheduleEvent;
import com.marchnetworks.schedule.events.ScheduleEventType;
import com.marchnetworks.schedule.service.ScheduleService;
import com.marchnetworks.server.event.EventRegistry;
import com.marchnetworks.shared.config.CommonConfiguration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FirmwareServiceImpl implements FirmwareService, InitializationListener, ScheduleConsumerService
{
	private static final Logger LOG = LoggerFactory.getLogger( FirmwareServiceImpl.class );

	private DeviceService deviceService;
	private FileStorageService fileStorageService;
	private ResourceTopologyServiceIF topologyService;
	private EventRegistry eventRegistry;
	private ConfigService configService;
	private FirmwareDAO firmwareDAO;
	private GroupFirmwareDAO groupFirmwareDAO;
	private TaskScheduler taskScheduler;
	private ScheduleService scheduleService;
	private DeviceCapabilityService deviceCapabilityService;
	private DeferredEventPool deferredEventPool;
	private CommonConfiguration commonConfig;
	private List<UpdFileInfo> currentUpdateFiles = new ArrayList();
	private List<UpgradeTaskInfo> currentUpgradeTasks = new ArrayList();

	private long fixedDeviceUpgradeTimeout = 0L;
	private long mobileDeviceUpgradeTimeout = 0L;

	public void onAppInitialized()
	{
		mobileDeviceUpgradeTimeout = ( commonConfig.getIntProperty( ConfigProperty.FIRMWARE_UPGRADE_MOBILE_TIMEOUT, 172800 ) * 1000 );
		fixedDeviceUpgradeTimeout = ( commonConfig.getIntProperty( ConfigProperty.FIRMWARE_UPGRADE_TIMEOUT, 900 ) * 1000 );
		LOG.info( "Upgrade timeout setting for Fixed device: {}, Mobile device: {}.", Long.valueOf( fixedDeviceUpgradeTimeout ), Long.valueOf( mobileDeviceUpgradeTimeout ) );

		createFirmwareGroups();

		checkForFirmwareUpdates();
	}

	public void setDeviceFirmwares( Firmware[] firmwares ) throws FirmwareException
	{
		for ( Firmware firmware : firmwares )
		{
			LOG.info( "set Device Firmware. device ID: {}, firmware ID: {}.", firmware.getDeviceId(), firmware.getFirmwareId() );
			DeviceResource deviceResource = getTopologyService().getDeviceResourceByDeviceId( firmware.getDeviceId() );
			if ( deviceResource == null )
			{
				String msg = "Cannot set firmware - Device {} does not exist." + firmware.getDeviceId();
				LOG.warn( msg );
				throw new FirmwareException( FirmwareExceptionTypeEnum.DEVICE_NOT_FOUND, msg );
			}

			if ( !deviceResource.isRootDevice() )
			{
				String parentDeviceId = deviceResource.getDeviceView().getParentDeviceId();
				if ( getTopologyService().getDeviceResourceByDeviceId( parentDeviceId ).getDeviceView().isR5() )
				{
					boolean supportCameraUpgrade = deviceCapabilityService.isCapabilityEnabled( Long.parseLong( parentDeviceId ), "upgradeChannel.2" );
					if ( !supportCameraUpgrade )
					{
						String msg = "The recorder doesn't support IP camera FW upgrade. Camera {} upgrade is aborted." + firmware.getDeviceId();
						LOG.warn( msg );
						throw new FirmwareException( FirmwareExceptionTypeEnum.CAMERA_UPDATE_NOT_SUPPORTED, msg );
					}
				}
			}

			FileStorageView firmwareStorage = null;

			if ( !CommonAppUtils.isNullOrEmptyString( firmware.getFirmwareId() ) )
			{
				try
				{
					firmwareStorage = fileStorageService.getFileStorage( String.valueOf( firmware.getFirmwareId() ) );
					if ( firmwareStorage == null )
					{
						String msg = "Cannot set firmware - the specified firmware " + firmware.getFirmwareId() + " doesn't exist.";
						LOG.warn( msg );
						throw new FirmwareException( FirmwareExceptionTypeEnum.FIRMWARE_FILE_NOT_FOUND, msg );
					}
				}
				catch ( FileStorageException e )
				{
					String msg = "Cannot set firmware - the specified firmware " + firmware.getFirmwareId() + " doesn't exist.";
					LOG.warn( msg );
					throw new FirmwareException( FirmwareExceptionTypeEnum.FIRMWARE_FILE_NOT_FOUND, msg );
				}

				if ( firmwareStorage.getProperty( "FIRMWARE_CCMDEVICEMODELS" ) != null )
				{
					if ( !deviceResource.getDeviceView().getManufacturer().equalsIgnoreCase( firmwareStorage.getProperty( "FIRMWARE_MANUFACTURERID" ) ) )
					{
						String msg = "Cannot set firmware - device " + firmware.getDeviceId() + " manufacturer doesn't match.";
						LOG.warn( msg );
						throw new FirmwareException( FirmwareExceptionTypeEnum.DEVICE_FIRMWARE_NOT_MATCH, msg );
					}

					if ( !checkCCMCameraModel( deviceResource.getDeviceView(), firmwareStorage ) )
					{
						String msg = "Cannot set firmware - device " + firmware.getDeviceId() + " model doesn't match.";
						LOG.warn( msg );
						throw new FirmwareException( FirmwareExceptionTypeEnum.DEVICE_FIRMWARE_NOT_MATCH, msg );
					}
				}
				else
				{
					if ( !deviceResource.getDeviceView().getFamily().equalsIgnoreCase( firmwareStorage.getProperty( "FIRMWARE_MODEL" ) ) )
					{
						String msg = "Cannot set firmware - device " + firmware.getDeviceId() + " family doesn't match.";
						LOG.warn( msg );
						throw new FirmwareException( FirmwareExceptionTypeEnum.DEVICE_FIRMWARE_NOT_MATCH, msg );
					}

					if ( ( firmwareStorage.getProperty( "FIRMWARE_TYPE" ) != null ) && ( CommonUtils.checkDeviceModel( deviceResource.getDeviceView().getModel(), firmwareStorage.getProperty( "FIRMWARE_TYPE" ) ) != 1 ) )
					{
						String msg = "Cannot set firmware - device " + firmware.getDeviceId() + " model doesn't match.";
						LOG.warn( msg );
						throw new FirmwareException( FirmwareExceptionTypeEnum.DEVICE_FIRMWARE_NOT_MATCH, msg );
					}
				}
			}

			Long scheduleId = firmware.getSchedulerId();
			if ( ( scheduleId != null ) && ( scheduleId.longValue() != 0L ) )
			{
				try
				{
					if ( scheduleService.getById( scheduleId ) == null )
					{
						String msg = "Cannot set the specified schedule with ID: " + scheduleId;
						LOG.warn( msg );
						throw new FirmwareException( FirmwareExceptionTypeEnum.FIRMWARE_SCHEDULE_NOT_FOUND, msg );
					}
				}
				catch ( ScheduleException e )
				{
					String msg = "Cannot set the specified schedule with ID: " + scheduleId;
					LOG.warn( msg );
					throw new FirmwareException( FirmwareExceptionTypeEnum.FIRMWARE_SCHEDULE_NOT_FOUND, msg );
				}
			}

			FirmwareEntity entity = firmwareDAO.findByDeviceId( firmware.getDeviceId() );
			if ( ( entity != null ) && ( ( entity.getUpdateState() == UpdateStateEnum.FIRMWARE_UPGRADE_PENDING ) || ( entity.getUpdateState() == UpdateStateEnum.FIRMWARE_UPGRADE_ACCEPTED ) ) )
			{
				String msg = "Cannot set firmware - Device " + firmware.getDeviceId() + " is performing upgrade task";
				LOG.warn( msg );
				throw new FirmwareException( FirmwareExceptionTypeEnum.DEVICE_BUSY, msg );
			}

			auditFirmwareUpgrade( firmwareStorage, deviceResource );
		}

		List<ChannelGroupFirmware> channelFirmwareList = new ArrayList();

		for ( Firmware firmware : firmwares )
		{
			FirmwareEntity entity = firmwareDAO.findByDeviceId( firmware.getDeviceId() );
			if ( entity == null )
			{
				entity = new FirmwareEntity();
				entity.readFromDataObject( firmware );
				firmwareDAO.create( entity );
			}
			else
			{
				String newFirmwareId = firmware.getFirmwareId() == null ? "" : firmware.getFirmwareId();
				String entityFirmwareId = entity.getTargetFirmwareId() == null ? "" : entity.getTargetFirmwareId().toString();
				if ( !newFirmwareId.equals( entityFirmwareId ) )
				{
					entity.setUpdateState( UpdateStateEnum.FIRMWARE_UPGRADE_IDLE );
				}
				entity.readFromDataObject( firmware );
			}

			FirmwareUpgradeEvent event = new FirmwareUpgradeEvent( UpdateStateEnum.FIRMWARE_UPGRADE_IDLE, firmware.getDeviceId(), null );
			eventRegistry.sendEventAfterTransactionCommits( event );

			if ( ( firmware.getUpdateType() == UpdateTypeEnum.IMMEDIATE ) && ( !CommonAppUtils.isNullOrEmptyString( firmware.getFirmwareId() ) ) && ( entity.getUpdateState() == UpdateStateEnum.FIRMWARE_UPGRADE_IDLE ) )
			{
				prepareFirmwareTask( firmware, channelFirmwareList );
			}
		}

		for ( ChannelGroupFirmware channelGroup : channelFirmwareList )
		{
			performMultipleChannelFirmwareTask( channelGroup );
		}
	}

	private void prepareFirmwareTask( Firmware firmware, List<ChannelGroupFirmware> channelFirmwareList )
	{
		DeviceResource dResource = getTopologyService().getDeviceResourceByDeviceId( firmware.getDeviceId() );
		DeviceView dView = dResource.getDeviceView();

		if ( !dResource.isRootDevice() )
		{
			String parentDeviceId = dView.getParentDeviceId();

			if ( getTopologyService().getDeviceResourceByDeviceId( parentDeviceId ).getDeviceView().isR5() )
			{
				if ( isRootDeviceUpgrading( parentDeviceId ) )
				{
					updateDeviceFirmwareState( firmware.getDeviceId(), UpdateStateEnum.FIRMWARE_UPGRADE_ERROR, "" );
					return;
				}
				if ( isChildDeviceUpgrading( parentDeviceId, true ) )
				{
					return;
				}
				for ( ChannelGroupFirmware channelGroup : channelFirmwareList )
				{
					if ( ( channelGroup.getParentDeviceId().equals( parentDeviceId ) ) && ( !channelGroup.getFirmwareId().equals( firmware.getFirmwareId() ) ) )
					{

						return;
					}
				}
			}

			boolean multiChannelUpgrade = deviceCapabilityService.isCapabilityEnabled( Long.parseLong( parentDeviceId ), "upgradeChannel.2" );
			if ( multiChannelUpgrade )
			{
				addChannelToList( channelFirmwareList, parentDeviceId, firmware );
				return;
			}
		}
		else if ( ( dView.isR5() ) && ( isChildDeviceUpgrading( firmware.getDeviceId(), false ) ) )
		{
			updateDeviceFirmwareState( firmware.getDeviceId(), UpdateStateEnum.FIRMWARE_UPGRADE_ERROR, "" );
			return;
		}

		applyFirmwareAsync( firmware );
	}

	private void addChannelToList( List<ChannelGroupFirmware> channelFirmwareList, String parentDeviceId, Firmware firmware )
	{
		ChannelGroupFirmware channelFirmware = null;
		for ( ChannelGroupFirmware channelGroup : channelFirmwareList )
		{
			if ( ( channelGroup.getParentDeviceId().equalsIgnoreCase( parentDeviceId ) ) && ( channelGroup.getFirmwareId().equals( firmware.getFirmwareId() ) ) )
			{
				channelFirmware = channelGroup;
				break;
			}
		}

		if ( channelFirmware == null )
		{
			channelFirmware = new ChannelGroupFirmware( parentDeviceId, firmware.getFirmwareId() );
			channelFirmwareList.add( channelFirmware );
		}

		channelFirmware.addChannelDeviceID( firmware.getDeviceId() );
		LOG.debug( "total groups: {}", Integer.valueOf( channelFirmwareList.size() ) );
	}

	private boolean isRootDeviceUpgrading( String deviceId )
	{
		FirmwareEntity entity = firmwareDAO.findByDeviceId( deviceId );
		if ( ( entity != null ) && ( ( entity.getUpdateState() == UpdateStateEnum.FIRMWARE_UPGRADE_PENDING ) || ( entity.getUpdateState() == UpdateStateEnum.FIRMWARE_UPGRADE_ACCEPTED ) ) )
		{
			return true;
		}

		return false;
	}

	private boolean isChildDeviceUpgrading( String deviceId, boolean pendingOnly )
	{
		List<FirmwareEntity> entities = firmwareDAO.findAllByState( UpdateStateEnum.FIRMWARE_UPGRADE_PENDING );

		for ( FirmwareEntity entity : entities )
		{
			DeviceResource devRes = getTopologyService().getDeviceResourceByDeviceId( String.valueOf( entity.getDeviceId() ) );
			if ( !devRes.isRootDevice() )
			{

				String pDeviceId = devRes.getDeviceView().getParentDeviceId();
				if ( deviceId.equals( pDeviceId ) )
				{
					return true;
				}
			}
		}
		return false;
	}

	public Firmware[] findAllDeviceFirmwares() throws FirmwareException
	{
		List<FirmwareEntity> entities = firmwareDAO.findAllDetached();
		List<Firmware> firmwares = new ArrayList();
		if ( ( entities == null ) || ( entities.size() == 0 ) )
		{
			return null;
		}

		for ( FirmwareEntity entity : entities )
		{
			firmwares.add( entity.toDataObject() );
		}

		if ( firmwares.size() == 0 )
		{
			return null;
		}
		return ( Firmware[] ) firmwares.toArray( new Firmware[firmwares.size()] );
	}

	public void setGroupFirmwares( GroupFirmware[] groupFirmwares ) throws FirmwareException
	{
		for ( GroupFirmware groupFirmware : groupFirmwares )
		{
			FileStorageView firmwareStorage = null;
			if ( !CommonAppUtils.isNullOrEmptyString( groupFirmware.getTargetFirmwareId() ) )
			{
				try
				{
					firmwareStorage = fileStorageService.getFileStorage( String.valueOf( groupFirmware.getTargetFirmwareId() ) );
				}
				catch ( FileStorageException e )
				{
					LOG.warn( "Cannot perform firmware task - cannot file the firmware file {}.", groupFirmware.getTargetFirmwareId() );
					throw new FirmwareException( FirmwareExceptionTypeEnum.FIRMWARE_FILE_NOT_FOUND, "firmware file doesn't exist." );
				}
			}
			auditGroupFirmwareUpgrade( firmwareStorage, groupFirmware.getGroup().toString() );
		}

		for ( GroupFirmware groupFirmware : groupFirmwares )
		{
			GroupFirmwareEntity entity = groupFirmwareDAO.findByGroup( groupFirmware.getGroup() );
			if ( entity != null )
			{
				entity.fromDataObject( groupFirmware );

				GroupFirmwareUpgradeEvent event = new GroupFirmwareUpgradeEvent( groupFirmware.getGroup().name(), groupFirmware.getTargetFirmwareId() );
				eventRegistry.sendEventAfterTransactionCommits( event );
				LOG.debug( "Send FirmwareUpgradeEvent: {} for device {}.", EventTypesEnum.GROUP_FIRMWARE_UPGRADE_CHANGED.getFullPathEventName(), groupFirmware.getGroup().toString() );
			}
		}
	}

	public GroupFirmware[] findAllGroupFirmwares() throws FirmwareException
	{
		List<GroupFirmwareEntity> entities = groupFirmwareDAO.findAllDetached();
		List<GroupFirmware> groupFirmwares = new ArrayList();
		if ( ( entities == null ) || ( entities.size() == 0 ) )
		{
			return null;
		}

		for ( GroupFirmwareEntity entity : entities )
		{
			groupFirmwares.add( entity.toDataObject() );
		}

		return ( GroupFirmware[] ) groupFirmwares.toArray( new GroupFirmware[groupFirmwares.size()] );
	}

	public void applyFirmwareAsync( Firmware firmware )
	{
		if ( firmware == null )
		{
			return;
		}

		if ( !CommonAppUtils.isNullOrEmptyString( firmware.getFirmwareId() ) )
		{
			if ( isChildDeviceUpgrading( firmware.getDeviceId(), false ) )
			{
				updateDeviceFirmwareState( firmware.getDeviceId(), UpdateStateEnum.FIRMWARE_UPGRADE_ERROR, "" );
				return;
			}
			FirmwareUpgradeTask task = new FirmwareUpgradeTask( firmware.getDeviceId(), firmware.getFirmwareId(), firmware.getOptParams() );
			taskScheduler.executeFixedPoolSerial( task, firmware.getDeviceId() );
		}
	}

	private void performMultipleChannelFirmwareTask( ChannelGroupFirmware channelFirmware )
	{
		ChannelFirmwareUpgradeTask task = new ChannelFirmwareUpgradeTask( channelFirmware );
		taskScheduler.executeNow( task );
	}

	public void applyMultipleChannelFirmware( ChannelGroupFirmware channelFirmware )
	{
		List<String> channelDeviceIds = new ArrayList<String>();
		try
		{
			FileStorageView firmwareStorage = fileStorageService.getFileStorage( channelFirmware.getFirmwareId() );
			String version = firmwareStorage.getProperty( "FIRMWARE_VERSION" );
			String fileName = firmwareStorage.getProperty( "FIRMWARE_FILENAME" );

			for ( String channelDeviceId : channelFirmware.getChannelDeviceIDs() )
			{
				try
				{
					DeviceConfigDescriptor deviceDesc = configService.getDeviceConfigByDeviceId( channelDeviceId );

					if ( ( deviceDesc.getAssignState() == DeviceImageState.PENDING ) || ( deviceDesc.getAssignState() == DeviceImageState.PENDING_OFFLINE ) || ( deviceDesc.getAssignState() == DeviceImageState.WAITING ) )
					{

						LOG.warn( "Channel {} is performing configuration changes, Firmware upgrade is deferred.", channelDeviceId );
						continue;
					}

					if ( configService.isAssociatedDeviceConfigurationOnProgress( channelDeviceId ) )
					{
						updateDeviceFirmwareState( channelDeviceId, UpdateStateEnum.FIRMWARE_UPGRADE_ERROR, "" );
						LOG.warn( "Channel {} Associated Root device is performing configuration changes, Firmware upgrade failed.", channelDeviceId );
						continue;
					}

					DeviceResource deviceRes = getTopologyService().getDeviceResourceByDeviceId( channelDeviceId );
					if ( deviceRes == null )
					{
						continue;
					}

					String channelVersion = deviceRes.getDeviceView().getSoftwareVersion();
					if ( channelVersion.equals( version ) )
					{
						updateDeviceFirmwareState( channelDeviceId, UpdateStateEnum.FIRMWARE_UPGRADE_COMPLETED, version );
						continue;
					}

					channelDeviceIds.add( channelDeviceId );
				}
				catch ( ConfigurationException e )
				{
					LOG.warn( "Get Device {} Config failed.", channelDeviceId );
					updateDeviceFirmwareState( channelDeviceId, UpdateStateEnum.FIRMWARE_UPGRADE_ERROR, "" );
				}
			}

			if ( channelDeviceIds.isEmpty() )
			{
				return;
			}

			deviceService.upgrade( channelFirmware.getParentDeviceId(), channelDeviceIds, version, fileName, firmwareStorage.getFileObject().getInputStream(), null );
			currentUpgradeTasks.add( new UpgradeTaskInfo( channelFirmware.getParentDeviceId(), channelFirmware.getFirmwareId(), channelDeviceIds ) );
			for ( String channel : channelDeviceIds )
			{
				updateDeviceFirmwareState( channel, UpdateStateEnum.FIRMWARE_UPGRADE_PENDING, version );
				FirmwareEntity entity = firmwareDAO.findByDeviceId( channel );
				entity.setFailureRetryCount( 3L );
			}
		}
		catch ( DeviceException e )
		{
			LOG.warn( "Failed to apply channel upgrade for device {}, reason: {}.", channelFirmware.getParentDeviceId(), e.getMessage() );
			updateChannelsFirmwareState( channelDeviceIds, UpdateStateEnum.FIRMWARE_UPGRADE_ERROR, "" );
		}
		catch ( FileStorageException e )
		{
			LOG.warn( "Get firmware file storage {} failed.", channelFirmware.getFirmwareId() );
			updateChannelsFirmwareState( channelDeviceIds, UpdateStateEnum.FIRMWARE_UPGRADE_ERROR, "" );
		}
	}

	public void applyFirmware( String deviceId, String firmwareId, String optParameters )
	{
		try
		{
			DeviceResource deviceRes = getTopologyService().getDeviceResourceByDeviceId( deviceId );
			if ( deviceRes == null )
			{
				return;
			}

			FirmwareEntity entity = firmwareDAO.findByDeviceId( deviceId );
			if ( entity.getTargetFirmwareId() == null )
			{
				return;
			}

			DeviceView device = deviceRes.getDeviceView();

			String deviceSWVersion = device.getSoftwareVersion();
			UpdFileInfo updInfo = getUpdateFileInfo( Long.valueOf( firmwareId ).longValue() );
			String targetSWVersion = updInfo.getTargetVersion( device.getModelName() );
			if ( CommonAppUtils.isNullOrEmptyString( targetSWVersion ) )
			{
				LOG.warn( "!!! Cannot find the target version from firmware {}, model name {}", updInfo.getName(), device.getModelName() );
				return;
			}

			LOG.debug( "deviceSWVersion: {}, targetSWVersion: {}", deviceSWVersion, targetSWVersion );

			if ( ( !updInfo.getFileType().equalsIgnoreCase( FirmwareFileTypeEnum.pat.toString() ) ) && ( CommonUtils.compareVersions( deviceSWVersion, targetSWVersion ) == 0 ) )
			{
				LOG.debug( "Device {} has been upgraded to {}.", deviceId, targetSWVersion );
				updateDeviceFirmwareState( deviceId, UpdateStateEnum.FIRMWARE_UPGRADE_COMPLETED, targetSWVersion );
				currentUpgradeTasks.remove( getUpgradeTaskInfo( deviceId, firmwareId ) );
				return;
			}

			if ( ( deviceRes.isRootDevice() ) && ( device.getConnectState() == ConnectState.OFFLINE ) )
			{
				LOG.warn( "Firmware {} upgrade for device ID {} deferred due to the device offline", firmwareId, deviceId );
				return;
			}

			DeviceConfigDescriptor deviceDesc = configService.getDeviceConfigByDeviceId( deviceId );
			if ( ( deviceDesc.getAssignState() == DeviceImageState.PENDING ) || ( deviceDesc.getAssignState() == DeviceImageState.PENDING_OFFLINE ) || ( deviceDesc.getAssignState() == DeviceImageState.WAITING ) )
			{

				LOG.warn( "Firmware {} upgrade for device ID {} deferred due to the device performing configuration changes.", firmwareId, deviceId );
				return;
			}

			if ( configService.isAssociatedDeviceConfigurationOnProgress( deviceId ) )
			{
				LOG.warn( "Firmware {} upgrade for device ID {} failed due to the associated device performing configuration changes.", firmwareId, deviceId );
				updateDeviceFirmwareState( deviceId, UpdateStateEnum.FIRMWARE_UPGRADE_ERROR, "" );
				return;
			}

			if ( ( DeviceManagementConstants.isR4_5000_Device( device.getFamily(), device.getModel() ) ) && ( ( deviceSWVersion.contains( "4.9.1" ) ) || ( deviceSWVersion.contains( "4.9.2" ) ) ) )
			{
				LOG.warn( "Device {} currently running SW version {} - blocking the upgrade", device.getDeviceId(), deviceSWVersion );
				updateDeviceFirmwareState( deviceId, UpdateStateEnum.FIRMWARE_UPGRADE_ERROR, "" );
				return;
			}

			FileStorageView firmwareStorage = fileStorageService.getFileStorage( String.valueOf( firmwareId ) );
			if ( firmwareStorage == null )
			{
				LOG.warn( "Cannot perform firmware task - cannot find the firmware file {}.", firmwareId );
				updateDeviceFirmwareState( deviceId, UpdateStateEnum.FIRMWARE_UPGRADE_ERROR, "" );
				return;
			}

			if ( entity.getUpdateState() == UpdateStateEnum.FIRMWARE_UPGRADE_TIMEOUT )
			{
				FirmwareUpgradeEvent event = new FirmwareUpgradeEvent( UpdateStateEnum.FIRMWARE_UPGRADE_TIMEOUT, entity.getDeviceId().toString(), deviceSWVersion );
				eventRegistry.sendEventAfterTransactionCommits( event );
				return;
			}

			if ( ( entity.getUpdateState() != UpdateStateEnum.FIRMWARE_UPGRADE_IDLE ) && ( entity.getUpdateState() != UpdateStateEnum.FIRMWARE_UPGRADE_WAITING ) && ( entity.getUpdateState() != UpdateStateEnum.FIRMWARE_UPGRADE_ACCEPTED ) )
			{

				return;
			}

			String version = null;
			String firmwareFilename = null;

			if ( ( firmwareStorage.getProperty( "FIRMWARE_FILETYPE" ) == null ) || ( firmwareStorage.getProperty( "FIRMWARE_FILETYPE" ).equalsIgnoreCase( FirmwareFileTypeEnum.upg.toString() ) ) || ( firmwareStorage.getProperty( "FIRMWARE_FILETYPE" ).equalsIgnoreCase( FirmwareFileTypeEnum.pat.toString() ) ) )
			{

				String minVersion = firmwareStorage.getProperty( "FIRMWARE_MINVERSION" );
				if ( ( !CommonAppUtils.isNullOrEmptyString( minVersion ) ) && ( CommonUtils.compareVersions( device.getSoftwareVersion(), minVersion ) < 0 ) )
				{
					LOG.warn( "Device version {} is lower than the firmware minimum supported version:{}", device.getSoftwareVersion(), minVersion );
					updateDeviceFirmwareState( deviceId, UpdateStateEnum.FIRMWARE_UPGRADE_ERROR, "" );
					return;
				}

				String maxVersion = firmwareStorage.getProperty( "FIRMWARE_MAXVERSION" );
				if ( ( !CommonAppUtils.isNullOrEmptyString( maxVersion ) ) && ( CommonUtils.compareVersions( device.getSoftwareVersion(), maxVersion ) > 0 ) )
				{
					LOG.warn( "Device version {} is newer than the firmware maximum supported version:{}", device.getSoftwareVersion(), maxVersion );
					updateDeviceFirmwareState( deviceId, UpdateStateEnum.FIRMWARE_UPGRADE_ERROR, "" );
					return;
				}

				version = firmwareStorage.getProperty( "FIRMWARE_VERSION" );
				firmwareFilename = firmwareStorage.getProperty( "FIRMWARE_FILENAME" );

			}
			else
			{
				UpgFileInfo upgFile = updInfo.getNextUpg( device.getSoftwareVersion(), device.getModelName() );
				if ( upgFile == null )
				{
					LOG.warn( "Cannot perform firmware task - cannot find next upgrade file." );
					updateDeviceFirmwareState( deviceId, UpdateStateEnum.FIRMWARE_UPGRADE_ERROR, "" );
					return;
				}
				firmwareFilename = upgFile.getUpgFileName();
				version = upgFile.getTarVersion();
				optParameters = "";
				firmwareStorage.setUpgName( firmwareFilename );
			}

			LOG.debug( "Calling deviceService.upgrade(deviceId=" + deviceId + ", version=" + version + ", filename=" + firmwareFilename + ")" );
			deviceService.upgrade( deviceId, version, firmwareFilename, firmwareStorage.getFileObject().getInputStream(), optParameters );
			updateDeviceFirmwareState( deviceId, UpdateStateEnum.FIRMWARE_UPGRADE_PENDING, version );

			setUpdateTaskInfo( deviceId, firmwareId );
		}
		catch ( ConfigurationException e1 )
		{
			LOG.warn( "Get Device {} Config failed.", deviceId );
			updateDeviceFirmwareState( deviceId, UpdateStateEnum.FIRMWARE_UPGRADE_ERROR, "" );
		}
		catch ( DeviceException e )
		{
			LOG.warn( "Failed to apply device configuration for device " + deviceId, e );
			updateDeviceFirmwareState( deviceId, UpdateStateEnum.FIRMWARE_UPGRADE_ERROR, "" );
		}
		catch ( FileStorageException e )
		{
			LOG.warn( "Get firmware file storage {} failed.", firmwareId );
			updateDeviceFirmwareState( deviceId, UpdateStateEnum.FIRMWARE_UPGRADE_ERROR, "" );
		}
	}

	public boolean canStartFirmwareUpgradeOnReconnect( String deviceId )
	{
		FirmwareEntity entity = firmwareDAO.findByDeviceId( deviceId );
		if ( entity == null )
		{
			return false;
		}

		if ( ( entity.getUpdateType() == UpdateTypeEnum.SCHEDULED ) && ( entity.getUpdateState() == UpdateStateEnum.FIRMWARE_UPGRADE_IDLE ) )
		{
			return false;
		}

		if ( ( entity.getUpdateState() == UpdateStateEnum.FIRMWARE_UPGRADE_ERROR ) && ( entity.getFailureRetryCount() < 3L ) )
		{
			entity.setUpdateState( UpdateStateEnum.FIRMWARE_UPGRADE_IDLE );
		}

		if ( entity.getUpdateState() == UpdateStateEnum.FIRMWARE_UPGRADE_WAITING_ACCEPT )
		{
			GenericValue lastUpdateState = deviceService.fetchDeviceParameterValue( deviceId, DeviceEventsEnum.SYSTEM_LASTUPDATE.getPath() );
			if ( ( lastUpdateState != null ) && ( !CommonAppUtils.isNullOrEmptyString( lastUpdateState.getStringValue() ) ) && ( "ok".equalsIgnoreCase( lastUpdateState.getStringValue() ) ) )
			{
				entity.setUpdateState( UpdateStateEnum.FIRMWARE_UPGRADE_ACCEPTED );
			}
		}

		if ( ( entity.getUpdateState() == UpdateStateEnum.FIRMWARE_UPGRADE_IDLE ) || ( entity.getUpdateState() == UpdateStateEnum.FIRMWARE_UPGRADE_WAITING ) || ( entity.getUpdateState() == UpdateStateEnum.FIRMWARE_UPGRADE_ACCEPTED ) )
		{

			return true;
		}

		return false;
	}

	public Firmware getDeviceTargetFirmware( String deviceId, String firmwareVersion ) throws FirmwareException
	{
		LOG.info( "DU: check the Firmeare for device: {}, firmwareVersion: {}.", deviceId, firmwareVersion );

		DeviceResource deviceResource = getTopologyService().getDeviceResourceByDeviceId( deviceId );
		if ( deviceResource == null )
		{
			throw new FirmwareException( FirmwareExceptionTypeEnum.DEVICE_NOT_FOUND, "Device not found" );
		}

		DeviceView device = deviceResource.getDeviceView();
		FileStorageView firmwareFile = fileStorageService.getFirstMatchFileStorage( firmwareVersion, device );
		LOG.info( "looking for FW files for family: {}, model: {} and version: " + firmwareVersion, device.getFamily(), device.getModel() );
		if ( firmwareFile == null )
		{
			throw new FirmwareException( FirmwareExceptionTypeEnum.FIRMWARE_FILE_NOT_FOUND, "Matching firmware not found" );
		}
		Firmware firmware = new Firmware();
		firmware.setDeviceId( deviceId );
		firmware.setFirmwareId( firmwareFile.getFileId() );
		firmware.setUpdateType( UpdateTypeEnum.IMMEDIATE );
		LOG.debug( " get firmware version : " + firmwareVersion );
		return firmware;
	}

	public Firmware getDeviceFirmware( String deviceId )
	{
		FirmwareEntity entity = firmwareDAO.findByDeviceId( deviceId );
		if ( entity != null )
		{
			return entity.toDataObject();
		}

		return null;
	}

	public GroupFirmware getGroupFirmware( FirmwareGroupEnum groupId )
	{
		GroupFirmwareEntity entity = groupFirmwareDAO.findByGroup( groupId );
		if ( entity != null )
		{
			return entity.toDataObject();
		}

		return null;
	}

	public boolean isFileAssociated( String fileStorageId )
	{
		if ( ( firmwareDAO.isFileAssociated( fileStorageId ) ) || ( groupFirmwareDAO.isFileAssociated( fileStorageId ) ) )
		{
			return true;
		}
		return false;
	}

	public void deleteDeviceFirmware( String deviceId )
	{
		FirmwareEntity entity = firmwareDAO.findByDeviceId( deviceId );
		if ( entity != null )
		{
			firmwareDAO.delete( entity );
		}
	}

	public void updateDeviceFirmwareState( String deviceId, UpdateStateEnum state, String firmwareVersion )
	{
		FirmwareEntity entity = firmwareDAO.findByDeviceId( deviceId );
		if ( ( entity != null ) && ( state != entity.getUpdateState() ) )
		{
			LOG.debug( "DU: Set device {} firmware update State to: {} for device {}.", deviceId, state.name() );
			entity.setUpdateState( state );
			FirmwareUpgradeEvent event = new FirmwareUpgradeEvent( state, deviceId, firmwareVersion );
			eventRegistry.sendEventAfterTransactionCommits( event );
		}
	}

	public void updateChannelsFirmwareState( List<String> channels, UpdateStateEnum state, String firmwareVersion )
	{
		for ( String channel : channels )
		{
			updateDeviceFirmwareState( channel, state, firmwareVersion );
		}
	}

	public String getDefaultFirmwareVersion( String familyId, String modelId )
	{
		if ( ( familyId == null ) || ( modelId == null ) )
		{
			return null;
		}
		FirmwareGroupEnum group = getFirmwareGroup( familyId, modelId );
		if ( group != null )
		{
			GroupFirmwareEntity entity = groupFirmwareDAO.findByGroup( group );
			if ( entity.getTargetFirmwareId() != null )
			{
				try
				{
					FileStorageView firmwareStorage = fileStorageService.getFileStorage( String.valueOf( entity.getTargetFirmwareId() ) );
					return firmwareStorage.getProperty( "FIRMWARE_VERSION" );
				}
				catch ( FileStorageException e )
				{
					return null;
				}
			}
		}

		return null;
	}

	public Long checkGroupFirmware( String deviceId )
	{
		DeviceResource deviceResource = getTopologyService().getDeviceResourceByDeviceId( deviceId );
		DeviceView device = deviceResource.getDeviceView();
		String familyId = device.getFamily();
		String modelId = device.getModel();

		FirmwareGroupEnum group = getFirmwareGroup( familyId, modelId );

		if ( group == null )
		{
			return null;
		}

		GroupFirmwareEntity entity = groupFirmwareDAO.findByGroup( group );
		if ( entity.getTargetFirmwareId() != null )
		{
			try
			{
				FileStorageView firmwareStorage = fileStorageService.getFileStorage( String.valueOf( entity.getTargetFirmwareId() ) );
				if ( firmwareStorage == null )
				{
					LOG.warn( "Cannot perform firmware update, the firmware {} associated with group {} cannot be found.", entity.getTargetFirmwareId(), group );
					return null;
				}

				if ( !familyId.equalsIgnoreCase( firmwareStorage.getProperty( "FIRMWARE_MODEL" ) ) )
				{
					return null;
				}

				if ( ( firmwareStorage.getProperty( "FIRMWARE_TYPE" ) != null ) && ( CommonUtils.checkDeviceModel( modelId, firmwareStorage.getProperty( "FIRMWARE_TYPE" ) ) != 1 ) )
				{
					LOG.debug( "DU: Device {} model {} doesn't match", deviceId, modelId );
					return null;
				}

				UpdFileInfo updInfo = getUpdateFileInfo( entity.getTargetFirmwareId().longValue() );
				if ( CommonUtils.compareVersions( device.getSoftwareVersion(), updInfo.getTargetVersion( device.getModelName() ) ) >= 0 )
				{
					return null;
				}

				return entity.getTargetFirmwareId();
			}
			catch ( FileStorageException e )
			{
				LOG.warn( "Cannot perform firmware update, the firmware {} associated with group {} cannot be found.", entity.getTargetFirmwareId(), group );
				return null;
			}
			catch ( IllegalArgumentException e )
			{
				LOG.warn( "Device version could not be found for device " + device.getModelName() );
				return null;
			}
		}

		return null;
	}

	public Firmware prepareGroupFirmware( String deviceId, Long firmwareId )
	{
		Firmware firmware = null;
		if ( ( deviceId != null ) && ( firmwareId != null ) && ( firmwareId.longValue() != 0L ) )
		{
			boolean newUpdate = false;
			FirmwareEntity firmwareEntity = firmwareDAO.findByDeviceId( deviceId );
			if ( firmwareEntity == null )
			{
				newUpdate = true;
				firmwareEntity = new FirmwareEntity();
			}

			firmwareEntity.setDeviceId( Long.valueOf( deviceId ) );
			firmwareEntity.setTargetFirmwareId( firmwareId );
			firmwareEntity.setUpdateType( UpdateTypeEnum.IMMEDIATE );
			firmwareEntity.setUpdateState( UpdateStateEnum.FIRMWARE_UPGRADE_IDLE );

			if ( newUpdate )
			{
				firmwareDAO.create( firmwareEntity );
			}

			firmware = firmwareEntity.toDataObject();
		}

		return firmware;
	}

	protected void auditFirmwareUpgrade( FileStorageView firmwareView, DeviceResource deviceResource )
	{
		if ( CommonAppUtils.getUsernameFromSecurityContext() != null )
		{
			String firmwareVersion = "";
			if ( firmwareView != null )
			{
				firmwareVersion = firmwareView.getProperty( "FIRMWARE_VERSION" );
			}

			AuditView.Builder auditBuilder = new AuditView.Builder( AuditEventNameEnum.FIRMWARE_UPGRADE.getName() ).addDetailsPair( "firmware_version", firmwareVersion );

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

	protected void auditGroupFirmwareUpgrade( FileStorageView firmwareView, String group )
	{
		if ( CommonAppUtils.getUsernameFromSecurityContext() != null )
		{
			String firmwareVersion = "";
			if ( firmwareView != null )
			{
				firmwareVersion = firmwareView.getProperty( "FIRMWARE_VERSION" );
			}

			AuditView.Builder auditBuilder = new AuditView.Builder( AuditEventNameEnum.GROUP_FIRMWARE_UPGRADE.getName() ).addDetailsPair( "firmware_group", group ).addDetailsPair( "firmware_version", firmwareVersion );

			eventRegistry.sendEventAfterTransactionCommits( new AuditEvent( auditBuilder.build() ) );
		}
	}

	private void checkForFirmwareUpdates()
	{
		List<FirmwareEntity> entities = firmwareDAO.findAllUnfinishedUpgrade();
		for ( FirmwareEntity entity : entities )
		{
			entity.setUpdateState( UpdateStateEnum.FIRMWARE_UPGRADE_ERROR );
		}
	}

	private FirmwareGroupEnum getFirmwareGroup( String familyId, String modelId )
	{
		if ( ( DeviceManagementConstants.isR5_Gen5Fixed_Device( familyId, modelId ) ) || ( DeviceManagementConstants.isR5_Gen6_Device( familyId, modelId ) ) || ( DeviceManagementConstants.isR5_Sandtrap_Device( familyId, modelId ) ) )
		{

			return FirmwareGroupEnum.R5_FIXED;
		}
		if ( familyId.equalsIgnoreCase( "257" ) )
		{
			if ( "1".equals( modelId ) )
			{
				return FirmwareGroupEnum.R5_FIXED;
			}
			if ( DeviceManagementConstants.isMobileDevice( familyId, modelId ) )
			{
				return FirmwareGroupEnum.RIDESAFE;
			}
		}
		else if ( ( familyId.equalsIgnoreCase( "256" ) ) && ( DeviceManagementConstants.isMobileDevice( familyId, modelId ) ) )
		{
			return FirmwareGroupEnum.R4;
		}

		return null;
	}

	private void createFirmwareGroups()
	{
		List<GroupFirmwareEntity> entities = groupFirmwareDAO.findAll();
		if ( ( entities == null ) || ( entities.size() == 0 ) )
		{
			FirmwareGroupEnum[] groups = FirmwareGroupEnum.values();

			for ( FirmwareGroupEnum group : groups )
			{
				GroupFirmwareEntity entity = new GroupFirmwareEntity();
				entity.setGroup( group );
				groupFirmwareDAO.create( entity );
			}
		}
	}

	private UpdFileInfo getUpdateFileInfo( long firmwareId ) throws FileStorageException
	{
		for ( UpdFileInfo updateFile : currentUpdateFiles )
		{
			if ( updateFile.getFirmwareId() == firmwareId )
			{
				return updateFile;
			}
		}

		String firmwareFileObjectId = String.valueOf( firmwareId );

		FileStorageView firmwareStorage = fileStorageService.getFileStorage( firmwareFileObjectId );
		if ( firmwareStorage == null )
		{
			String msg = "Could not retrieve required properties for firmware application from file storage id " + firmwareFileObjectId;
			throw new FileStorageException( FileStorageExceptionType.FILE_NOT_FOUND, msg );
		}

		String firmwareFilename = firmwareStorage.getProperty( "FIRMWARE_FILENAME" );
		String filePath = fileStorageService.getFileStorageObject( firmwareStorage.getFileId() ).getFileRepositoryPath();
		UpdFileInfo updInfo = fileStorageService.getUpdFileInfo( firmwareFilename, filePath );

		updInfo.setFirmwareId( firmwareId );
		updInfo.setTargetVersion( firmwareStorage.getProperty( "FIRMWARE_VERSION" ) );
		if ( firmwareStorage.getProperty( "FIRMWARE_FILETYPE" ) == null )
		{
			updInfo.setFileType( FirmwareFileTypeEnum.upg.toString() );
		}
		else
		{
			updInfo.setFileType( firmwareStorage.getProperty( "FIRMWARE_FILETYPE" ) );
			if ( firmwareStorage.getProperty( "FIRMWARE_FILETYPE" ).equalsIgnoreCase( FirmwareFileTypeEnum.rel.toString() ) )
			{
				updInfo.setVersionListe( firmwareStorage.getProperty( "FIRMWARE_VERSIONLIST" ) );
			}
		}

		currentUpdateFiles.add( updInfo );

		return updInfo;
	}

	private boolean checkCCMCameraModel( DeviceView device, FileStorageView file )
	{
		String models = file.getProperty( "FIRMWARE_CCMDEVICEMODELS" );

		if ( models != null )
		{
			List<ChannelDeviceModel> modelList = ( List ) CoreJsonSerializer.collectionFromJson( models, new TypeToken()
			{
			} );
			for ( ChannelDeviceModel channelDevice : modelList )
			{
				if ( channelDevice.matches( new ChannelDeviceModel( device.getModelName(), device.getFamily(), device.getModel() ) ) )
				{
					return true;
				}
			}
		}

		return false;
	}

	public synchronized void onFirmwareFileRemoved( String firmwareFileId )
	{
		Long firmwareId = Long.valueOf( firmwareFileId );

		List<FirmwareEntity> entities = firmwareDAO.findAll();
		for ( FirmwareEntity entity : entities )
		{
			if ( ( entity.getTargetFirmwareId() != null ) && ( entity.getTargetFirmwareId().longValue() == firmwareId.longValue() ) )
			{
				entity.setTargetFirmwareId( null );
				if ( ( entity.getUpdateState() == UpdateStateEnum.FIRMWARE_UPGRADE_PENDING ) || ( entity.getUpdateState() == UpdateStateEnum.FIRMWARE_UPGRADE_ACCEPTED ) )
				{
					updateDeviceFirmwareState( entity.getDeviceId().toString(), UpdateStateEnum.FIRMWARE_UPGRADE_ERROR, null );
				}
				else
				{
					updateDeviceFirmwareState( entity.getDeviceId().toString(), UpdateStateEnum.FIRMWARE_UPGRADE_IDLE, null );
				}
			}
		}

		List<GroupFirmwareEntity> groupEntities = groupFirmwareDAO.findAll();
		for ( GroupFirmwareEntity groupEntity : groupEntities )
		{
			if ( ( groupEntity.getTargetFirmwareId() != null ) && ( groupEntity.getTargetFirmwareId().longValue() == firmwareId.longValue() ) )
			{
				groupEntity.setTargetFirmwareId( null );
				GroupFirmwareUpgradeEvent event = new GroupFirmwareUpgradeEvent( groupEntity.getGroup().name(), null );
				eventRegistry.sendEventAfterTransactionCommits( event );
			}
		}

		for ( UpdFileInfo updateFile : currentUpdateFiles )
		{
			if ( updateFile.getFirmwareId() == firmwareId.longValue() )
			{
				currentUpdateFiles.remove( updateFile );
				break;
			}
		}
	}

	public void onFirmwareScheduleStart( Long scheduleId )
	{
		LOG.debug( "Firmware Schedule start: scheduleId {}", scheduleId );
		List<ChannelGroupFirmware> channelFirmwareList = new ArrayList();
		List<FirmwareEntity> entities = firmwareDAO.findByScheduleId( scheduleId );
		for ( FirmwareEntity entity : entities )
		{
			DeviceResource deviceRes = getTopologyService().getDeviceResourceByDeviceId( entity.toDataObject().getDeviceId() );
			if ( deviceRes.isRootDevice() )
			{
				if ( ( entity.getUpdateState() == UpdateStateEnum.FIRMWARE_UPGRADE_TIMEOUT ) || ( entity.getUpdateState() == UpdateStateEnum.FIRMWARE_UPGRADE_ERROR ) )
				{
					entity.setUpdateState( UpdateStateEnum.FIRMWARE_UPGRADE_IDLE );
				}
			}
			else if ( UpdateStateEnum.FIRMWARE_UPGRADE_IDLE == entity.getUpdateState() )
			{
				entity.setUpdateState( UpdateStateEnum.FIRMWARE_UPGRADE_WAITING );
			}

			if ( ( entity.getUpdateState() == UpdateStateEnum.FIRMWARE_UPGRADE_IDLE ) || ( entity.getUpdateState() == UpdateStateEnum.FIRMWARE_UPGRADE_WAITING ) )
			{
				Firmware firmware = entity.toDataObject();
				prepareFirmwareTask( firmware, channelFirmwareList );
			}
		}

		for ( ChannelGroupFirmware channelGroup : channelFirmwareList )
		{
			performMultipleChannelFirmwareTask( channelGroup );
		}
	}

	public void onFirmwareScheduleEnd( Long scheduleId )
	{
		LOG.debug( "Firmware Schedule end: scheduleId {}", scheduleId );
		List<FirmwareEntity> entities = firmwareDAO.findByScheduleId( scheduleId );
		for ( FirmwareEntity entity : entities )
		{
			if ( ( entity.getUpdateState() == UpdateStateEnum.FIRMWARE_UPGRADE_PENDING ) || ( entity.getUpdateState() == UpdateStateEnum.FIRMWARE_UPGRADE_ACCEPTED ) )
			{
				DeviceResource deviceRes = getTopologyService().getDeviceResourceByDeviceId( entity.toDataObject().getDeviceId() );
				if ( deviceRes.isRootDevice() )
				{
					entity.setUpdateState( UpdateStateEnum.FIRMWARE_UPGRADE_TIMEOUT );
				}
			}

			if ( entity.getUpdateState() == UpdateStateEnum.FIRMWARE_UPGRADE_WAITING )
			{
				entity.setUpdateState( UpdateStateEnum.FIRMWARE_UPGRADE_IDLE );
			}
		}
	}

	public void onFirmwareScheduleDeleted( Long scheduleId )
	{
		List<FirmwareEntity> entities = firmwareDAO.findByScheduleId( scheduleId );
		for ( FirmwareEntity entity : entities )
		{
			entity.setSchedulerId( null );
			FirmwareUpgradeEvent event = new FirmwareUpgradeEvent( UpdateStateEnum.FIRMWARE_UPGRADE_IDLE, entity.getDeviceId().toString(), null );
			eventRegistry.sendEventAfterTransactionCommits( event );
		}
	}

	public void handleUpgradeEvent( String deviceId, AbstractDeviceConfigurationEvent event )
	{
		if ( !isUpgradeInProgress( deviceId ) )
		{
			return;
		}

		FirmwareEntity entity = firmwareDAO.findByDeviceId( deviceId );
		UpgradeTaskInfo task = getUpgradeTaskInfo( deviceId, entity.getTargetFirmwareId().toString() );
		DeviceResource deviceRes = getTopologyService().getDeviceResourceByDeviceId( deviceId );

		if ( DeviceConfigurationEventType.UPGRADE_ACCEPTED.equals( event.getDeviceEventType() ) )
		{
			task.setUpgradeStartTimeWithCurrentTime();
			if ( !deviceRes.isRootDevice() )
			{
				entity.setUpdateState( UpdateStateEnum.FIRMWARE_UPGRADE_ACCEPTED );

				startNextCamerUpgrade( task.getDeviceId() );
				return;
			}

			if ( ( entity.getUpdateState() == UpdateStateEnum.FIRMWARE_UPGRADE_WAITING_ACCEPT ) && ( event.getTaskId().equals( task.getUpgradeTaskId() ) ) )
			{
				try
				{
					FileStorageView firmwareStorage = fileStorageService.getFileStorage( entity.getTargetFirmwareId().toString() );
					if ( ( firmwareStorage.getProperty( "FIRMWARE_FILETYPE" ) != null ) && ( firmwareStorage.getProperty( "FIRMWARE_FILETYPE" ).equalsIgnoreCase( FirmwareFileTypeEnum.pat.toString() ) ) )
					{

						if ( ( firmwareStorage.getProperty( "FIRMWARE_RESTART" ) == null ) || ( firmwareStorage.getProperty( "FIRMWARE_RESTART" ).equalsIgnoreCase( "false" ) ) )
						{
							LOG.info( "Device {} has completed Patch Upgrade.", deviceId );
							updateDeviceFirmwareState( deviceId, UpdateStateEnum.FIRMWARE_UPGRADE_COMPLETED, null );
							currentUpgradeTasks.remove( task );
							return;
						}
					}
				}
				catch ( FileStorageException e )
				{
					LOG.error( e.getMessage() );
					updateDeviceFirmwareState( deviceId, UpdateStateEnum.FIRMWARE_UPGRADE_ERROR, e.getMessage() );
					currentUpgradeTasks.remove( task );
					return;
				}

				entity.setUpdateState( UpdateStateEnum.FIRMWARE_UPGRADE_ACCEPTED );
			}
		}
		else if ( DeviceConfigurationEventType.UPGRADE_FAILED.equals( event.getDeviceEventType() ) )
		{
			if ( ( entity.getUpdateState() == UpdateStateEnum.FIRMWARE_UPGRADE_PENDING ) || ( entity.getUpdateState() == UpdateStateEnum.FIRMWARE_UPGRADE_WAITING_ACCEPT ) )
			{

				if ( ( deviceRes.isRootDevice() ) && ( deviceRes.getDeviceView().getConnectState() == ConnectState.ONLINE ) && ( entity.getFailureRetryCount() < 3L ) )
				{

					LOG.debug( "Device {} will retry firmware upgrade later.", deviceId );
					entity.setUpdateState( UpdateStateEnum.FIRMWARE_UPGRADE_IDLE );
					entity.setFailureRetryCount( entity.getFailureRetryCount() + 1L );
					DeferredEvent deferredEvent = new DeferredEvent( new DeviceDeferredUpgradeEvent( deviceId, entity.getTargetFirmwareId().toString(), entity.getOptParameters(), Long.valueOf( 0L ) ), ConnectState.ONLINE.toString(), 120000L, true );
					deferredEventPool.set( deviceId, deferredEvent );
				}
				else
				{
					LOG.warn( "Firmware upgrade for device ID {} failed due to the device offline or reaching maximum retries.", deviceId );
					updateDeviceFirmwareState( deviceId, UpdateStateEnum.FIRMWARE_UPGRADE_ERROR, "" );
					if ( !deviceRes.isRootDevice() )
					{
						startNextCamerUpgrade( task.getDeviceId() );

						task.getChannelDeviceIds().remove( deviceId );
						if ( task.getChannelDeviceIds().size() > 0 )
						{
							task.setUpgradeStartTimeWithCurrentTime();
						}
						else
						{
							currentUpgradeTasks.remove( task );
						}
					}
					else
					{
						currentUpgradeTasks.remove( task );
					}
				}
			}
		}
		else if ( ( DeviceConfigurationEventType.UPGRADE_WAITING_ACCEPT.equals( event.getDeviceEventType() ) ) && ( entity.getUpdateState() == UpdateStateEnum.FIRMWARE_UPGRADE_PENDING ) )
		{
			LOG.debug( "Set taskId {} for Device {}", event.getTaskId(), deviceId );
			entity.setUpdateState( UpdateStateEnum.FIRMWARE_UPGRADE_WAITING_ACCEPT );
			task.setUpgradeTaskId( event.getTaskId() );
		}
	}

	public void handleSystemChangedEvent( String deviceId )
	{
		FirmwareEntity entity = firmwareDAO.findByDeviceId( deviceId );
		if ( entity == null )
		{
			return;
		}

		if ( entity.getUpdateState() == UpdateStateEnum.FIRMWARE_UPGRADE_WAITING_ACCEPT )
		{
			GenericValue lastUpdateState = deviceService.fetchDeviceParameterValue( deviceId, DeviceEventsEnum.SYSTEM_LASTUPDATE.getPath() );
			LOG.debug( "Fetched device {} last update state {}", deviceId, lastUpdateState.getStringValue() );
			if ( ( lastUpdateState == null ) || ( CommonAppUtils.isNullOrEmptyString( lastUpdateState.getStringValue() ) ) )
				return;
			if ( "ok".equalsIgnoreCase( lastUpdateState.getStringValue() ) )
			{
				entity.setUpdateState( UpdateStateEnum.FIRMWARE_UPGRADE_ACCEPTED );
			}
			else if ( "failed".equalsIgnoreCase( lastUpdateState.getStringValue() ) )
			{
				updateDeviceFirmwareState( deviceId, UpdateStateEnum.FIRMWARE_UPGRADE_ERROR, "" );
				return;
			}
		}

		if ( entity.getUpdateState() == UpdateStateEnum.FIRMWARE_UPGRADE_ACCEPTED )
		{
			try
			{
				FileStorageView firmwareStorage = fileStorageService.getFileStorage( entity.getTargetFirmwareId().toString() );
				if ( ( firmwareStorage.getProperty( "FIRMWARE_FILETYPE" ) != null ) && ( firmwareStorage.getProperty( "FIRMWARE_FILETYPE" ).equalsIgnoreCase( FirmwareFileTypeEnum.pat.toString() ) ) )
				{

					updateDeviceFirmwareState( deviceId, UpdateStateEnum.FIRMWARE_UPGRADE_COMPLETED, null );
				}
				else
				{
					LOG.debug( "applyFirmwareAsync for device {}", deviceId );

					applyFirmwareAsync( entity.toDataObject() );
				}
			}
			catch ( FileStorageException e )
			{
				updateDeviceFirmwareState( deviceId, UpdateStateEnum.FIRMWARE_UPGRADE_ERROR, e.getMessage() );
			}
		}
	}

	public void handleDeferredUpgradeEvent( String deviceId )
	{
		Firmware firmware = getDeviceFirmware( deviceId );
		applyFirmwareAsync( firmware );
	}

	public void handleDeviceUpgradeEvent( AbstractDeviceConfigurationEvent deviceConfigEvent, String deviceId )
	{
		LOG.debug( "Processing Device {} Upgrade Event {}. taskID = {}.", new Object[] {deviceId, deviceConfigEvent.getDeviceEventType(), deviceConfigEvent.getTaskId()} );
		if ( ( deviceConfigEvent.getDeviceEventType().equals( DeviceConfigurationEventType.CONFIG_APPLIED ) ) || ( deviceConfigEvent.getDeviceEventType().equals( DeviceConfigurationEventType.CONFIG_APPLIED_LASTCONFIG ) ) )
		{
			Firmware firmware = getDeviceFirmware( deviceId );
			if ( ( firmware != null ) && ( firmware.getUpdateType() == UpdateTypeEnum.IMMEDIATE ) && ( firmware.getUpdateState() == UpdateStateEnum.FIRMWARE_UPGRADE_IDLE ) )
			{
				applyFirmwareAsync( firmware );
			}
		}
		else
		{
			handleUpgradeEvent( deviceId, deviceConfigEvent );
		}
	}

	public void handleDeviceRegistrationEvent( DeviceRegistrationEvent deviceRegistrationEvent, String deviceId )
	{
		RegistrationStatus status = deviceRegistrationEvent.getRegistrationStatus();
		boolean isMassRegsiter = deviceRegistrationEvent.isMassRegistration();
		LOG.debug( "Process Device Registration Event, status: {}", status );
		if ( ( status == RegistrationStatus.REGISTERED ) || ( status == RegistrationStatus.PENDING_REPLACEMENT ) )
		{
			if ( !isMassRegsiter )
			{
				Long groupfirmwareId = checkGroupFirmware( deviceId );
				if ( groupfirmwareId != null )
				{
					Firmware firmware = prepareGroupFirmware( deviceId, groupfirmwareId );
					applyFirmwareAsync( firmware );
				}
			}
		}
		else if ( status == RegistrationStatus.UNREGISTERED )
		{
			deleteDeviceFirmware( deviceId );
		}
	}

	public void handleConnectionStateChnageEvent( DeviceConnectionStateChangeEvent connectionStateEvent )
	{
		LOG.debug( "Processing Device Connection State Change Event state: {}.", connectionStateEvent.getConnectState().name() );
		if ( ( connectionStateEvent.getConnectState() != null ) && ( connectionStateEvent.getConnectState().equals( ConnectState.ONLINE ) ) )
		{
			String deviceId = connectionStateEvent.getDeviceId();
			if ( canStartFirmwareUpgradeOnReconnect( deviceId ) )
			{
				Firmware firmware = getDeviceFirmware( deviceId );
				applyFirmwareAsync( firmware );
			}
		}
	}

	public void handleScheduleEvent( Event event )
	{
		ScheduleEvent scheduleEvent = ( ScheduleEvent ) event;
		LOG.debug( "Processing Schedule Event {}", scheduleEvent.getScheduleEventType() );
		if ( scheduleEvent.getScheduleEventType() == ScheduleEventType.NOTIFICATION )
		{
			Long scheduleId = scheduleEvent.getScheduleId();

			if ( scheduleEvent.isStart() )
			{
				onFirmwareScheduleStart( scheduleId );
			}
			else
			{
				onFirmwareScheduleEnd( scheduleId );
			}
		}
		else if ( scheduleEvent.getScheduleEventType() == ScheduleEventType.DELETED )
		{
			onFirmwareScheduleDeleted( scheduleEvent.getScheduleId() );
		}
	}

	public void handleIPCameraUpgradeEvent( String deviceId )
	{
		DeviceResource deviceResource = getTopologyService().getDeviceResourceByDeviceId( deviceId );
		if ( ( deviceResource == null ) || ( deviceResource.isRootDevice() ) )
		{
			LOG.warn( "Cannot find deviceResource for deviceId: ", deviceId );
			return;
		}

		FirmwareEntity entity = firmwareDAO.findByDeviceId( deviceId );
		if ( entity == null )
		{
			return;
		}

		if ( ( entity.getUpdateState() == UpdateStateEnum.FIRMWARE_UPGRADE_PENDING ) || ( entity.getUpdateState() == UpdateStateEnum.FIRMWARE_UPGRADE_ACCEPTED ) || ( entity.getUpdateState() == UpdateStateEnum.FIRMWARE_UPGRADE_ERROR ) )
		{

			String deviceSWVersion = deviceResource.getDeviceView().getSoftwareVersion();
			try
			{
				FileStorageView firmwareStorage = fileStorageService.getFileStorage( entity.getTargetFirmwareId().toString() );
				if ( firmwareStorage == null )
				{
					LOG.warn( "Cannot perform firmware task - cannot file the firmware file {}.", entity.getTargetFirmwareId().toString() );
					if ( entity.getUpdateState() != UpdateStateEnum.FIRMWARE_UPGRADE_ERROR )
					{
						updateDeviceFirmwareState( deviceId, UpdateStateEnum.FIRMWARE_UPGRADE_ERROR, "" );
					}
					return;
				}

				String targetVersion = firmwareStorage.getProperty( "FIRMWARE_VERSION" );
				LOG.debug( "handlesIPCameraUpgradeEvent, device version: {}, target version: {}.", deviceSWVersion, targetVersion );

				if ( CommonUtils.compareVersions( deviceSWVersion, targetVersion ) == 0 )
				{
					LOG.debug( "Send IP camera {} FIRMWARE_UPGRADE_COMPLETED event.", deviceId );
					updateDeviceFirmwareState( deviceId, UpdateStateEnum.FIRMWARE_UPGRADE_COMPLETED, targetVersion );

					removeChanneFromUpgradeTask( deviceId );
				}
			}
			catch ( FileStorageException e )
			{
				LOG.error( e.getMessage() );
			}
		}
	}

	private void setUpdateTaskInfo( String deviceId, String firmwareId )
	{
		UpgradeTaskInfo taskInfo = getUpgradeTaskInfo( deviceId, firmwareId );
		if ( taskInfo != null )
		{
			taskInfo.setUpgradeStartTime( 0L );
			taskInfo.setUpgradeTaskId( "" );
		}
		else
		{
			currentUpgradeTasks.add( new UpgradeTaskInfo( deviceId, firmwareId ) );
		}
	}

	private void startNextCamerUpgrade( String rootDeviceId )
	{
		List<ChannelGroupFirmware> channelFirmwareList = new ArrayList();
		List<FirmwareEntity> entities = firmwareDAO.findAllReadyUpgrades();

		for ( FirmwareEntity entity : entities )
		{
			Firmware firmware = entity.toDataObject();
			DeviceResource deviceResource = getTopologyService().getDeviceResourceByDeviceId( firmware.getDeviceId() );
			if ( ( deviceResource != null ) && ( !deviceResource.isRootDevice() ) && ( deviceResource.getDeviceView().getParentDeviceId().equals( rootDeviceId ) ) )
			{
				if ( ( ( UpdateTypeEnum.IMMEDIATE == entity.getUpdateType() ) && ( UpdateStateEnum.FIRMWARE_UPGRADE_IDLE == entity.getUpdateState() ) ) || ( ( UpdateTypeEnum.SCHEDULED == entity.getUpdateType() ) && ( UpdateStateEnum.FIRMWARE_UPGRADE_WAITING == entity.getUpdateState() ) ) )
				{
					prepareFirmwareTask( firmware, channelFirmwareList );
				}
			}
		}
		LOG.debug( "startNextCamerUpgrade - total ready upgrades: {}.", Integer.valueOf( channelFirmwareList.size() ) );

		if ( channelFirmwareList.size() > 0 )
		{
			performMultipleChannelFirmwareTask( ( ChannelGroupFirmware ) channelFirmwareList.get( 0 ) );
		}
	}

	public void removeChanneFromUpgradeTask( String deviceId )
	{
		for ( Iterator i$ = currentUpgradeTasks.iterator(); i$.hasNext(); )
		{
			UpgradeTaskInfo task = ( UpgradeTaskInfo ) i$.next();
			List<String> channelIds = task.getChannelDeviceIds();
			if ( ( channelIds != null ) && ( channelIds.contains( deviceId ) ) )
			{
				channelIds.remove( deviceId );
				LOG.debug( "IP camera {} has removed from currentUpgradeTasks list.", deviceId );
				if ( !channelIds.isEmpty() )
					break;
				currentUpgradeTasks.remove( task );
			}
		}
	}

	public boolean isUpgradeInProgress( String deviceId )
	{
		Firmware firmware = getDeviceFirmware( deviceId );

		if ( ( firmware != null ) && ( ( firmware.getUpdateState() == UpdateStateEnum.FIRMWARE_UPGRADE_PENDING ) || ( firmware.getUpdateState() == UpdateStateEnum.FIRMWARE_UPGRADE_ACCEPTED ) || ( firmware.getUpdateState() == UpdateStateEnum.FIRMWARE_UPGRADE_WAITING_ACCEPT ) ) )
		{

			return true;
		}

		DeviceResource dResource = getTopologyService().getDeviceResourceByDeviceId( deviceId );
		DeviceView dView = dResource.getDeviceView();

		if ( !dResource.isRootDevice() )
		{
			String parentDeviceId = dView.getParentDeviceId();

			if ( ( getTopologyService().getDeviceResourceByDeviceId( parentDeviceId ).getDeviceView().isR5() ) && ( isRootDeviceUpgrading( parentDeviceId ) ) )
			{
				return true;
			}

		}
		else if ( isChildDeviceUpgrading( deviceId, true ) )
		{
			return true;
		}

		return false;
	}

	private long getDeviceTimeoutSetting( String deviceId )
	{
		DeviceResource deviceResource = getTopologyService().getDeviceResourceByDeviceId( deviceId );
		DeviceView device = deviceResource.getDeviceView();
		if ( DeviceManagementConstants.isMobileDevice( device.getFamily(), device.getModel() ) )
		{
			return mobileDeviceUpgradeTimeout;
		}
		return fixedDeviceUpgradeTimeout;
	}

	private void processDeviceTimeout( String deviceId )
	{
		FirmwareEntity entity = firmwareDAO.findByDeviceId( deviceId );
		if ( entity == null )
		{
			return;
		}

		if ( entity.getUpdateState() == UpdateStateEnum.FIRMWARE_UPGRADE_ACCEPTED )
		{
			LOG.warn( "Error: Device {} upgrade time out.", deviceId );
			updateDeviceFirmwareState( deviceId, UpdateStateEnum.FIRMWARE_UPGRADE_ERROR, "" );
		}
	}

	public void checkUpgradeTaskTimeout()
	{
		for ( int index = currentUpgradeTasks.size() - 1; index >= 0; index-- )
		{
			UpgradeTaskInfo taskInfo = ( UpgradeTaskInfo ) currentUpgradeTasks.get( index );
			if ( taskInfo.getUpgradeTimeElapsed() > getDeviceTimeoutSetting( taskInfo.getDeviceId() ) )
			{
				List<String> channelDeviceIds = taskInfo.getChannelDeviceIds();
				if ( channelDeviceIds != null )
				{
					for ( String channelDeviceId : channelDeviceIds )
					{
						processDeviceTimeout( channelDeviceId );
					}
				}
				else
				{
					processDeviceTimeout( taskInfo.getDeviceId() );
				}

				currentUpgradeTasks.remove( index );
			}
		}

		LOG.debug( "DU: end of checkUpgradeTaskTimeout, total current upgrade tasks: " + currentUpgradeTasks.size() );
	}

	private UpgradeTaskInfo getUpgradeTaskInfo( String deviceId, String firmwareId )
	{
		for ( UpgradeTaskInfo task : currentUpgradeTasks )
		{
			if ( ( ( task.getDeviceId().equals( deviceId ) ) || ( ( task.getChannelDeviceIds() != null ) && ( task.getChannelDeviceIds().contains( deviceId ) ) ) ) && ( task.getFirmwareId().equals( firmwareId ) ) )
			{
				return task;
			}
		}

		return null;
	}

	public ResourceTopologyServiceIF getTopologyService()
	{
		if ( topologyService == null )
		{
			topologyService = ( ( ResourceTopologyServiceIF ) ApplicationContextSupport.getBean( "resourceTopologyServiceProxy_internal" ) );
		}
		return topologyService;
	}

	public boolean isScheduleInUse( Long id )
	{
		return firmwareDAO.isScheduleInUse( id );
	}

	public void setFileStorageService( FileStorageService fileStorageService )
	{
		this.fileStorageService = fileStorageService;
	}

	public void setDeviceService( DeviceService deviceService )
	{
		this.deviceService = deviceService;
	}

	public void setEventRegistry( EventRegistry eventRegistry )
	{
		this.eventRegistry = eventRegistry;
	}

	public void setConfigService( ConfigService configservice )
	{
		configService = configservice;
	}

	public void setFirmwareDAO( FirmwareDAO dao )
	{
		firmwareDAO = dao;
	}

	public void setGroupFirmwareDAO( GroupFirmwareDAO dao )
	{
		groupFirmwareDAO = dao;
	}

	public void setTaskScheduler( TaskScheduler taskScheduler )
	{
		this.taskScheduler = taskScheduler;
	}

	public void setScheduleService( ScheduleService scheduleService )
	{
		this.scheduleService = scheduleService;
	}

	public void setDeviceCapabilityService( DeviceCapabilityService deviceCapabilityService )
	{
		this.deviceCapabilityService = deviceCapabilityService;
	}

	public void setDeferredEventPool( DeferredEventPool deferredEventPool )
	{
		this.deferredEventPool = deferredEventPool;
	}

	public void setCommonConfig( CommonConfiguration commonConfig )
	{
		this.commonConfig = commonConfig;
	}
}

