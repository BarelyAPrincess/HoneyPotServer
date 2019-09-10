package com.marchnetworks.security.authentication;

import com.marchnetworks.command.api.security.SessionCoreService;
import com.marchnetworks.command.api.security.UserInformation;

import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;

import java.util.List;

public class SessionServiceImpl implements SessionCoreService, SessionService
{
	private SessionRegistry sessionRegistry;

	public UserInformation veryifyUser( String sessionId )
	{
		SessionInformation sessionInfo = sessionRegistry.getSessionInformation( sessionId );

		UserInformation result = null;
		if ( sessionInfo != null )
		{
			result = ( UserInformation ) sessionInfo.getPrincipal();
		}
		return result;
	}

	public void deleteAllSessions( boolean expireOnly )
	{
		List<Object> principals = sessionRegistry.getAllPrincipals();
		for ( Object p : principals )
		{
			List<SessionInformation> sessions = sessionRegistry.getAllSessions( p, true );
			for ( SessionInformation session : sessions )
			{
				if ( expireOnly )
				{
					session.expireNow();
				}
				else
				{
					sessionRegistry.removeSessionInformation( session.getSessionId() );
				}
			}
		}
	}

	public int getTotalSessions()
	{
		int result = 0;
		List<Object> principals = sessionRegistry.getAllPrincipals();
		for ( Object p : principals )
		{
			List<SessionInformation> sessions = sessionRegistry.getAllSessions( p, true );
			result += sessions.size();
		}
		return result;
	}

	public void deleteSessionsByPrincipalName( String username )
	{
		List<Object> principals = sessionRegistry.getAllPrincipals();

		for ( Object p : principals )
		{
			if ( ( p instanceof UserInformation ) )
			{
				UserInformation userInfo = ( UserInformation ) p;
				if ( userInfo.getName().equals( username ) )
				{
					List<SessionInformation> sessions = sessionRegistry.getAllSessions( p, true );
					for ( SessionInformation session : sessions )
					{
						session.expireNow();
						sessionRegistry.removeSessionInformation( session.getSessionId() );
					}
				}
			}
		}
	}

	public void deleteSession( String sessionId )
	{
		SessionInformation session = sessionRegistry.getSessionInformation( sessionId );
		if ( session != null )
		{
			session.expireNow();
			sessionRegistry.removeSessionInformation( sessionId );
		}
	}

	public void setSessionRegistry( SessionRegistry sessionRegistry )
	{
		this.sessionRegistry = sessionRegistry;
	}
}

