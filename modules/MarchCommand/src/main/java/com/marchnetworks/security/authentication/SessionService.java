package com.marchnetworks.security.authentication;

import com.marchnetworks.command.api.security.UserInformation;

public abstract interface SessionService
{
	public abstract UserInformation veryifyUser( String paramString );

	public abstract void deleteAllSessions( boolean paramBoolean );

	public abstract void deleteSessionsByPrincipalName( String paramString );

	public abstract int getTotalSessions();
}

