package com.marchnetworks.command.api.security;

public interface TokenContributor
{
	AuthorizationContent getAuthorizationContent( String paramString, Long paramLong );
}
