package com.marchnetworks.management.instrumentation;

import com.marchnetworks.command.common.topology.data.DeviceResource;

public abstract interface BaseDeviceTask extends Runnable
{
	public abstract void setDeviceId( String paramString );

	public abstract void setDeviceResource( DeviceResource paramDeviceResource );

	public abstract void setStartTime( long paramLong );
}

