package com.marchnetworks.management.instrumentation.model;

import com.marchnetworks.management.instrumentation.data.DeviceNetworkInfoType;

public abstract interface DeviceNetworkInfoMBean
{
	public abstract DeviceNetworkInfoType getNetworkInfoType();

	public abstract String getValue();
}

