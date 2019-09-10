package com.marchnetworks.command.spring.security;

import com.marchnetworks.command.api.security.SessionCoreService;
import com.marchnetworks.command.api.security.UserInformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;

public class CommandAuthenticationProvider implements AuthenticationProvider
{
	private static final Logger LOG = LoggerFactory.getLogger( CommandAuthenticationProvider.class );
	private SessionCoreService sessionCoreService;

	public Authentication authenticate( Authentication authentication ) throws AuthenticationException
	{
		if ( !( authentication instanceof SessionAuthenticationToken ) )
		{
			return null;
		}
		if ( sessionCoreService == null )
		{
			LOG.error( "No core OSGI service found in CommandAuthenticationProvider, not able to authenticate. Must be set as the coreService bean property." );
			throw new AuthenticationServiceException( "No core OSGI service found in CommandAuthenticationProvider, not able to authenticate. Must be set as the coreService bean property." );
		}

		SessionAuthenticationToken token = ( SessionAuthenticationToken ) authentication;
		String sessionId = token.getSessionId();
		LOG.debug( "Verifying App session Id {}", sessionId );

		UserInformation info = sessionCoreService.veryifyUser( sessionId );

		if ( info == null )
		{
			String errorMessage = "SessionId " + sessionId + " not found when veryfing user";
			LOG.error( errorMessage );
			throw new AuthenticationServiceException( errorMessage );
		}

		List<GrantedAuthority> authorities = new ArrayList();
		for ( String authority : info.getAuthorities() )
		{
			authorities.add( new SimpleGrantedAuthority( authority ) );
		}

		SessionAuthenticationToken result = new SessionAuthenticationToken( info, sessionId, authorities );
		result.setDetails( token.getDetails() );
		result.setAuthenticated( true );
		return result;
	}

	public boolean supports( Class<? extends Object> authentication )
	{
		return SessionAuthenticationToken.class.isAssignableFrom( authentication );
	}

	public void setSessionCoreService( SessionCoreService sessionCoreService )
	{
		this.sessionCoreService = sessionCoreService;
	}
}
