package com.marchnetworks.management.instrumentation.comm;

import com.marchnetworks.command.common.device.data.ChannelState;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.management.instrumentation.DeviceService;
import com.marchnetworks.management.instrumentation.events.ChannelConnectionStateEvent;
import com.marchnetworks.server.event.EventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelStateEventHandler implements EventListener
{
	private static final Logger LOG = LoggerFactory.getLogger( ChannelStateEventHandler.class );

	private DeviceService deviceService;

	public void process( Event aEvent )
	{
		if ( ( aEvent instanceof ChannelConnectionStateEvent ) )
		{
			ChannelConnectionStateEvent ev = ( ChannelConnectionStateEvent ) aEvent;
			deviceService.updateChannelState( ev.getDeviceId(), ev.getChannelId(), ev.getConnectionState().name() );
		}
		else
		{
			LOG.warn( "Not a DeviceConnectionStateNotification" );
		}
	}

	public String getListenerName()
	{
		return ChannelStateEventHandler.class.getSimpleName();
	}

	public void setDeviceService( DeviceService deviceService )
	{
		this.deviceService = deviceService;
	}
}

