package com.marchnetworks.management.instrumentation.monitor;

import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.instrumentation.DeviceAdaptorFactory;
import com.marchnetworks.management.instrumentation.DeviceEventMessageInterceptor;
import com.marchnetworks.management.instrumentation.DeviceRegistry;
import com.marchnetworks.server.event.EventRegistry;

public abstract class AbstractDeviceMonitor implements DeviceEventMessageInterceptor
{
	private DeviceRegistry deviceRegistry;
	private EventRegistry eventRegistry;

	public void setDeviceRegistry( DeviceRegistry deviceRegistry )
	{
		this.deviceRegistry = deviceRegistry;
	}

	public void setEventRegistry( EventRegistry eventRegistry )
	{
		this.eventRegistry = eventRegistry;
	}

	public DeviceRegistry getDeviceRegistry()
	{
		return deviceRegistry;
	}

	public EventRegistry getEventRegistry()
	{
		return eventRegistry;
	}

	protected DeviceAdaptorFactory getDeviceAdaptorFactory()
	{
		return ( DeviceAdaptorFactory ) ApplicationContextSupport.getBean( "deviceAdaptorFactory" );
	}
}

