package com.marchnetworks.command.spring.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class SessionAuthenticationToken extends AbstractAuthenticationToken
{
	private static final long serialVersionUID = 4026821642720220913L;
	private Object principal;
	private String sessionId;

	public SessionAuthenticationToken( String sessionId )
	{
		super( null );
		this.sessionId = sessionId;
		setAuthenticated( false );
	}

	public SessionAuthenticationToken( Object principal, String sessionId, Collection<GrantedAuthority> authorities )
	{
		super( authorities );
		this.principal = principal;
		this.sessionId = sessionId;
		setAuthenticated( true );
	}

	public Object getCredentials()
	{
		return "";
	}

	public Object getPrincipal()
	{
		return principal;
	}

	public String getSessionId()
	{
		return sessionId;
	}
}
