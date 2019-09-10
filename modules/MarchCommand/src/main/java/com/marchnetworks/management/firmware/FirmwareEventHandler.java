package com.marchnetworks.management.firmware;

import com.marchnetworks.command.common.device.data.RegistrationStatus;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.common.event.EventTypesEnum;
import com.marchnetworks.management.file.events.FileStorageEvent;
import com.marchnetworks.management.firmware.service.FirmwareService;
import com.marchnetworks.management.instrumentation.events.AbstractDeviceConfigurationEvent;
import com.marchnetworks.management.instrumentation.events.AbstractDeviceEvent;
import com.marchnetworks.management.instrumentation.events.ChildDeviceRegistrationEvent;
import com.marchnetworks.management.instrumentation.events.DeviceChannelChangedEvent;
import com.marchnetworks.management.instrumentation.events.DeviceConnectionStateChangeEvent;
import com.marchnetworks.management.instrumentation.events.DeviceDeferredUpgradeEvent;
import com.marchnetworks.management.instrumentation.events.DeviceRegistrationEvent;
import com.marchnetworks.management.instrumentation.events.DeviceSystemChangedEvent;
import com.marchnetworks.schedule.events.ScheduleEvent;
import com.marchnetworks.server.event.EventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FirmwareEventHandler implements EventListener
{
	private static final Logger LOG = LoggerFactory.getLogger( FirmwareEventHandler.class );

	private FirmwareService firmwareService;

	public void process( Event a_Event )
	{
		if ( ( a_Event instanceof AbstractDeviceEvent ) )
		{
			AbstractDeviceEvent deviceEvent = ( AbstractDeviceEvent ) a_Event;
			String deviceId = deviceEvent.getDeviceId();
			LOG.debug( "Processing Device {} Event {}...", deviceId, deviceEvent.getEventType() );
			if ( ( deviceEvent instanceof DeviceSystemChangedEvent ) )
			{
				firmwareService.handleSystemChangedEvent( deviceId );
			}
			else if ( ( deviceEvent instanceof DeviceDeferredUpgradeEvent ) )
			{
				firmwareService.handleDeferredUpgradeEvent( deviceId );
			}
			else if ( ( deviceEvent instanceof AbstractDeviceConfigurationEvent ) )
			{
				firmwareService.handleDeviceUpgradeEvent( ( AbstractDeviceConfigurationEvent ) deviceEvent, deviceId );
			}
			else if ( ( deviceEvent instanceof DeviceChannelChangedEvent ) )
			{
				firmwareService.handleIPCameraUpgradeEvent( deviceId );
			}
			else if ( ( deviceEvent instanceof ChildDeviceRegistrationEvent ) )
			{
				if ( RegistrationStatus.UNREGISTERED == ( ( ChildDeviceRegistrationEvent ) deviceEvent ).getRegistrationStatus() )
				{
					firmwareService.deleteDeviceFirmware( deviceId );
				}
			}
			else if ( ( a_Event instanceof DeviceRegistrationEvent ) )
			{
				firmwareService.handleDeviceRegistrationEvent( ( DeviceRegistrationEvent ) a_Event, deviceId );
			}
			else if ( ( a_Event instanceof DeviceConnectionStateChangeEvent ) )
			{
				firmwareService.handleConnectionStateChnageEvent( ( DeviceConnectionStateChangeEvent ) a_Event );
			}
		}
		else if ( ( a_Event instanceof FileStorageEvent ) )
		{
			if ( ( ( FileStorageEvent ) a_Event ).getEventNotificationType().equals( EventTypesEnum.FIRMWARE_REMOVED.getFullPathEventName() ) )
			{
				firmwareService.onFirmwareFileRemoved( ( ( FileStorageEvent ) a_Event ).getFileStorageId() );
			}
		}
		else if ( ( a_Event instanceof ScheduleEvent ) )
		{
			firmwareService.handleScheduleEvent( ( ScheduleEvent ) a_Event );
		}
	}

	public String getListenerName()
	{
		return FirmwareEventHandler.class.getSimpleName();
	}

	public void setFirmwareService( FirmwareService firmwareService )
	{
		this.firmwareService = firmwareService;
	}
}

