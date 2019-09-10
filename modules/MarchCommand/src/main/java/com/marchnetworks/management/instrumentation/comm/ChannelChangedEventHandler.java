package com.marchnetworks.management.instrumentation.comm;

import com.marchnetworks.common.event.Event;
import com.marchnetworks.management.instrumentation.DeviceService;
import com.marchnetworks.management.instrumentation.events.DeviceChannelChangedEvent;
import com.marchnetworks.server.event.EventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelChangedEventHandler implements EventListener
{
	private static final Logger LOG = LoggerFactory.getLogger( ChannelChangedEventHandler.class );

	private DeviceService deviceService;

	public String getListenerName()
	{
		return ChannelChangedEventHandler.class.getSimpleName();
	}

	public void process( Event aEvent )
	{
		if ( ( aEvent instanceof DeviceChannelChangedEvent ) )
		{
			DeviceChannelChangedEvent ev = ( DeviceChannelChangedEvent ) aEvent;

			LOG.debug( "Processing channel changed event" );
			deviceService.updateChannelFromDevice( ev.getDeviceId(), ev.getChannelId() );
		}
	}

	public void setDeviceService( DeviceService deviceService )
	{
		this.deviceService = deviceService;
	}
}

