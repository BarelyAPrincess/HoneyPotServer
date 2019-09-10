package com.marchnetworks.management.instrumentation.adaptation;

import com.marchnetworks.management.instrumentation.events.AbstractDeviceEvent;

public abstract interface DeviceEventHandler
{
	public abstract void handleEvent( String paramString, AbstractDeviceEvent paramAbstractDeviceEvent );
}

