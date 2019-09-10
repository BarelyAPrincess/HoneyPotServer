package com.marchnetworks.security.ldap;

import com.marchnetworks.common.config.AppConfig;
import com.marchnetworks.common.config.AppConfigImpl;
import com.marchnetworks.common.config.ConfigProperty;
import com.marchnetworks.security.dns.DnsLookup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import javax.naming.CommunicationException;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;

public abstract class LdapCommand
{
	private static final Logger LOG = LoggerFactory.getLogger( LdapLookupCommand.class );
	private static int DEFAULT_CONNECT_TIMEOUT = 5000;
	private static int DEFAULT_DNS_RESPONSE_TIMEOUT = 10000;
	protected static int DEFAULT_READ_TIMEOUT = 15000;

	protected LdapContext ctx = null;

	protected Hashtable<String, Object> env = new Hashtable();

	protected AppConfig m_AppConfig = AppConfigImpl.getInstance();

	protected String ldapLoginUser = null;
	protected String ldapLoginPasswd = null;
	protected boolean startTLS = false;

	public LdapCommand( String ldapLoginUser, String ldapLoginPasswd, int retry ) throws LdapCommandException
	{
		this.ldapLoginUser = ldapLoginUser;
		this.ldapLoginPasswd = ldapLoginPasswd;

		try
		{
			DnsLookup.ldapListObtained.await( DEFAULT_DNS_RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS );
		}
		catch ( InterruptedException e )
		{
			throw new LdapCommandException( "LDAP Unable to retrieve servers list", e );
		}

		env.put( "java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory" );
		String providerUrl = DnsLookup.getLdapProviderUrl();

		env.put( "java.naming.provider.url", providerUrl );

		LOG.debug( "*****LDAP Connection: Connecting to " + providerUrl + " using username " + ldapLoginUser );

		env.put( "com.sun.jndi.ldap.read.timeout", String.valueOf( DEFAULT_READ_TIMEOUT ) );
		String timeout = m_AppConfig.getProperty( ConfigProperty.LDAP_TIMEOUT );
		if ( timeout != null )
		{
			Integer ldapConnectionTimeout = Integer.valueOf( Integer.parseInt( timeout ) * 1000 );
			if ( ldapConnectionTimeout.intValue() > 0 )
			{
				env.put( "com.sun.jndi.ldap.connect.timeout", calculateTimeout( ldapConnectionTimeout, retry ).toString() );
			}
			else
			{
				env.put( "com.sun.jndi.ldap.connect.timeout", String.valueOf( DEFAULT_CONNECT_TIMEOUT ) );
			}
		}

		String de = m_AppConfig.getProperty( ConfigProperty.LDAP_DISCOVERY_ENABLED );
		boolean discovery = false;
		if ( de != null )
		{
			discovery = Boolean.parseBoolean( de );
		}

		String t = m_AppConfig.getProperty( ConfigProperty.LDAP_STARTTLS );
		if ( t != null )
		{
			startTLS = Boolean.parseBoolean( t );
		}

		if ( !startTLS )
		{
			addAuthentication( null );
		}
		else
		{
			LOG.debug( "*****LDAP Protocol Extension: startTLS" );
		}

		String s = m_AppConfig.getProperty( ConfigProperty.LDAP_SSL );
		if ( ( s != null ) && ( Boolean.parseBoolean( s ) ) && ( !discovery ) && ( !startTLS ) )
		{
			env.put( "java.naming.security.protocol", "ssl" );
			env.put( "java.naming.ldap.factory.socket", SSLTrustAllSocketFactory.class.getName() );
			LOG.debug( "*****LDAP Connection Type: SSL" );
		}

		String d = m_AppConfig.getProperty( ConfigProperty.LDAP_DEBUG );
		if ( ( d != null ) && ( Boolean.parseBoolean( d ) ) )
		{
			env.put( "com.sun.jndi.ldap.trace.ber", System.err );
		}
	}

	private Integer calculateTimeout( Integer timeout, int retry )
	{
		Integer t = timeout;

		switch ( retry )
		{
			case 0:
				t = Integer.valueOf( timeout.intValue() / 8 );
				break;
			case 1:
				t = Integer.valueOf( timeout.intValue() / 5 );
				break;
			case 2:
				t = Integer.valueOf( timeout.intValue() / 2 );
				break;
		}

		if ( t.intValue() < 500 )
			t = Integer.valueOf( 500 );
		return t;
	}

	protected void addAuthentication( LdapContext context ) throws LdapCommandException
	{
		String method = m_AppConfig.getProperty( ConfigProperty.LDAP_METHOD );

		LOG.debug( "*****LDAP Connection Method: " + method );

		if ( ( method != null ) && ( method.equals( "SIMPLE" ) ) )
		{
			useSimpleMethod( context );
		}
		else if ( ( method != null ) && ( method.equals( "DIGEST" ) ) )
		{
			useDigestMethod( context );
		}
		else
		{
			LOG.debug( "*****LDAP Connection: No method found in xml, defaulting to DIGEST." );
			useDigestMethod( context );
		}

		if ( context == null )
		{
			env.put( "java.naming.security.principal", ldapLoginUser );

			env.put( "java.naming.security.credentials", ldapLoginPasswd );
		}
		else
		{
			try
			{
				context.addToEnvironment( "java.naming.security.principal", ldapLoginUser );

				context.addToEnvironment( "java.naming.security.credentials", ldapLoginPasswd );

				context.reconnect( context.getConnectControls() );
			}
			catch ( NamingException e )
			{
				throw new LdapCommandException( "LDAP Unable to bind with credentials provided", e );
			}
		}
	}

	private void useDigestMethod( LdapContext ctx ) throws LdapCommandException
	{
		int symbol = ldapLoginUser.indexOf( '@' );
		if ( symbol != -1 )
		{
			String domain = ldapLoginUser.substring( symbol + 1, ldapLoginUser.length() );
			ldapLoginUser = ldapLoginUser.substring( 0, symbol );
			if ( ctx == null )
			{
				env.put( "java.naming.security.sasl.realm", domain );
			}
			else
			{
				try
				{
					ctx.addToEnvironment( "java.naming.security.sasl.realm", domain );
				}
				catch ( NamingException e )
				{
					throw new LdapCommandException( "LDAP Unable to add authentication principal", e );
				}
			}
		}

		if ( ctx == null )
		{
			env.put( "java.naming.security.authentication", "DIGEST-MD5" );
		}
		else
		{
			try
			{
				ctx.addToEnvironment( "java.naming.security.authentication", "DIGEST-MD5" );
			}
			catch ( NamingException e )
			{
				throw new LdapCommandException( "LDAP Unable to add authentication method", e );
			}
		}
	}

	private void useSimpleMethod( LdapContext ctx ) throws LdapCommandException
	{
		if ( ctx == null )
		{
			env.put( "java.naming.security.authentication", "SIMPLE" );
		}
		else
		{
			try
			{
				ctx.addToEnvironment( "java.naming.security.authentication", "SIMPLE" );
			}
			catch ( NamingException e )
			{
				throw new LdapCommandException( "LDAP Unable to add authentication method", e );
			}
		}
	}

	protected LdapContext loginLdap() throws LdapCommandException
	{
		LdapContext ctx = null;
		try
		{
			ctx = new InitialLdapContext( env, null );
		}
		catch ( CommunicationException e )
		{
			throw new LdapDownException( e.getMessage(), e );
		}
		catch ( NamingException e )
		{
			throw new LdapCommandException( e );
		}

		if ( startTLS )
		{
			SSLTrustAllSocketFactory sf = null;
			StartTlsResponse tls = null;
			try
			{
				sf = new SSLTrustAllSocketFactory( DEFAULT_READ_TIMEOUT );

				tls = ( StartTlsResponse ) ctx.extendedOperation( new StartTlsRequest() );
				tls.negotiate( sf );
				addAuthentication( ctx );
				tls.close();
			}
			catch ( NamingException e )
			{
				LOG.debug( "Unable to find host" );
				throw new LdapCommandException( "Unable to find host", e );
			}
			catch ( IOException e1 )
			{
				LOG.warn( "Unable to negotiate SSL Session", e1 );
				throw new LdapCommandException( "Unable to negotiate SSL Session", e1 );
			}
			catch ( KeyManagementException e )
			{
				LOG.warn( "Unable to manage Certificates", e );
				throw new LdapCommandException( "Unable to manage Certificates", e );
			}
			catch ( NoSuchAlgorithmException e )
			{
				LOG.warn( "No Algorithm supported", e );
				throw new LdapCommandException( "No Algorithm supported", e );
			}
		}

		return ctx;
	}

	protected String getSearchRoot()
	{
		return m_AppConfig.getProperty( ConfigProperty.LDAP_SEARCH_DIRECTORY_ROOT );
	}

	public abstract void execute() throws LdapCommandException;
}

