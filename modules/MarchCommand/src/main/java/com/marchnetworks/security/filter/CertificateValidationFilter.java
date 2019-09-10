package com.marchnetworks.security.filter;

import com.marchnetworks.command.api.security.AuthenticationExceptionReasonCodes;
import com.marchnetworks.command.api.security.CommandAuthenticationDetails;
import com.marchnetworks.command.api.security.UserAuthenticationException;
import com.marchnetworks.security.smartcard.CertStringGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.NullRememberMeServices;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CertificateValidationFilter extends GenericFilterBean
{
	private static final Logger LOG = LoggerFactory.getLogger( CertificateValidationFilter.class );
	private AuthenticationEntryPoint authenticationEntryPoint;
	private AuthenticationManager authenticationManager;
	private RememberMeServices rememberMeServices = new NullRememberMeServices();
	private boolean ignoreFailure = false;
	private String credentialsCharset = "UTF-8";

	private CertStringGenerator theGenerator;

	public void afterPropertiesSet()
	{
		Assert.notNull( authenticationManager, "An AuthenticationManager is required" );

		if ( !isIgnoreFailure() )
		{
			Assert.notNull( authenticationEntryPoint, "An AuthenticationEntryPoint is required" );
		}
	}

	public void doFilter( ServletRequest req, ServletResponse res, FilterChain chain ) throws IOException, ServletException
	{
		HttpServletRequest request = ( HttpServletRequest ) req;
		HttpServletResponse response = ( HttpServletResponse ) res;

		String header = request.getHeader( "X-CertValidation" );
		if ( ( header != null ) && ( !header.isEmpty() ) )
		{

			Authentication authFromSession = theGenerator.getAuthentication( request.getSession().getId() );
			try
			{
				byte[] certId = header.substring( 0, header.indexOf( " " ) ).getBytes( "UTF-8" );
				byte[] validationString = header.substring( header.indexOf( " " ) + 1 ).getBytes();
				byte[] decodedValidationString = Base64.decode( validationString );
				if ( theGenerator.isValid( request.getSession().getId().getBytes(), certId, decodedValidationString ) )
				{
					Authentication authResult = theGenerator.getAuthentication( request.getSession().getId() );

					UsernamePasswordAuthenticationToken aToken = new UsernamePasswordAuthenticationToken( authResult.getPrincipal(), authResult.getCredentials(), authResult.getAuthorities() );

					SecurityContextHolder.getContext().setAuthentication( aToken );
					aToken.setDetails( new CommandAuthenticationDetails( request ) );
					rememberMeServices.loginSuccess( request, response, aToken );

					onSuccessfulAuthentication( request, response, aToken );

					theGenerator.remove( request.getSession().getId() );
				}
				else
				{
					SecurityContextHolder.getContext().setAuthentication( null );
					response.setHeader( "x-reason", AuthenticationExceptionReasonCodes.INVALID_CERTIFICATE.toString() );
					UserAuthenticationException userAuthException = new UserAuthenticationException( AuthenticationExceptionReasonCodes.INVALID_CERTIFICATE );
					userAuthException.setUserName( authFromSession.getName() );
					getAuthenticationEntryPoint().commence( request, response, userAuthException );
				}
			}
			catch ( Exception e )
			{
				LOG.debug( "Certificate validation on login failed due to {}", e );
				response.setHeader( "x-reason", AuthenticationExceptionReasonCodes.INVALID_CERTIFICATE.toString() );
				SecurityContextHolder.getContext().setAuthentication( null );
				UserAuthenticationException userAuthException = new UserAuthenticationException( AuthenticationExceptionReasonCodes.INVALID_CERTIFICATE );
				userAuthException.setUserName( authFromSession.getName() );
				getAuthenticationEntryPoint().commence( request, response, userAuthException );
			}
		}

		chain.doFilter( request, response );
	}

	protected void onSuccessfulAuthentication( HttpServletRequest request, HttpServletResponse response, Authentication authResult ) throws IOException
	{
	}

	protected void onUnsuccessfulAuthentication( HttpServletRequest request, HttpServletResponse response, AuthenticationException failed ) throws IOException
	{
	}

	protected AuthenticationEntryPoint getAuthenticationEntryPoint()
	{
		return authenticationEntryPoint;
	}

	public void setAuthenticationEntryPoint( AuthenticationEntryPoint authenticationEntryPoint )
	{
		this.authenticationEntryPoint = authenticationEntryPoint;
	}

	protected AuthenticationManager getAuthenticationManager()
	{
		return authenticationManager;
	}

	public void setAuthenticationManager( AuthenticationManager authenticationManager )
	{
		this.authenticationManager = authenticationManager;
	}

	protected boolean isIgnoreFailure()
	{
		return ignoreFailure;
	}

	public void setIgnoreFailure( boolean ignoreFailure )
	{
		this.ignoreFailure = ignoreFailure;
	}

	public void setRememberMeServices( RememberMeServices rememberMeServices )
	{
		Assert.notNull( rememberMeServices, "rememberMeServices cannot be null" );
		this.rememberMeServices = rememberMeServices;
	}

	public void setCredentialsCharset( String credentialsCharset )
	{
		Assert.hasText( credentialsCharset, "credentialsCharset cannot be null or empty" );
		this.credentialsCharset = credentialsCharset;
	}

	protected String getCredentialsCharset( HttpServletRequest httpRequest )
	{
		return credentialsCharset;
	}

	public CertStringGenerator getTheGenerator()
	{
		return theGenerator;
	}

	public void setTheGenerator( CertStringGenerator theGenerator )
	{
		this.theGenerator = theGenerator;
	}
}

