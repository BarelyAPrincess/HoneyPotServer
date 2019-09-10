package com.marchnetworks.security.ldap;

import com.marchnetworks.command.api.security.AuthenticationExceptionReasonCodes;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.user.data.MemberView;
import com.marchnetworks.common.config.ConfigProperty;
import com.marchnetworks.common.diagnostics.metrics.MetricsHelper;
import com.marchnetworks.common.diagnostics.metrics.MetricsTypes;
import com.marchnetworks.shared.config.CommonConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LDAPServiceImpl implements LDAPService
{
	private static final Logger LOG = LoggerFactory.getLogger( LDAPServiceImpl.class );
	private CommonConfiguration commonConfig;

	public List<String> getGroups( MemberView aMember ) throws LdapException
	{
		String ldapPassword = getLdapPasswordFromConfig();
		String ldapUsername = getLdapUserNameFromConfig();

		String dn = null;
		if ( ( aMember.getDetailsView() == null ) || ( aMember.getDetailsView().getDistinguishedName() == null ) )
		{
			MemberView temp = lookupUniqueUser( aMember.getName(), LDAPField.SAM_ACCOUNT_NAME );
			if ( temp == null )
				throw new LdapException( "Unique user not found." );
			dn = temp.getDetailsView().getDistinguishedName();
		}
		else
		{
			dn = aMember.getDetailsView().getDistinguishedName();
		}

		LdapGroupLookupCommand cmd;

		try
		{
			cmd = new LdapGroupLookupCommand( dn, ldapUsername, ldapPassword );
			cmd.execute();
		}
		catch ( LdapCommandException e )
		{
			throw new LdapException( "Failed to authenticate user with LDAP Server. " + e.getMessage(), e );
		}

		return cmd.getLdapGroupLookupList();
	}

	public List<MemberView> lookupUsers( String username, int maxResults ) throws LdapException
	{
		String ldapPassword = getLdapPasswordFromConfig();
		String ldapUsername = getLdapUserNameFromConfig();
		try
		{
			LdapLookupCommand cmd = new LdapLookupCommand( username, ldapUsername, ldapPassword, true );
			cmd.setLdapLookupMaxResults( maxResults );
			cmd.execute();
			return cmd.getLdapMemberLookupList();
		}
		catch ( LdapCommandException e )
		{
			throw new LdapException( "Failed to lookup user with LDAP Server. " + e.getMessage() );
		}
	}

	public MemberView lookupUniqueUser( String username ) throws LdapException
	{
		LDAPField ldapField = LDAPField.SAM_ACCOUNT_NAME;
		if ( !LdapConstants.isUsernameQualified( username ) )
		{
			ldapField = LdapConstants.isDistinguishedName( username ) ? LDAPField.DN : LDAPField.UPN;
		}
		return lookupUniqueUser( username, ldapField );
	}

	public MemberView lookupUniqueUser( String username, LDAPField ldapField ) throws LdapException
	{
		if ( ( username != null ) && ( isLDAPEnabled() ) )
		{
			String ldapPassword = getLdapPasswordFromConfig();
			String ldapUsername = getLdapUserNameFromConfig();
			try
			{
				LdapLookupCommand cmd = new LdapLookupCommand( username, ldapField, ldapUsername, ldapPassword );
				cmd.execute();
				List<MemberView> searchLdapResult = cmd.getLdapMemberLookupList();
				if ( searchLdapResult.size() > 1 )
					throw new LdapException( AuthenticationExceptionReasonCodes.LDAP_ACCOUNT_NOT_UNIQUE );
				if ( searchLdapResult.isEmpty() )
				{
					throw new LdapException( AuthenticationExceptionReasonCodes.LDAP_ACCOUNT_NOT_FOUND );
				}
				return ( MemberView ) searchLdapResult.get( 0 );
			}
			catch ( LdapCommandException e )
			{
				LdapException ex = new LdapException( "Failed to lookup Unique user on LDAP. " + e.getMessage(), e );
				ex.setReasonCode( AuthenticationExceptionReasonCodes.FAILED );
				throw ex;
			}
		}
		return null;
	}

	public AuthenticationToken bind( String userPrincipalName, String userDistinguishedName, String userSAMAccountName, String password )
	{
		AuthenticationToken token = new AuthenticationToken();
		if ( ( !isLDAPEnabled() ) || ( ( isSimpleAuthenticationEnabled() ) && ( CommonAppUtils.isNullOrEmptyString( password ) ) ) )
		{
			return token;
		}

		if ( password == null )
		{
			password = "";
		}
		int retryAttempt = 0;

		while ( retryAttempt < 3 )
		{
			if ( retryAttempt == 3 )
			{
				throw new LdapDownException( "Failed to authenticate user after trying all LDAP servers." );
			}
			if ( !CommonAppUtils.isNullOrEmptyString( userPrincipalName ) )
			{
				try
				{
					LdapAuthenticateCommand cmd = new LdapAuthenticateCommand( userPrincipalName, password, retryAttempt );
					cmd.execute();
					if ( cmd.isUserAuthenticated() )
					{
						token.setAuthenticated( true, LDAPField.UPN );
						return token;
					}
				}
				catch ( LdapCommandException e )
				{
					LOG.debug( "UPN login attempt failed for user name: {}, exception message is {}", userPrincipalName, e.getMessage() );
				}
				catch ( LdapDownException e )
				{
					LOG.debug( "UPN login attempt failed for user name: {}, exception message is {}", userPrincipalName, e.getMessage() );
				}
			}

			boolean useDN = ( isSimpleAuthenticationEnabled() ) && ( !CommonAppUtils.isNullOrEmptyString( userDistinguishedName ) );
			String userCredential = useDN ? userDistinguishedName : userSAMAccountName;
			try
			{
				LdapAuthenticateCommand cmd = new LdapAuthenticateCommand( userCredential, password, retryAttempt );
				cmd.execute();
				if ( cmd.isUserAuthenticated() )
				{
					LDAPField ldapField = useDN ? LDAPField.DN : LDAPField.SAM_ACCOUNT_NAME;
					MetricsHelper.metrics.addBucketCounter( MetricsTypes.LDAP_FALLBACK_BIND.getName(), ldapField.name() );
					token.setAuthenticated( true, ldapField );
					return token;
				}
			}
			catch ( LdapCommandException e )
			{
				LOG.debug( "Fallback login attempt failed for user name: {}, exception message is {}", userCredential, e.getMessage() );
				if ( !isDynamicDiscoveryEnabled() )
				{
					break;
				}
			}
			catch ( LdapDownException e )
			{
				if ( !isDynamicDiscoveryEnabled() )
				{
					throw e;
				}
			}
			retryAttempt++;
		}
		return token;
	}

	private boolean isLDAPEnabled()
	{
		return commonConfig.getProperty( ConfigProperty.LDAP_ENABLED ).equals( "true" );
	}

	private String getLdapPasswordFromConfig()
	{
		return commonConfig.getLdapPasswordFromConfig();
	}

	private String getLdapUserNameFromConfig()
	{
		return commonConfig.getProperty( ConfigProperty.LDAP_USERNAME );
	}

	private boolean isSimpleAuthenticationEnabled()
	{
		return commonConfig.getProperty( ConfigProperty.LDAP_METHOD ).equals( "SIMPLE" );
	}

	private boolean isDynamicDiscoveryEnabled()
	{
		return commonConfig.getBooleanProperty( ConfigProperty.LDAP_DISCOVERY_ENABLED );
	}

	public void setCommonConfig( CommonConfiguration commonConfig )
	{
		this.commonConfig = commonConfig;
	}
}

