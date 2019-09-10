package com.marchnetworks.management.instrumentation;

import java.util.List;

public abstract interface DeviceCapabilityService
{
	public abstract boolean isCapabilityEnabled( long paramLong, String paramString );

	public abstract boolean isCapabilityEnabled( long paramLong, String paramString, boolean paramBoolean );

	public abstract void updateCapabilities( long paramLong, List<String> paramList );

	public abstract void clearCapabilities( long paramLong );

	public abstract void refreshDeviceCapabilities( long paramLong );
}

