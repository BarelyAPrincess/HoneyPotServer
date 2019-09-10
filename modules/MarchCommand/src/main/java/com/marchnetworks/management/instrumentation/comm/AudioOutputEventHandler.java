package com.marchnetworks.management.instrumentation.comm;

import com.marchnetworks.command.common.device.data.RegistrationStatus;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.management.instrumentation.AudioOutputService;
import com.marchnetworks.management.instrumentation.events.DeviceAudioOutputEvent;
import com.marchnetworks.management.instrumentation.events.DeviceRegistrationEvent;
import com.marchnetworks.server.event.EventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AudioOutputEventHandler implements EventListener
{
	private AudioOutputService audioOutputService;
	private static final Logger LOG = LoggerFactory.getLogger( AudioOutputEventHandler.class );

	public void process( Event event )
	{
		if ( ( event instanceof DeviceRegistrationEvent ) )
		{
			long start = System.currentTimeMillis();
			DeviceRegistrationEvent deviceRegistrationEvent = ( DeviceRegistrationEvent ) event;
			RegistrationStatus status = deviceRegistrationEvent.getRegistrationStatus();

			if ( status == RegistrationStatus.UNREGISTERED )
			{
				audioOutputService.processDeviceUnregistration( Long.valueOf( deviceRegistrationEvent.getDeviceId() ) );
			}
			else if ( status == RegistrationStatus.REGISTERED )
			{
				audioOutputService.processDeviceRegistration( Long.valueOf( deviceRegistrationEvent.getDeviceId() ) );
				long end = System.currentTimeMillis();
				LOG.debug( "Post-Registration for AudioOutputEventHandler: " + ( end - start ) + " ms." );
			}
		}
		else if ( ( event instanceof DeviceAudioOutputEvent ) )
		{
			DeviceAudioOutputEvent audioOutputEvent = ( DeviceAudioOutputEvent ) event;
			audioOutputService.processDeviceOutputEvent( audioOutputEvent );
		}
	}

	public String getListenerName()
	{
		return AudioOutputEventHandler.class.getSimpleName();
	}

	public void setAudioOutputService( AudioOutputService audioOutputService )
	{
		this.audioOutputService = audioOutputService;
	}
}

