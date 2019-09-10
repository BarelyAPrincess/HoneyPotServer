package com.marchnetworks.security.ldap;

import com.marchnetworks.command.common.user.data.MemberTypeEnum;
import com.marchnetworks.command.common.user.data.MemberView;
import com.marchnetworks.command.common.user.data.UserDetailsView;
import com.marchnetworks.common.config.ConfigProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;

public class LdapLookupCommand extends LdapCommand
{
	private static final Logger LOG = LoggerFactory.getLogger( LdapLookupCommand.class );

	protected List<MemberView> ldapMemberLookupList = new ArrayList();

	private int ldapLookupMaxResults;

	protected String searchName;
	private LDAPField searchAttribute;
	private static final String LDAP_LOOKUP_CN = "CN";
	private static final String LDAP_LOOKUP_MAIL = "mail";
	private static final String LDAP_LOOKUP_OBJECT_CLASS = "objectClass";
	private static final String LDAP_LOOKUP_TELEPHONE = "telephoneNumber";
	private static final String LDAP_LOOKUP_SAMACCOUNTNAME = "sAMAccountName";
	private static final String LDAP_LOOKUP_TITLE = "title";
	private static final String LDAP_LOOKUP_MANAGER = "manager";
	private static final String LDAP_LOOKUP_PRINCIPALNAME = "userPrincipalName";
	private static final String LDAP_LOOKUP_DISTINGUISHEDNAME = "distinguishedName";
	private static final int LDAP_LOOKUP_DEFAULT_MAX_RESULTS = 200;
	private String LOOKUP_COMMONNAME = "CN";
	private String LOOKUP_MAIL = "mail";
	private String LOOKUP_PHONE = "telephoneNumber";
	private String LOOKUP_ACCOUNTNAME = "sAMAccountName";
	private String LOOKUP_TITLE = "title";
	private String LOOKUP_MANAGER = "manager";
	protected String LOOKUP_PRINCIPALNAME = "userPrincipalName";
	private String LOOKUP_DISTINGUISHEDNAME = "distinguishedName";
	private boolean wildCardAppend = false;

	public LdapLookupCommand( String userName, String ldapUserLogin, String ldapUserPassword, boolean wildCard ) throws LdapCommandException
	{
		super( ldapUserLogin, ldapUserPassword, 10 );
		searchName = userName;
		wildCardAppend = wildCard;
		LOG.debug( "*****LDAP Connection: Searching for " + userName );
	}

	public LdapLookupCommand( String userName, LDAPField searchAttribute, String ldapUserLogin, String ldapUserPassword ) throws LdapCommandException
	{
		super( ldapUserLogin, ldapUserPassword, 10 );
		searchName = userName;
		this.searchAttribute = searchAttribute;
	}

	public void execute() throws LdapCommandException
	{
		try
		{
			StartTlsResponse tls = null;
			ctx = loginLdap();

			if ( startTLS )
			{
				SSLTrustAllSocketFactory sf = new SSLTrustAllSocketFactory( DEFAULT_READ_TIMEOUT );
				tls = ( StartTlsResponse ) ctx.extendedOperation( new StartTlsRequest() );
				tls.negotiate( sf );
				addAuthentication( ctx );
			}

			NamingEnumeration<SearchResult> results = performLookup( ctx );
			while ( results.hasMoreElements() )
			{
				SearchResult searchResult = ( SearchResult ) results.nextElement();
				ldapMemberLookupList.add( createMemberFromLdapSearchResult( searchResult ) );
			}

			if ( LOG.isDebugEnabled() )
			{
				LOG.debug( "Results list size:", Integer.valueOf( ldapMemberLookupList.size() ) );
				if ( ldapMemberLookupList.isEmpty() )
				{
					LOG.debug( "Users not found with criteria from LDAP. Criteria:", getSearchName() );
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
			LOG.debug( "Unable to perform username search on LDAP due to {}", e );
			throw new LdapCommandException( "Unable to perform username search on LDAP", e );
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

	public List<MemberView> getLdapMemberLookupList()
	{
		return ldapMemberLookupList;
	}

	public String[] getLdapUserNamesLookupArray()
	{
		String[] namesArray = new String[ldapMemberLookupList.size()];

		int iCount = 0;
		for ( MemberView aMember : ldapMemberLookupList )
		{
			namesArray[iCount] = aMember.getDetailsView().getFullname();
			iCount++;
		}
		return namesArray;
	}

	protected NamingEnumeration<SearchResult> performLookup( DirContext ctx ) throws LdapCommandException, NamingException
	{
		if ( searchName == null )
		{
			LOG.error( "No lookup attribute defined" );
			throw new LdapCommandException( "Need to set LDAP lookup attribute" );
		}
		SearchControls controls = standardSearchControls();
		String domainBuffer = getSearchRoot();

		String customFilter = m_AppConfig.getProperty( ConfigProperty.LDAP_CUSTOMFILTER );
		if ( ( customFilter != null ) && ( !customFilter.equals( "" ) ) )
		{
			StringBuffer userBuffer = customUserBuffer( customFilter );
			NamingEnumeration<SearchResult> results = ctx.search( domainBuffer, userBuffer.toString(), controls );
			return results;
		}

		StringBuffer userBuffer = standardADUserBuffer();
		NamingEnumeration<SearchResult> results = ctx.search( domainBuffer, userBuffer.toString(), controls );
		if ( !results.hasMoreElements() )
		{
			userBuffer = standardLdapUserBuffer();
			results = ctx.search( domainBuffer, userBuffer.toString(), controls );
		}

		return results;
	}

	protected SearchControls standardSearchControls()
	{
		SearchControls controls = new SearchControls();
		controls.setSearchScope( 2 );
		controls.setCountLimit( getLookupMaxResultsConfig() );
		String[] returnedAtts = prepareAttributes();
		controls.setReturningAttributes( returnedAtts );
		return controls;
	}

	private String[] prepareAttributes()
	{
		List<String> theAttributes = new ArrayList();
		String temp = m_AppConfig.getProperty( ConfigProperty.LDAP_ACCOUNTNAME );
		if ( ( temp != null ) && ( !temp.equals( "" ) ) )
		{
			theAttributes.add( temp );
			LOOKUP_ACCOUNTNAME = temp;
		}
		else
		{
			theAttributes.add( "sAMAccountName" );
			LOOKUP_ACCOUNTNAME = "sAMAccountName";
		}

		temp = m_AppConfig.getProperty( ConfigProperty.LDAP_COMMONNAME );
		if ( ( temp != null ) && ( !temp.equals( "" ) ) )
		{
			theAttributes.add( temp );
			LOOKUP_COMMONNAME = temp;
		}
		else
		{
			theAttributes.add( "CN" );
			LOOKUP_COMMONNAME = "CN";
		}

		temp = m_AppConfig.getProperty( ConfigProperty.LDAP_MAIL );
		if ( ( temp != null ) && ( !temp.equals( "" ) ) )
		{
			theAttributes.add( temp );
			LOOKUP_MAIL = temp;
		}
		else
		{
			theAttributes.add( "mail" );
			LOOKUP_MAIL = "mail";
		}

		temp = m_AppConfig.getProperty( ConfigProperty.LDAP_PHONE );
		if ( ( temp != null ) && ( !temp.equals( "" ) ) )
		{
			theAttributes.add( temp );
			LOOKUP_PHONE = temp;
		}
		else
		{
			theAttributes.add( "telephoneNumber" );
			LOOKUP_PHONE = "telephoneNumber";
		}

		temp = m_AppConfig.getProperty( ConfigProperty.LDAP_TITLE );
		if ( ( temp != null ) && ( !temp.equals( "" ) ) )
		{
			theAttributes.add( temp );
			LOOKUP_TITLE = temp;
		}
		else
		{
			theAttributes.add( "title" );
			LOOKUP_TITLE = "title";
		}

		temp = m_AppConfig.getProperty( ConfigProperty.LDAP_MANAGER );
		if ( ( temp != null ) && ( !temp.equals( "" ) ) )
		{
			theAttributes.add( temp );
			LOOKUP_MANAGER = temp;
		}
		else
		{
			theAttributes.add( "manager" );
			LOOKUP_MANAGER = "manager";
		}

		temp = m_AppConfig.getProperty( ConfigProperty.LDAP_PRINCIPALNAME );
		if ( ( temp != null ) && ( !temp.equals( "" ) ) )
		{
			theAttributes.add( temp );
			LOOKUP_PRINCIPALNAME = temp;
		}
		else
		{
			theAttributes.add( "userPrincipalName" );
			LOOKUP_PRINCIPALNAME = "userPrincipalName";
		}

		temp = m_AppConfig.getProperty( ConfigProperty.LDAP_DISTINGUISHEDNAME );
		if ( ( temp != null ) && ( !temp.equals( "" ) ) )
		{
			theAttributes.add( temp );
			LOOKUP_DISTINGUISHEDNAME = temp;
		}
		else
		{
			theAttributes.add( "distinguishedName" );
			LOOKUP_DISTINGUISHEDNAME = "distinguishedName";
		}

		theAttributes.add( "objectClass" );
		String[] anArray = ( String[] ) theAttributes.toArray( new String[theAttributes.size()] );

		return anArray;
	}

	private StringBuffer standardLdapUserBuffer()
	{
		StringBuffer userBuffer = new StringBuffer();
		userBuffer.append( "(&(|(&(|(ObjectClass=user)(ObjectClass=userProxy)(ObjectClass=inetOrgPerson))(!(ObjectClass=computer)))(&(ObjectClass=group)(!(ObjectClass=computer))))(|" );
		if ( searchName != null )
		{
			userBuffer.append( "(" + LOOKUP_COMMONNAME + "=" ).append( prepareNameForLookup( searchName ) ).append( ")" );
			userBuffer.append( "(" + LOOKUP_ACCOUNTNAME + "=" ).append( prepareNameForLookup( searchName ) ).append( ")" );
		}
		userBuffer.append( "))" );
		return userBuffer;
	}

	private StringBuffer customUserBuffer( String customFilter )
	{
		StringBuffer userBuffer = new StringBuffer();
		userBuffer.append( "(&(" + customFilter + ")(|" );
		if ( searchName != null )
		{
			userBuffer.append( getSearchString() );
		}
		userBuffer.append( "))" );
		return userBuffer;
	}

	private StringBuffer standardADUserBuffer() throws LdapCommandException
	{
		StringBuffer userBuffer = new StringBuffer();
		userBuffer.append( "(&(|((&(|(ObjectClass=user)(ObjectClass=userProxy)(ObjectClass=inetOrgPerson))(!(ObjectClass=computer))(sAMAccountType=805306368)))((&(ObjectClass=group)(sAMAccountType=268435456))))(|" );

		if ( searchName != null )
		{
			userBuffer.append( getSearchString() );
		}
		userBuffer.append( "))" );
		return userBuffer;
	}

	private String getSearchString()
	{
		StringBuilder sb = new StringBuilder();
		if ( searchAttribute != null )
		{
			if ( searchAttribute == LDAPField.UPN )
			{
				sb.append( "(" ).append( LOOKUP_PRINCIPALNAME ).append( "=" ).append( searchName ).append( ")" );
			}
			else if ( searchAttribute == LDAPField.DN )
			{
				sb.append( "(" ).append( LOOKUP_DISTINGUISHEDNAME ).append( "=" ).append( searchName ).append( ")" );
			}
			else if ( searchAttribute == LDAPField.SAM_ACCOUNT_NAME )
			{
				sb.append( "(" ).append( LOOKUP_ACCOUNTNAME ).append( "=" ).append( searchName ).append( ")" );
			}
		}
		else
		{
			sb.append( "(" + LOOKUP_COMMONNAME + "=" ).append( prepareNameForLookup( searchName ) ).append( ")" );
			sb.append( "(" + LOOKUP_ACCOUNTNAME + "=" ).append( prepareNameForLookup( searchName ) ).append( ")" );
			sb.append( "(" + LOOKUP_PRINCIPALNAME + "=" ).append( searchName ).append( ")" );
			sb.append( "(" + LOOKUP_DISTINGUISHEDNAME + "=" ).append( searchName ).append( ")" );
		}
		return sb.toString();
	}

	private String prepareNameForLookup( String commonName )
	{
		if ( !wildCardAppend )
		{
			return commonName;
		}
		StringBuffer sb = new StringBuffer( commonName );
		Pattern p = Pattern.compile( "[\\*]+" );
		Matcher m = p.matcher( commonName );
		if ( !m.find() )
		{
			sb.append( "*" );
		}
		return sb.toString();
	}

	protected MemberView createMemberFromLdapSearchResult( SearchResult searchResult ) throws NamingException
	{
		MemberView resultMember = null;
		if ( searchResult != null )
		{
			resultMember = new MemberView();
			resultMember.setDetailsView( new UserDetailsView() );
			Attributes attrs = searchResult.getAttributes();

			NamingEnumeration<? extends Attribute> attrResult = attrs.getAll();

			while ( attrResult.hasMoreElements() )
			{
				Attribute attribute = attrResult.nextElement();
				String value = attribute.get().toString();
				if ( attribute.getID().equalsIgnoreCase( "objectClass" ) )
				{
					if ( attribute.contains( "group" ) )
					{
						resultMember.setType( MemberTypeEnum.GROUP );
					}
					else
					{
						resultMember.setType( MemberTypeEnum.LDAP_USER );
					}
				}
				if ( attribute.getID().equalsIgnoreCase( LOOKUP_MAIL ) )
				{
					resultMember.getDetailsView().setEmail( value );
				}
				else if ( attribute.getID().equalsIgnoreCase( LOOKUP_PHONE ) )
				{
					resultMember.getDetailsView().setTelephone( value );
				}
				else if ( attribute.getID().equalsIgnoreCase( LOOKUP_ACCOUNTNAME ) )
				{
					resultMember.setName( value );
				}
				else if ( attribute.getID().equalsIgnoreCase( LOOKUP_COMMONNAME ) )
				{
					resultMember.getDetailsView().setFullname( value );
				}
				else if ( attribute.getID().equalsIgnoreCase( LOOKUP_PRINCIPALNAME ) )
				{
					resultMember.getDetailsView().setPrincipalName( value );
				}
				else if ( attribute.getID().equalsIgnoreCase( LOOKUP_TITLE ) )
				{
					resultMember.getDetailsView().setPosition( value );
				}
				else if ( attribute.getID().equalsIgnoreCase( LOOKUP_DISTINGUISHEDNAME ) )
				{
					resultMember.getDetailsView().setDistinguishedName( value );
				}
				else if ( attribute.getID().equalsIgnoreCase( LOOKUP_MANAGER ) )
				{

					int start = value.indexOf( "=" );
					int end = value.indexOf( ",", start );
					resultMember.getDetailsView().setManager( value.substring( start + 1, end ) );
				}
				else
				{
					LOG.trace( "Known LDAP attribute found ID=" + attribute.getID() + " value=" + attribute.get() );
				}
			}
		}
		return resultMember;
	}

	protected int getLookupMaxResultsConfig()
	{
		return ( ldapLookupMaxResults <= 0 ) || ( ldapLookupMaxResults > 200 ) ? 200 : ldapLookupMaxResults;
	}

	public int getLdapLookupMaxResults()
	{
		return ldapLookupMaxResults;
	}

	public void setLdapLookupMaxResults( int ldapLookupMaxResults )
	{
		this.ldapLookupMaxResults = ldapLookupMaxResults;
	}

	public String getSearchName()
	{
		return searchName;
	}

	public void setSearchName( String searchName )
	{
		this.searchName = searchName;
	}
}

