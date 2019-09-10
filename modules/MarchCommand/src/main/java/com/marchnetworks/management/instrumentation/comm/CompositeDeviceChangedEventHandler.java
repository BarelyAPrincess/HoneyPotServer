package com.marchnetworks.management.instrumentation.comm;

import com.marchnetworks.common.event.Event;
import com.marchnetworks.management.instrumentation.dao.DeviceDAO;
import com.marchnetworks.management.instrumentation.events.AbstractDeviceEvent;
import com.marchnetworks.management.instrumentation.events.DeviceChannelsInUseEvent;
import com.marchnetworks.management.instrumentation.events.DeviceChannelsMaxEvent;
import com.marchnetworks.management.instrumentation.events.DeviceSystemChangedEvent;
import com.marchnetworks.management.instrumentation.events.DeviceSystemConfigHashEvent;
import com.marchnetworks.management.instrumentation.model.CompositeDevice;
import com.marchnetworks.management.instrumentation.model.Device;
import com.marchnetworks.server.event.EventListener;
import com.marchnetworks.server.event.EventRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompositeDeviceChangedEventHandler implements EventListener
{
	private static final Logger LOG = LoggerFactory.getLogger( CompositeDeviceChangedEventHandler.class );

	private DeviceDAO deviceDAO;

	private EventRegistry eventRegistry;

	public String getListenerName()
	{
		return CompositeDeviceChangedEventHandler.class.getSimpleName();
	}

	public void process( Event event )
	{
		if ( ( event instanceof DeviceSystemConfigHashEvent ) )
		{
			CompositeDevice cd = findDevice( ( ( DeviceSystemConfigHashEvent ) event ).getDeviceId() );
			if ( cd != null )
			{
				LOG.info( "Processing config changed event for CompositeDevice {}", cd.getAddress() );

				AbstractDeviceEvent dne = new DeviceSystemChangedEvent( cd.getDeviceId() );
				eventRegistry.sendEventAfterTransactionCommits( dne );
			}
		}
		else if ( ( event instanceof DeviceChannelsMaxEvent ) )
		{
			DeviceChannelsMaxEvent dcme = ( DeviceChannelsMaxEvent ) event;
			CompositeDevice cd = findDevice( dcme.getDeviceId() );

			if ( cd != null )
			{
				LOG.info( "Updating DeviceId=" + cd.getDeviceId() + " to have ChannelsMax=" + dcme.getChannelsMax() );
				cd.setChannelsMax( Integer.valueOf( dcme.getChannelsMax() ) );
			}
		}
		else if ( ( event instanceof DeviceChannelsInUseEvent ) )
		{
			DeviceChannelsInUseEvent dciue = ( DeviceChannelsInUseEvent ) event;
			CompositeDevice cd = findDevice( dciue.getDeviceId() );

			if ( cd != null )
			{
				LOG.info( "Updating DeviceId=" + cd.getDeviceId() + " to have ChannelsInUse=" + dciue.getInUse() );
				cd.setChannelsInUse( Integer.valueOf( dciue.getInUse() ) );
			}
		}
	}

	protected CompositeDevice findDevice( String deviceId )
	{
		Device device = deviceDAO.findById( deviceId );
		if ( ( device instanceof CompositeDevice ) )
		{
			return ( CompositeDevice ) device;
		}
		return null;
	}

	public void setDeviceDAO( DeviceDAO deviceDAO )
	{
		this.deviceDAO = deviceDAO;
	}

	public void setEventRegistry( EventRegistry eventRegistry )
	{
		this.eventRegistry = eventRegistry;
	}
}

