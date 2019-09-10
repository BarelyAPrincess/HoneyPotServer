package com.marchnetworks.command.api.security;

public abstract interface DeviceSessionCoreService
{
	public abstract String getSessionFromDevice( String paramString1, String paramString2 ) throws DeviceSessionException;

	public abstract String getNewSessionFromDevice( String paramString1, String paramString2 ) throws DeviceSessionException;
}
