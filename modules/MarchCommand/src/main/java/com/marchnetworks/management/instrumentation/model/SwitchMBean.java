package com.marchnetworks.management.instrumentation.model;

import com.marchnetworks.command.common.device.data.SwitchState;
import com.marchnetworks.command.common.device.data.SwitchType;
import com.marchnetworks.command.common.transport.data.Pair;

public abstract interface SwitchMBean
{
	public abstract String getSwitchId();

	public abstract String getName();

	public abstract SwitchType getType();

	public abstract SwitchState getState();

	public abstract String getSwitchDeviceId();

	public abstract String getSwitchDeviceAddress();

	public abstract Long getDeviceId();

	public abstract Pair[] getInfoAsPairs();
}

