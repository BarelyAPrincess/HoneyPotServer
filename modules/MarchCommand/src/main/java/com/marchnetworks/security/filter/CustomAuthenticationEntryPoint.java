package com.marchnetworks.security.filter;

import com.marchnetworks.audit.data.AuditEventNameEnum;
import com.marchnetworks.audit.data.AuditView;
import com.marchnetworks.audit.data.AuditView.Builder;
import com.marchnetworks.audit.events.AuditEvent;
import com.marchnetworks.command.api.security.UserAuthenticationException;
import com.marchnetworks.common.config.ConfigProperty;
import com.marchnetworks.common.utils.DateUtils;
import com.marchnetworks.management.user.UserService;
import com.marchnetworks.server.event.EventRegistry;
import com.marchnetworks.shared.config.CommonConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.web.authentication.www.DigestAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.NonceExpiredException;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CustomAuthenticationEntryPoint extends DigestAuthenticationEntryPoint
{
	private static final Logger LOG = LoggerFactory.getLogger( CustomAuthenticationEntryPoint.class );
	private CommonConfiguration commonConfig;
	private EventRegistry eventRegistry;
	private UserService userService;
	private boolean digestSupported = false;
	private String digestEnabled = "";

	public void afterPropertiesSet() throws Exception
	{
		super.setKey( "infinova" );
		if ( "".equals( digestEnabled ) )
		{
			digestEnabled = System.getProperty( "digestAuthentication" );
			if ( "true".equals( digestEnabled ) )
			{
				digestSupported = true;
			}
		}
		super.setRealmName( commonConfig.getProperty( ConfigProperty.REALM, "Command" ) );

		if ( ( getRealmName() == null ) || ( "".equals( getRealmName() ) ) )
		{
			throw new IllegalArgumentException( "realmName must be specified" );
		}

		if ( ( getKey() == null ) || ( "".equals( getKey() ) ) )
		{
			throw new IllegalArgumentException( "key must be specified" );
		}
	}

	public void commence( HttpServletRequest request, HttpServletResponse response, AuthenticationException authException ) throws IOException, ServletException
	{
		HttpServletResponse httpResponse = response;

		if ( digestSupported )
		{

			long expiryTime = System.currentTimeMillis() + getNonceValiditySeconds() * 1000;
			String signatureValue = new String( md5Hex( expiryTime + ":" + getKey() ) );
			String nonceValue = expiryTime + ":" + signatureValue;
			String nonceValueBase64 = new String( Base64.encode( nonceValue.getBytes() ) );

			String authenticateHeader = "Digest realm=\"" + getRealmName() + "\", " + "qop=\"auth\", nonce=\"" + nonceValueBase64 + "\"";

			if ( ( authException instanceof NonceExpiredException ) )
			{
				authenticateHeader = authenticateHeader + ", stale=\"true\"";
			}

			LOG.debug( "WWW-Authenticate header sent to user agent: {}", authenticateHeader );

			httpResponse.addHeader( "WWW-Authenticate", authenticateHeader );
		}

		httpResponse.addHeader( "WWW-Authenticate", "Basic realm=\"" + getRealmName() + "\"" );

		if ( httpResponse.getStatus() != 202 )
		{
			httpResponse.sendError( 401, authException.getMessage() );
		}

		if ( ( authException instanceof UserAuthenticationException ) )
		{

			UserAuthenticationException commandAuthException = ( UserAuthenticationException ) authException;
			String userName = commandAuthException.getUserName();
			try
			{
				List<String> storedUsername = userService.findMembersNames( new String[] {userName} );
				if ( ( storedUsername != null ) && ( storedUsername.size() > 0 ) )
				{
					AuditView audit = new Builder( AuditEventNameEnum.USER_LOGIN_FAILED.getName(), ( String ) storedUsername.get( 0 ), request.getRemoteAddr(), Long.valueOf( DateUtils.getCurrentUTCTimeInMillis() ) ).addDetailsPair( "reason", commandAuthException.getReasonCode() ).build();

					eventRegistry.send( new AuditEvent( audit ) );
				}
			}
			catch ( Exception localException )
			{
			}
		}
	}

	private String md5Hex( String data )
	{
		MessageDigest digest;

		try
		{
			digest = MessageDigest.getInstance( "MD5" );
		}
		catch ( NoSuchAlgorithmException e )
		{
			throw new IllegalStateException( "No MD5 algorithm available!" );
		}

		return new String( Hex.encode( digest.digest( data.getBytes() ) ) );
	}

	public void setCommonConfig( CommonConfiguration commonConfig )
	{
		this.commonConfig = commonConfig;
	}

	public void setEventRegistry( EventRegistry eventRegistry )
	{
		this.eventRegistry = eventRegistry;
	}

	public void setUserService( UserService userService )
	{
		this.userService = userService;
	}
}

