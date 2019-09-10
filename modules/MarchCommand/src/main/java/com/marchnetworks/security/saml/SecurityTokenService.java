package com.marchnetworks.security.saml;

import com.marchnetworks.command.api.security.SamlException;

public abstract interface SecurityTokenService
{
	public abstract String getUserSecurityToken( Long paramLong1, String paramString, Long paramLong2 ) throws SamlException;

	public abstract String getUserSecurityToken( String paramString1, String paramString2 ) throws SamlException;

	public abstract String getServerSecurityToken( String paramString ) throws SamlException;

	public abstract String getServerSecurityToken( String paramString, int paramInt ) throws SamlException;
}

