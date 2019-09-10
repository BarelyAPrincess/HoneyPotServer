package com.marchnetworks.command.spring.security;

import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SessionAuthenticationFilter extends GenericFilterBean
{
	private AuthenticationManager authenticationManager;
	protected AuthenticationDetailsSource<HttpServletRequest, WebAuthenticationDetails> authenticationDetailsSource = new WebAuthenticationDetailsSource();

	private static final String SESSIONIDHEADER = "x-sessionId";

	private static final String SESSIONIDPARAM = "sessionId";

	public void afterPropertiesSet()
	{
		Assert.notNull( authenticationManager, "An AuthenticationManager is required" );
	}

	public void doFilter( ServletRequest req, ServletResponse res, FilterChain chain ) throws IOException, ServletException
	{
		HttpServletRequest request = ( HttpServletRequest ) req;
		HttpServletResponse response = ( HttpServletResponse ) res;
		Authentication authResult;

		if ( !requiresAuthentication( request, response ) )
		{
			chain.doFilter( request, response );
			return;
		}

		try
		{
			authResult = attemptAuthentication( request, response );
			if ( authResult == null )
				return;
		}
		catch ( AuthenticationException failed )
		{
			SecurityContextHolder.getContext().setAuthentication( null );
			response.sendError( 401, failed.getMessage() );
			return;
		}

		SecurityContextHolder.getContext().setAuthentication( authResult );
		chain.doFilter( request, response );
	}

	protected boolean requiresAuthentication( HttpServletRequest request, HttpServletResponse response )
	{
		String sessionIdheader = request.getHeader( "x-sessionId" );
		String sessionIdparam = request.getParameter( "sessionId" );
		if ( ( sessionIdheader == null ) && ( sessionIdparam == null ) )
		{
			return false;
		}
		return true;
	}

	public Authentication attemptAuthentication( HttpServletRequest request, HttpServletResponse response ) throws AuthenticationException, IOException, ServletException
	{
		String sessionId = null;

		if ( request.isSecure() )
		{
			sessionId = request.getHeader( "x-sessionId" );
			if ( sessionId == null )
			{
				sessionId = request.getParameter( "sessionId" );
			}
		}
		if ( sessionId == null )
		{
			throw new AuthenticationServiceException( "Session does not exist" );
		}
		SessionAuthenticationToken authRequest = new SessionAuthenticationToken( sessionId );
		authRequest.setDetails( authenticationDetailsSource.buildDetails( request ) );

		return getAuthenticationManager().authenticate( authRequest );
	}

	protected AuthenticationManager getAuthenticationManager()
	{
		return authenticationManager;
	}

	public void setAuthenticationManager( AuthenticationManager authenticationManager )
	{
		this.authenticationManager = authenticationManager;
	}

	public void setAuthenticationDetailsSource( AuthenticationDetailsSource<HttpServletRequest, WebAuthenticationDetails> authenticationDetailsSource )
	{
		Assert.notNull( authenticationDetailsSource, "AuthenticationDetailsSource required" );
		this.authenticationDetailsSource = authenticationDetailsSource;
	}

	public AuthenticationDetailsSource<HttpServletRequest, WebAuthenticationDetails> getAuthenticationDetailsSource()
	{
		return authenticationDetailsSource;
	}
}
