package com.marchnetworks.management.instrumentation.events;

import com.marchnetworks.command.common.transport.data.Pair;

public abstract interface DeviceOutputEvent
{
	public abstract String getDeviceId();

	public abstract DeviceOutputEventType getType();

	public abstract String getDeviceOutputId();

	public abstract String getState();

	public abstract Pair[] getExtraInfo();
}

