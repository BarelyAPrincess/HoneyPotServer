package com.marchnetworks.security.ldap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;

import javax.naming.CommunicationException;
import javax.naming.NamingException;

public class LdapAuthenticateCommand extends LdapCommand
{
	private static Logger LOG = LoggerFactory.getLogger( LdapAuthenticateCommand.class );

	public LdapAuthenticateCommand( String userName, String password, int retry ) throws LdapCommandException
	{
		super( userName, password, retry );
	}

	public void execute() throws LdapCommandException
	{
		try
		{
			LOG.debug( "Attempting to authenticate user: {}", env.get( "java.naming.security.principal" ) );
			ctx = loginLdap();
			Hashtable<?, ?> envProps;
			if ( ctx != null )
			{
				if ( LOG.isTraceEnabled() )
				{
					envProps = ctx.getEnvironment();
					for ( Object envPropKey : envProps.keySet() )
					{
						if ( !"java.naming.security.credentials".equals( envPropKey.toString() ) )
						{

							LOG.trace( " Context property : {} value {}", new Object[] {envPropKey.toString(), envProps.get( envPropKey ).toString()} );
						}
					}
				}
			}
			ctx.close();
		}
		catch ( CommunicationException ce )
		{
			String details = ce.getMessage() + " " + ce.getExplanation();
			LOG.debug( "Failed to communicate with Ldap Server when closing ldap context. Details: {} Root Cause {}", details, ce.getCause() );
			throw new LdapDownException( "Failed to communicate with Ldap Server. Details:" + details );
		}
		catch ( NamingException ne )
		{
			String details = ne.getMessage() + " " + ne.getExplanation();
			LOG.debug( "Error when closing context. Original error: {} Root Cause {}", details, ne.getCause() );
			throw new LdapDownException( "Error when closing ldap context. Details:" + details );
		}
	}

	public boolean isUserAuthenticated()
	{
		return ctx != null;
	}
}

