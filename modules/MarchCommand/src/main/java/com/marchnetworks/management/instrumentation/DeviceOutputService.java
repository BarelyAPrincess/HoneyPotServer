package com.marchnetworks.management.instrumentation;

import com.marchnetworks.management.instrumentation.events.DeviceOutputEvent;
import com.marchnetworks.management.instrumentation.model.DeviceOutputMBean;

public abstract interface DeviceOutputService
{
	public abstract DeviceOutputMBean getDeviceOutputById( Long paramLong );

	public abstract void processDeviceOutputEvent( DeviceOutputEvent paramDeviceOutputEvent );

	public abstract void processDeviceRegistration( Long paramLong );

	public abstract void processDeviceUnregistration( Long paramLong );
}

