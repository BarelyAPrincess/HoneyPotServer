package com.marchnetworks.management.instrumentation.comm;

import com.marchnetworks.common.event.Event;
import com.marchnetworks.common.transport.datamodel.DeviceException;
import com.marchnetworks.management.instrumentation.DeviceService;
import com.marchnetworks.management.instrumentation.events.DeviceSystemChangedEvent;
import com.marchnetworks.server.event.EventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemChangedEventHandler implements EventListener
{
	private static final Logger LOG = LoggerFactory.getLogger( SystemChangedEventHandler.class );

	private DeviceService deviceService;

	public String getListenerName()
	{
		return SystemChangedEventHandler.class.getSimpleName();
	}

	public void process( Event aEvent )
	{
		if ( ( aEvent instanceof DeviceSystemChangedEvent ) )
		{
			DeviceSystemChangedEvent scEvent = ( DeviceSystemChangedEvent ) aEvent;
			try
			{
				deviceService.updateDeviceDetails( scEvent.getDeviceId() );
			}
			catch ( DeviceException e )
			{
				LOG.info( "Could not update device details of deviceId {}. Error details:{}", scEvent.getDeviceId(), e.getDetailedErrorMessage() );
			}
		}
	}

	public void setDeviceService( DeviceService deviceService )
	{
		this.deviceService = deviceService;
	}
}

