package com.marchnetworks.management.instrumentation.adaptation;

import com.marchnetworks.management.instrumentation.DeviceEventMessageInterceptor;
import com.marchnetworks.management.instrumentation.DeviceEventMessageInterceptorRegistry;
import com.marchnetworks.management.instrumentation.events.AbstractDeviceEvent;
import com.marchnetworks.server.event.EventRegistry;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceEventHandlerImpl implements DeviceEventHandler
{
	private static final Logger LOG = LoggerFactory.getLogger( DeviceEventHandlerImpl.class );

	private DeviceEventMessageInterceptorRegistry deviceEventInterceptorRegistry;
	private EventRegistry eventRegistry;

	public void handleEvent( String deviceId, AbstractDeviceEvent event )
	{
		List<DeviceEventMessageInterceptor> interceptors = deviceEventInterceptorRegistry.getInterceptors();
		for ( DeviceEventMessageInterceptor interceptor : interceptors )
		{
			boolean proceed = interceptor.doInterceptDeviceEvent( event );

			if ( !proceed )
			{
				LOG.warn( "Event {} was intercepted and will not continue in the processing chain.", event.getEventType().toString() );
				return;
			}
		}

		eventRegistry.send( event );
	}

	public void setDeviceEventInterceptorRegistry( DeviceEventMessageInterceptorRegistry deviceEventInterceptorRegistry )
	{
		this.deviceEventInterceptorRegistry = deviceEventInterceptorRegistry;
	}

	public void setEventRegistry( EventRegistry eventRegistry )
	{
		this.eventRegistry = eventRegistry;
	}
}

