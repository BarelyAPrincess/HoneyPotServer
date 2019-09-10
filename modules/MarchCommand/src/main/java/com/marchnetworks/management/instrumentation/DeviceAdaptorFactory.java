package com.marchnetworks.management.instrumentation;

import com.marchnetworks.command.common.topology.data.DeviceResource;

public abstract interface DeviceAdaptorFactory
{
	public abstract <T extends RemoteDeviceOperations> T getDeviceAdaptor( DeviceResource paramDeviceResource );
}

