package com.marchnetworks.security.device;

import com.marchnetworks.command.api.security.DeviceSessionException;

public abstract interface DeviceSessionHolderService
{
	public abstract String getNewSessionFromDevice( String paramString1, String paramString2 ) throws DeviceSessionException;

	public abstract String getSessionFromDevice( String paramString1, String paramString2 ) throws DeviceSessionException;

	public abstract void extendSessionForDevice( String paramString );

	public abstract boolean hasValidSession( String paramString );

	public abstract void invalidateAllDeviceSessions( String paramString );

	public abstract void processDeviceUnregistered( String paramString );
}

