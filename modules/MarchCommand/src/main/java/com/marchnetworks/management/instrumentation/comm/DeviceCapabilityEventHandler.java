package com.marchnetworks.management.instrumentation.comm;

import com.marchnetworks.command.common.device.data.ConnectState;
import com.marchnetworks.command.common.device.data.RegistrationStatus;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.management.instrumentation.DeviceCapabilityService;
import com.marchnetworks.management.instrumentation.events.AbstractDeviceEvent;
import com.marchnetworks.management.instrumentation.events.DeviceConnectionStateChangeEvent;
import com.marchnetworks.management.instrumentation.events.DeviceRegistrationEvent;
import com.marchnetworks.management.instrumentation.events.DeviceRestartEvent;
import com.marchnetworks.server.event.EventListener;

public class DeviceCapabilityEventHandler implements EventListener
{
	private DeviceCapabilityService deviceCapabilityService;

	public void process( Event event )
	{
		if ( ( event instanceof DeviceRegistrationEvent ) )
		{
			DeviceRegistrationEvent dre = ( DeviceRegistrationEvent ) event;
			if ( dre.getRegistrationStatus().equals( RegistrationStatus.UNREGISTERED ) )
			{
				deviceCapabilityService.clearCapabilities( Long.valueOf( dre.getDeviceId() ).longValue() );
			}
		}
		else if ( ( event instanceof DeviceRestartEvent ) )
		{
			AbstractDeviceEvent dscEvent = ( AbstractDeviceEvent ) event;
			deviceCapabilityService.refreshDeviceCapabilities( Long.valueOf( dscEvent.getDeviceId() ).longValue() );
		}
		else if ( ( event instanceof DeviceConnectionStateChangeEvent ) )
		{
			DeviceConnectionStateChangeEvent connectionStateEvent = ( DeviceConnectionStateChangeEvent ) event;
			if ( ( connectionStateEvent.getConnectState() != null ) && ( connectionStateEvent.getConnectState().equals( ConnectState.OFFLINE ) ) )
			{
				return;
			}
			deviceCapabilityService.refreshDeviceCapabilities( Long.valueOf( connectionStateEvent.getDeviceId() ).longValue() );
		}
	}

	public String getListenerName()
	{
		return DeviceCapabilityEventHandler.class.getSimpleName();
	}

	public void setDeviceCapabilityService( DeviceCapabilityService deviceCapabilityService )
	{
		this.deviceCapabilityService = deviceCapabilityService;
	}
}

