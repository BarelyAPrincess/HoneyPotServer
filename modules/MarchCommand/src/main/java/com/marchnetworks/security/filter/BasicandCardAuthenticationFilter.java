package com.marchnetworks.security.filter;

import com.marchnetworks.command.api.security.CommandAuthenticationDetails;
import com.marchnetworks.command.api.security.UserAuthenticationException;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.security.smartcard.CertStringGenerator;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.web.authentication.NullRememberMeServices;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BasicandCardAuthenticationFilter extends BasicAuthenticationFilter
{
	private CertStringGenerator theGenerator;
	private RememberMeServices rememberMeServices = new NullRememberMeServices();

	private static final String CLIENT_IDENTIFIER = "X-Hostname";

	protected void onSuccessfulAuthentication( HttpServletRequest request, HttpServletResponse response, Authentication authResult ) throws IOException
	{
		if ( authResult.isAuthenticated() )
		{
			SecurityContextHolder.getContext().setAuthentication( authResult );
			rememberMeServices.loginSuccess( request, response, authResult );
		}
		else
		{
			response.setHeader( "X-CertificateNeeded", theGenerator.certRequestString( authResult, request.getSession().getId() ) );
			response.setStatus( 202 );
		}
	}

	public void doFilter( ServletRequest req, ServletResponse res, FilterChain chain ) throws IOException, ServletException
	{
		boolean debug = logger.isDebugEnabled();
		HttpServletRequest request = ( HttpServletRequest ) req;
		HttpServletResponse response = ( HttpServletResponse ) res;

		String header = request.getHeader( "Authorization" );
		String validationRequired = request.getHeader( "X-CertValidation" );
		String clientIdentifier = request.getHeader( "X-Hostname" );
		String clientVersion = request.getHeader( "x-client-version" );
		if ( ( header != null ) && ( header.startsWith( "Basic " ) ) && ( validationRequired == null ) )
		{
			byte[] base64Token = header.substring( 6 ).getBytes( "UTF-8" );
			String token = new String( Base64.decode( base64Token ), getCredentialsCharset( request ) );

			String username = "";
			String password = "";
			int delim = token.indexOf( ":" );

			if ( delim != -1 )
			{
				username = token.substring( 0, delim );
				password = token.substring( delim + 1 );
			}

			if ( debug )
			{
				logger.debug( "Basic Authentication Authorization header found for user '" + username + "'" );
			}

			if ( authenticationIsRequired( username ) )
			{
				UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken( username, password );
				CommandAuthenticationDetails details = new CommandAuthenticationDetails( request );
				if ( !CommonAppUtils.isNullOrEmptyString( clientIdentifier ) )
				{
					details.addParam( "X-Hostname", clientIdentifier );
				}
				if ( !CommonAppUtils.isNullOrEmptyString( clientVersion ) )
				{
					details.addParam( "x-client-version", clientVersion );
				}
				authRequest.setDetails( details );

				Authentication authResult;

				try
				{
					authResult = getAuthenticationManager().authenticate( authRequest );
				}
				catch ( AuthenticationException failed )
				{
					if ( debug )
					{
						logger.debug( "Authentication request for user: " + username + " failed: " + failed.toString() );
					}

					if ( ( failed instanceof UserAuthenticationException ) )
					{
						UserAuthenticationException failedAuthException = ( UserAuthenticationException ) failed;
						failedAuthException.setUserName( username );
						response.setHeader( "x-reason", failedAuthException.getReasonCode() );
					}

					rememberMeServices.loginFail( request, response );

					onUnsuccessfulAuthentication( request, response, failed );

					getAuthenticationEntryPoint().commence( request, response, failed );
					return;
				}

				if ( debug )
					logger.debug( "Authentication success: " + authResult.toString() );

				onSuccessfulAuthentication( request, response, authResult );
			}
		}

		chain.doFilter( request, response );
	}

	public CertStringGenerator getTheGenerator()
	{
		return theGenerator;
	}

	public void setTheGenerator( CertStringGenerator theGenerator )
	{
		this.theGenerator = theGenerator;
	}

	private boolean authenticationIsRequired( String username )
	{
		Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();

		if ( ( existingAuth == null ) || ( !existingAuth.isAuthenticated() ) )
		{
			return true;
		}

		if ( ( ( existingAuth instanceof UsernamePasswordAuthenticationToken ) ) && ( !existingAuth.getName().equals( username ) ) )
		{
			return true;
		}

		if ( ( existingAuth instanceof AnonymousAuthenticationToken ) )
		{
			return true;
		}

		return false;
	}
}

