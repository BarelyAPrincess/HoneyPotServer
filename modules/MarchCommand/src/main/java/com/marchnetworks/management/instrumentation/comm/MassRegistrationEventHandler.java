package com.marchnetworks.management.instrumentation.comm;

import com.marchnetworks.command.common.device.data.MassRegistrationInfo;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.management.instrumentation.DeviceService;
import com.marchnetworks.management.instrumentation.events.MassRegistrationEvent;
import com.marchnetworks.server.event.EventListener;

import java.util.List;

public class MassRegistrationEventHandler implements EventListener
{
	public DeviceService deviceService;

	public void process( Event event )
	{
		if ( ( event instanceof MassRegistrationEvent ) )
		{
			MassRegistrationEvent massRegistrationEvent = ( MassRegistrationEvent ) event;
			List<MassRegistrationInfo> devices = massRegistrationEvent.getMassRegistrationInfo();
			deviceService.massRegister( devices );
		}
	}

	public String getListenerName()
	{
		return MassRegistrationEvent.class.getSimpleName();
	}

	public void setDeviceService( DeviceService deviceService )
	{
		this.deviceService = deviceService;
	}
}

