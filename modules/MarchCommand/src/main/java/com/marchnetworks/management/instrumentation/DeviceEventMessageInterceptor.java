package com.marchnetworks.management.instrumentation;

import com.marchnetworks.management.instrumentation.events.AbstractDeviceEvent;

public abstract interface DeviceEventMessageInterceptor
{
	public abstract boolean doInterceptDeviceEvent( AbstractDeviceEvent paramAbstractDeviceEvent );
}

