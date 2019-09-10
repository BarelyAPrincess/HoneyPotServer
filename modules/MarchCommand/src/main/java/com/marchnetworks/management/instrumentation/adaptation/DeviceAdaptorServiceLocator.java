package com.marchnetworks.management.instrumentation.adaptation;

import com.marchnetworks.management.instrumentation.DeviceAdaptor;

public abstract interface DeviceAdaptorServiceLocator
{
	public abstract DeviceAdaptor getDeviceAdaptor( String paramString );
}

