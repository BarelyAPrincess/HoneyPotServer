package com.marchnetworks.command.api.security;

public interface SessionCoreService
{
	UserInformation veryifyUser( String paramString );

	void deleteSession( String paramString );
}
