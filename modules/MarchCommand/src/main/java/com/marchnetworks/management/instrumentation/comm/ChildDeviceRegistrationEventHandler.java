package com.marchnetworks.management.instrumentation.comm;

import com.marchnetworks.common.event.Event;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.instrumentation.DeviceService;
import com.marchnetworks.management.instrumentation.events.DeviceChannelAddedEvent;
import com.marchnetworks.management.instrumentation.events.DeviceChannelRemovedEvent;
import com.marchnetworks.server.event.EventListener;

public class ChildDeviceRegistrationEventHandler implements EventListener
{
	public String getListenerName()
	{
		return ChildDeviceRegistrationEventHandler.class.getSimpleName();
	}

	public void process( Event event )
	{
		if ( ( event instanceof DeviceChannelAddedEvent ) )
		{
			DeviceChannelAddedEvent channelAdded = ( DeviceChannelAddedEvent ) event;
			getDeviceService().addChannelToDevice( channelAdded.getDeviceId(), channelAdded.getChannelId() );
		}
		else if ( ( event instanceof DeviceChannelRemovedEvent ) )
		{
			DeviceChannelRemovedEvent channelRemoved = ( DeviceChannelRemovedEvent ) event;
			getDeviceService().removeChannelFromDevice( channelRemoved.getDeviceId(), channelRemoved.getChannelId() );
		}
	}

	private DeviceService getDeviceService()
	{
		return ( DeviceService ) ApplicationContextSupport.getBean( "deviceService" );
	}
}

