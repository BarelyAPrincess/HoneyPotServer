package com.marchnetworks.management.instrumentation.comm;

import com.marchnetworks.command.common.device.data.RegistrationStatus;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.management.instrumentation.DeviceSwitchService;
import com.marchnetworks.management.instrumentation.events.DeviceRegistrationEvent;
import com.marchnetworks.management.instrumentation.events.DeviceSwitchEvent;
import com.marchnetworks.server.event.EventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceSwitchEventHandler implements EventListener
{
	private DeviceSwitchService switchService;
	private static final Logger LOG = LoggerFactory.getLogger( DeviceSwitchEventHandler.class );

	public void process( Event event )
	{
		if ( ( event instanceof DeviceRegistrationEvent ) )
		{
			DeviceRegistrationEvent deviceRegistrationEvent = ( DeviceRegistrationEvent ) event;
			RegistrationStatus status = deviceRegistrationEvent.getRegistrationStatus();

			if ( status == RegistrationStatus.UNREGISTERED )
			{
				switchService.processDeviceUnregistration( Long.valueOf( deviceRegistrationEvent.getDeviceId() ) );
			}
			else if ( status == RegistrationStatus.REGISTERED )
			{
				long start = System.currentTimeMillis();
				switchService.processDeviceRegistration( Long.valueOf( deviceRegistrationEvent.getDeviceId() ) );
				long end = System.currentTimeMillis();
				LOG.debug( "Post-Registration time for DeviceSwitchEventHandler: " + ( end - start ) + " ms." );
			}
		}
		else if ( ( event instanceof DeviceSwitchEvent ) )
		{
			DeviceSwitchEvent switchEvent = ( DeviceSwitchEvent ) event;
			switchService.processDeviceOutputEvent( switchEvent );
		}
	}

	public String getListenerName()
	{
		return DeviceSwitchEventHandler.class.getSimpleName();
	}

	public void setSwitchService( DeviceSwitchService switchService )
	{
		this.switchService = switchService;
	}
}

