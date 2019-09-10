package com.marchnetworks.management.instrumentation.model;

import com.marchnetworks.command.common.device.data.DeviceOutputView;

public abstract interface DeviceOutputMBean
{
	public abstract Long getId();

	public abstract DeviceOutputView toDataObject();

	public abstract String getOutputId();

	public abstract String getName();

	public abstract Class<? extends DeviceOutputView> getDataObjectClass();
}

