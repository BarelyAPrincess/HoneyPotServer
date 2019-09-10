package com.marchnetworks.security.filter;

import com.marchnetworks.audit.data.AuditEventNameEnum;
import com.marchnetworks.audit.data.AuditView;
import com.marchnetworks.audit.data.AuditView.Builder;
import com.marchnetworks.audit.events.AuditEvent;
import com.marchnetworks.command.api.security.CommandAuthenticationDetails;
import com.marchnetworks.server.event.EventRegistry;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class UserAuthenticationAuditorFilter extends GenericFilterBean
{
	private static final String AUTH_OBJECT_AUDITED_TAG = "user.login";
	private EventRegistry eventRegistry;

	public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain ) throws IOException, ServletException
	{
		Authentication authObject = SecurityContextHolder.getContext().getAuthentication();
		if ( ( ( authObject instanceof UsernamePasswordAuthenticationToken ) ) && ( authObject.isAuthenticated() ) )
		{
			CommandAuthenticationDetails authDetails = ( CommandAuthenticationDetails ) authObject.getDetails();
			if ( !authDetails.getParams().containsKey( "user.login" ) )
			{
				authDetails.addParam( "user.login", new Object() );
				AuditView audit = new Builder( AuditEventNameEnum.USER_LOGIN.getName() ).build();
				eventRegistry.send( new AuditEvent( audit ) );
			}
		}
		chain.doFilter( request, response );
	}

	public void setEventRegistry( EventRegistry eventRegistry )
	{
		this.eventRegistry = eventRegistry;
	}
}

