package com.marchnetworks.management.instrumentation;

import com.marchnetworks.command.common.topology.data.DeviceResource;

public abstract interface DeviceAdaptor
{
	public abstract void setDeviceResource( DeviceResource paramDeviceResource );

	public abstract DeviceResource getDeviceResource();
}

