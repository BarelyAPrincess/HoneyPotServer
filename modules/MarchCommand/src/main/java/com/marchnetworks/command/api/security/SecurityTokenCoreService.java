package com.marchnetworks.command.api.security;

public abstract interface SecurityTokenCoreService
{
	public abstract String getServerSecurityToken( String paramString, int paramInt ) throws SamlException;
}
