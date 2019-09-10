package com.marchnetworks.management.instrumentation;

import com.marchnetworks.command.common.device.data.RegistrationStatus;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.management.instrumentation.events.ChildDeviceRegistrationEvent;
import com.marchnetworks.management.instrumentation.events.DeviceRegistrationEvent;
import com.marchnetworks.server.event.EventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceRemovalHandler implements EventListener
{
	private static final Logger LOG = LoggerFactory.getLogger( DeviceRemovalHandler.class );

	private DeviceRegistry deviceRegistry;

	public String getListenerName()
	{
		return DeviceRemovalHandler.class.getSimpleName();
	}

	public void process( Event event )
	{
		RegistrationStatus status = null;
		String deviceId = null;
		if ( ( event instanceof DeviceRegistrationEvent ) )
		{
			LOG.debug( "Processing event {}", event );
			DeviceRegistrationEvent deviceEvent = ( DeviceRegistrationEvent ) event;
			status = deviceEvent.getRegistrationStatus();
			deviceId = deviceEvent.getDeviceId();
		}
		else if ( ( event instanceof ChildDeviceRegistrationEvent ) )
		{
			ChildDeviceRegistrationEvent childDeviceEvent = ( ChildDeviceRegistrationEvent ) event;
			status = childDeviceEvent.getRegistrationStatus();
			deviceId = childDeviceEvent.getDeviceId();
		}

		if ( status.equals( RegistrationStatus.UNREGISTERED ) )
		{
			LOG.debug( "Handling {} event for device {}.", new Object[] {event, deviceId} );
			deviceRegistry.removeDevice( deviceId );
			if ( ( event instanceof DeviceRegistrationEvent ) )
			{
				LOG.info( "Device {} removed.", deviceId );
			}
		}
	}

	public void setDeviceRegistry( DeviceRegistry deviceRegistry )
	{
		this.deviceRegistry = deviceRegistry;
	}
}

