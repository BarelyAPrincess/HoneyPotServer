package com.marchnetworks.management.config;

import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.config.events.ConfigNotificationEvent;
import com.marchnetworks.management.config.events.ConfigNotificationReasonCode;
import com.marchnetworks.management.config.events.ConfigNotificationType;
import com.marchnetworks.management.config.events.DeviceConfigAppliedNotification;
import com.marchnetworks.management.config.events.DeviceConfigAssociationNotification;
import com.marchnetworks.management.config.events.DeviceConfigFailedNotification;
import com.marchnetworks.management.config.events.DeviceConfigUnregistrationNotification;
import com.marchnetworks.management.config.events.DeviceConfigUpgradeNotification;
import com.marchnetworks.management.config.model.DeviceConfig;
import com.marchnetworks.management.config.service.DeviceConfigDescriptor;
import com.marchnetworks.management.instrumentation.model.DeviceMBean;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;
import com.marchnetworks.server.event.EventRegistry;

import java.util.Collections;
import java.util.Set;

public abstract class BaseConfigHandler
{
	protected EventRegistry eventRegistry;
	protected ResourceTopologyServiceIF topologyService;

	protected void sendConfigAssociationNotificationEvent( String deviceId, DeviceConfig devConfig, ConfigNotificationType type, ConfigNotificationReasonCode reason )
	{
		DeviceConfigAssociationNotification devConfigEvent = new DeviceConfigAssociationNotification( deviceId, devConfig.getId(), type );

		devConfigEvent.setImageState( devConfig.getAssignState() );
		devConfigEvent.setSnapshotState( devConfig.getSnapshotState() );
		devConfigEvent.setReasonCode( reason );
		devConfigEvent.setFirmwareVersionInfo( devConfig.toDataObject().getFirmwareVersion() );
		ConfigNotificationEvent event = new ConfigNotificationEvent( devConfigEvent, getTerritoryInfoForEvent( deviceId ) );
		eventRegistry.sendEventAfterTransactionCommits( event );
	}

	protected void sendConfigUnregistrationNotificationEvent( DeviceMBean device, DeviceConfig devConfig, ConfigNotificationType type, ConfigNotificationReasonCode reason )
	{
		DeviceConfigUnregistrationNotification devConfigEvent = new DeviceConfigUnregistrationNotification( device.getDeviceId(), devConfig.getId(), type );

		devConfigEvent.setImageState( devConfig.getAssignState() );
		devConfigEvent.setSnapshotState( devConfig.getSnapshotState() );
		devConfigEvent.setReasonCode( reason );
		devConfigEvent.setFirmwareVersionInfo( devConfig.toDataObject().getFirmwareVersion() );
		ConfigNotificationEvent event = new ConfigNotificationEvent( devConfigEvent, getTerritoryInfoForEvent( device.getDeviceId() ) );
		eventRegistry.sendEventAfterTransactionCommits( event );
	}

	protected void sendConfigFailedNotificationEvent( DeviceMBean device, DeviceConfig devConfig, ConfigNotificationType type, ConfigNotificationReasonCode reason )
	{
		DeviceConfigFailedNotification devConfigEvent = new DeviceConfigFailedNotification( device.getDeviceId(), devConfig.getId(), type );

		devConfigEvent.setImageState( devConfig.getAssignState() );
		devConfigEvent.setSnapshotState( devConfig.getSnapshotState() );
		devConfigEvent.setReasonCode( reason );
		devConfigEvent.setFirmwareVersionInfo( devConfig.toDataObject().getFirmwareVersion() );
		ConfigNotificationEvent event = new ConfigNotificationEvent( devConfigEvent, getTerritoryInfoForEvent( device.getDeviceId() ) );
		eventRegistry.sendEventAfterTransactionCommits( event );
	}

	protected void sendConfigAppliedNotificationEvent( DeviceMBean device, DeviceConfig devConfig, ConfigNotificationType type, ConfigNotificationReasonCode reason )
	{
		DeviceConfigAppliedNotification devConfigEvent = new DeviceConfigAppliedNotification( device.getDeviceId(), devConfig.getId(), type );

		devConfigEvent.setImageState( devConfig.getAssignState() );
		devConfigEvent.setSnapshotState( devConfig.getSnapshotState() );
		devConfigEvent.setReasonCode( reason );
		devConfigEvent.setFirmwareVersionInfo( devConfig.toDataObject().getFirmwareVersion() );
		ConfigNotificationEvent event = new ConfigNotificationEvent( devConfigEvent, getTerritoryInfoForEvent( device.getDeviceId() ) );
		eventRegistry.sendEventAfterTransactionCommits( event );
	}

	protected void sendConfigUpgradeNotificationEvent( DeviceMBean device, DeviceConfig devConfig, ConfigNotificationType type, ConfigNotificationReasonCode reason )
	{
		DeviceConfigUpgradeNotification devConfigEvent = new DeviceConfigUpgradeNotification( device.getDeviceId(), devConfig.getId(), type );

		devConfigEvent.setImageState( devConfig.getAssignState() );
		devConfigEvent.setSnapshotState( devConfig.getSnapshotState() );
		devConfigEvent.setReasonCode( reason );
		devConfigEvent.setFirmwareVersionInfo( devConfig.toDataObject().getFirmwareVersion() );
		ConfigNotificationEvent event = new ConfigNotificationEvent( devConfigEvent, getTerritoryInfoForEvent( device.getDeviceId() ) );
		eventRegistry.sendEventAfterTransactionCommits( event );
	}

	public EventRegistry getEventRegistry()
	{
		return eventRegistry;
	}

	public void setEventRegistry( EventRegistry eventRegistry )
	{
		this.eventRegistry = eventRegistry;
	}

	public void setTopologyService( ResourceTopologyServiceIF topologyService )
	{
		this.topologyService = topologyService;
	}

	public ResourceTopologyServiceIF getTopologyService()
	{
		if ( topologyService == null )
		{
			topologyService = ( ( ResourceTopologyServiceIF ) ApplicationContextSupport.getBean( "resourceTopologyServiceProxy_internal" ) );
		}
		return topologyService;
	}

	private Set<Long> getTerritoryInfoForEvent( String deviceId )
	{
		return Collections.singleton( getTopologyService().getResourceIdByDeviceId( deviceId ) );
	}
}
