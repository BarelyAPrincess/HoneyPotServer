package com.marchnetworks.security.ldap;

import org.slf4j.Logger;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;

public class LdapGroupLookupCommand extends LdapLookupCommand
{
	private static final Logger LOG = org.slf4j.LoggerFactory.getLogger( LdapLookupCommand.class );
	private static final String LDAP_TOKEN_GROUPS = "tokenGroups";
	private List<String> ldapGroupLookupList = new java.util.ArrayList();

	public List<String> getLdapGroupLookupList()
	{
		return ldapGroupLookupList;
	}

	public LdapGroupLookupCommand( String userName, String ldapUserLogin, String ldapUserPassword ) throws LdapCommandException
	{
		super( userName, ldapUserLogin, ldapUserPassword, false );
	}

	public void execute() throws LdapCommandException
	{
		try
		{
			StartTlsResponse tls = null;
			env.put( "java.naming.ldap.attributes.binary", "tokenGroups" );
			ctx = loginLdap();

			if ( startTLS )
			{
				SSLTrustAllSocketFactory sf = new SSLTrustAllSocketFactory( DEFAULT_READ_TIMEOUT );
				tls = ( StartTlsResponse ) ctx.extendedOperation( new StartTlsRequest() );
				tls.negotiate( sf );
				addAuthentication( ctx );
			}

			SearchControls controls = groupSearchControls();
			NamingEnumeration<SearchResult> results = ctx.search( getSearchName(), "(objectClass=*)", controls );

			StringBuffer groupsSearchFilter = new StringBuffer();
			groupsSearchFilter.append( "(|" );
			while ( results.hasMoreElements() )
			{
				SearchResult searchResult = ( SearchResult ) results.nextElement();
				Attributes attrs = searchResult.getAttributes();
				if ( attrs != null )
				{
					try
					{
						for ( NamingEnumeration<? extends Attribute> ae = attrs.getAll(); ae.hasMore(); )
						{
							Attribute attr = ( Attribute ) ae.next();
							for ( NamingEnumeration<?> e = attr.getAll(); e.hasMore(); )
							{
								byte[] sid = ( byte[] ) e.next();
								groupsSearchFilter.append( "(objectSid=" + binarySidToStringSid( sid ) + ")" );
							}
						}
					}
					catch ( NamingException e )
					{
						LOG.warn( "Problem listing membership: " + e );
					}
				}
			}
			groupsSearchFilter.append( ")" );

			setSearchName( "*" );
			String domainBuffer = getSearchRoot();
			controls = standardSearchControls();
			results = ctx.search( domainBuffer, groupsSearchFilter.toString(), controls );
			while ( results.hasMoreElements() )
			{
				SearchResult sr = ( SearchResult ) results.next();
				Attributes attrs = sr.getAttributes();
				if ( ( attrs != null ) && ( attrs.get( "sAMAccountName" ) != null ) )
				{
					ldapGroupLookupList.add( ( String ) attrs.get( "sAMAccountName" ).get() );
				}
			}

			if ( startTLS )
			{
				tls.close();
			}

			ctx.close();
		}
		catch ( NamingException e )
		{
			LOG.warn( "Unable to perform username search on LDAP", e );
			throw new LdapCommandException( "Unable to perform username search on LDAP", e );
		}
		catch ( IOException e1 )
		{
			LOG.warn( "Unable to negotiate SSL Session", e1 );
			throw new LdapCommandException( "Unable to negotiate SSL Session", e1 );
		}
		catch ( KeyManagementException e2 )
		{
			LOG.warn( "Unable to manage Certificates", e2 );
			throw new LdapCommandException( "Unable to manage Certificates", e2 );
		}
		catch ( NoSuchAlgorithmException e1 )
		{
			throw new LdapCommandException( "No Such Algorithm Exception", e1 );
		}
	}

	private SearchControls groupSearchControls()
	{
		SearchControls controls = new SearchControls();
		controls.setSearchScope( 0 );
		controls.setCountLimit( getLookupMaxResultsConfig() );
		String[] returnedAtts = {"tokenGroups"};
		controls.setReturningAttributes( returnedAtts );
		return controls;
	}

	public static final String binarySidToStringSid( byte[] SID )
	{
		String strSID = "";

		strSID = "S";
		long version = SID[0];
		strSID = strSID + "-" + Long.toString( version );
		long authority = SID[4];

		for ( int i = 0; i < 4; i++ )
		{
			authority <<= 8;
			authority += ( SID[( 4 + i )] & 0xFF );
		}

		strSID = strSID + "-" + Long.toString( authority );
		long count = SID[2];
		count <<= 8;
		count += ( SID[1] & 0xFF );

		for ( int j = 0; j < count; j++ )
		{
			long rid = SID[( 11 + j * 4 )] & 0xFF;
			for ( int k = 1; k < 4; k++ )
			{
				rid <<= 8;
				rid += ( SID[( 11 - k + j * 4 )] & 0xFF );
			}
			strSID = strSID + "-" + Long.toString( rid );
		}

		return strSID;
	}
}

