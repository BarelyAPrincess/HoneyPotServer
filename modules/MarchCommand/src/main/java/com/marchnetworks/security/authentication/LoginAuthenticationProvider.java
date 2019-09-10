package com.marchnetworks.security.authentication;

import com.marchnetworks.app.service.OsgiService;
import com.marchnetworks.audit.data.AuditEventNameEnum;
import com.marchnetworks.audit.data.AuditView;
import com.marchnetworks.audit.events.AuditEvent;
import com.marchnetworks.command.api.security.AppAuthenticationService;
import com.marchnetworks.command.api.security.AuthenticationExceptionReasonCodes;
import com.marchnetworks.command.api.security.CommandAuthenticationDetails;
import com.marchnetworks.command.api.security.UserAuthenticationException;
import com.marchnetworks.command.api.security.UserInformation;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.user.UserException;
import com.marchnetworks.command.common.user.data.AppProfileData;
import com.marchnetworks.command.common.user.data.MemberTypeEnum;
import com.marchnetworks.command.common.user.data.MemberView;
import com.marchnetworks.command.common.user.data.RightEnum;
import com.marchnetworks.command.common.user.data.UserDetailsView;
import com.marchnetworks.common.diagnostics.metrics.MetricsHelper;
import com.marchnetworks.common.diagnostics.metrics.MetricsTypes;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.user.UserService;
import com.marchnetworks.security.ldap.AuthenticationToken;
import com.marchnetworks.security.ldap.LDAPField;
import com.marchnetworks.security.ldap.LDAPService;
import com.marchnetworks.security.ldap.LdapConstants;
import com.marchnetworks.security.ldap.LdapDownException;
import com.marchnetworks.server.event.EventRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class LoginAuthenticationProvider implements AuthenticationProvider, UserDetailsService
{
	private static final Logger LOG = LoggerFactory.getLogger( LoginAuthenticationProvider.class );
	private UserService userService;
	private LDAPService ldapService;
	private EventRegistry eventRegistry;
	private SessionService sessionService;

	public Authentication authenticate( Authentication authentication ) throws UserAuthenticationException
	{
		if ( !( authentication instanceof UsernamePasswordAuthenticationToken ) )
		{
			return null;
		}
		UsernamePasswordAuthenticationToken token = ( UsernamePasswordAuthenticationToken ) authentication;

		if ( ( token.getDetails() instanceof CommandAuthenticationDetails ) )
		{
			CommandAuthenticationDetails details = ( CommandAuthenticationDetails ) token.getDetails();
			for ( Entry<String, Object> param : details.getParams().entrySet() )
			{
				CommonAppUtils.addAuthenticationObject( ( String ) param.getKey(), param.getValue() );
			}
		}

		MemberView memberWrapper = null;
		try
		{
			memberWrapper = authenticateUser( token.getName(), token.getCredentials().toString() );
		}
		catch ( UserAuthenticationException e )
		{
			LOG.warn( "Login failure, username: {}, code: {}, message: {}", new Object[] {token.getName(), e.getReasonCode(), e.getMessage()} );

			if ( e.getReasonCode().equals( AuthenticationExceptionReasonCodes.NOT_AUTHORIZED.name().toLowerCase() ) )
			{
				String username = authentication.getName();
				AuditView.Builder builder = new AuditView.Builder( AuditEventNameEnum.USER_LOGIN_FAILED.getName() );
				builder.setUsername( username );
				WebAuthenticationDetails details = ( WebAuthenticationDetails ) authentication.getDetails();
				String address = details.getRemoteAddress();
				builder.setRemoteIpAddress( address );
				builder.addDetailsPair( "reason", e.getReasonCode() );
				eventRegistry.send( new AuditEvent( builder.build() ) );
			}
			throw e;
		}

		MetricsHelper.metrics.addConcurrent( MetricsTypes.USER_LOGINS.getName(), sessionService.getTotalSessions() + 1 );

		if ( memberWrapper != null )
		{
			List<GrantedAuthority> authorities = new ArrayList();

			for ( RightEnum right : memberWrapper.getAssembledRights() )
			{
				authorities.add( new SimpleGrantedAuthority( "ROLE_" + right.toString() ) );
			}
			for ( AppProfileData data : memberWrapper.getAssembledAppData() )
			{
				for ( String right : data.getAppRights() )
				{
					authorities.add( new SimpleGrantedAuthority( "ROLE_" + right ) );
				}
			}

			UserInformation userInfo = new UserInformation( memberWrapper.getName(), convertAuthoritiesToStrings( authorities ) );
			UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken( userInfo, token.getCredentials(), authorities );
			result.setDetails( token.getDetails() );

			if ( ( memberWrapper.getDetailsView() != null ) && ( memberWrapper.getDetailsView().getCertificateId() != null ) )
			{
				result.setAuthenticated( false );
			}

			OsgiService osgiService = ( OsgiService ) ApplicationContextSupport.getBean( "osgiManager" );
			for ( AppAuthenticationService service : osgiService.getServices( AppAuthenticationService.class ) )
			{
				service.authenticate( memberWrapper, result );
			}

			CommonAppUtils.clearAuthenticationObjects();

			return result;
		}
		throw new UserAuthenticationException( "User not found.", AuthenticationExceptionReasonCodes.NOT_AUTHORIZED );
	}

	public boolean supports( Class<? extends Object> authentication )
	{
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom( authentication );
	}

	private List<String> convertAuthoritiesToStrings( List<GrantedAuthority> authorities )
	{
		List<String> result = new ArrayList();
		for ( GrantedAuthority ga : authorities )
		{
			result.add( ga.getAuthority() );
		}
		return result;
	}

	public MemberView authenticateUser( String username, String password ) throws UserAuthenticationException
	{
		MemberView databaseMemberView = null;
		MemberView ldapMemberView = null;
		AuthenticationToken token = null;
		String shortUsername = username;
		if ( LdapConstants.isUsernameQualified( username ) )
		{
			LDAPField ldapField = LdapConstants.isDistinguishedName( username ) ? LDAPField.DN : LDAPField.UPN;
			ldapMemberView = lookupUserInLdap( username, ldapField );

			UserDetailsView detailsView = ldapMemberView.getDetailsView();
			token = ldapService.bind( detailsView.getPrincipalName(), detailsView.getDistinguishedName(), ldapMemberView.getName(), password );
			if ( !token.isAuthenticated() )
			{
				throw new UserAuthenticationException( AuthenticationExceptionReasonCodes.NOT_AUTHORIZED );
			}
			shortUsername = ldapMemberView.getName();
		}

		databaseMemberView = userService.getMember( shortUsername );
		if ( databaseMemberView == null )
		{
			return groupUserLogin( ldapMemberView, username, password );
		}

		if ( ( databaseMemberView.getDetailsView() != null ) && ( !databaseMemberView.getDetailsView().isActive() ) )
		{
			throw new UserAuthenticationException( "User is not active.", AuthenticationExceptionReasonCodes.LOCKED );
		}

		if ( databaseMemberView.getType() == MemberTypeEnum.GROUP )
		{
			throw new UserAuthenticationException( "Attempt to login as invalid user. Aborting.", AuthenticationExceptionReasonCodes.NOT_AUTHORIZED );
		}

		if ( databaseMemberView.getType() == MemberTypeEnum.LOCAL_USER )
		{
			return localUserLogin( databaseMemberView, password );
		}
		return loginLDAPUser( databaseMemberView, ldapMemberView, token, password );
	}

	private MemberView lookupUserInLdap( String username, LDAPField userAttribute )
	{
		MemberView aMember = ldapService.lookupUniqueUser( username, userAttribute );
		if ( aMember == null )
		{
			throw new UserAuthenticationException( "User not found in ldap.", AuthenticationExceptionReasonCodes.NOT_AUTHORIZED );
		}
		return aMember;
	}

	private MemberView loginLDAPUser( MemberView databaseMemberView, MemberView ldapMemberView, AuthenticationToken authenticationToken, String password ) throws UserAuthenticationException
	{
		if ( authenticationToken == null )
		{
			try
			{
				UserDetailsView detailsView = databaseMemberView.getDetailsView();
				authenticationToken = ldapService.bind( detailsView.getPrincipalName(), detailsView.getDistinguishedName(), databaseMemberView.getName(), password );
			}
			catch ( LdapDownException e )
			{
				if ( userService.authenticateUser( databaseMemberView, password ) )
				{
					try
					{
						return userService.assembleRightsAndResources( databaseMemberView, false );
					}
					catch ( UserException e1 )
					{
						throw new UserAuthenticationException( e1.getMessage(), AuthenticationExceptionReasonCodes.FAILED );
					}
				}

				throw e;
			}
			if ( authenticationToken.isAuthenticated() )
			{
				String userAttribute = databaseMemberView.getDetailsView().getPrincipalName();
				if ( authenticationToken.getLdapField() == LDAPField.DN )
				{
					userAttribute = databaseMemberView.getDetailsView().getDistinguishedName();
				}
				else if ( authenticationToken.getLdapField() == LDAPField.SAM_ACCOUNT_NAME )
				{
					userAttribute = databaseMemberView.getName();
				}
				ldapMemberView = lookupUserInLdap( userAttribute, authenticationToken.getLdapField() );
			}
		}

		if ( authenticationToken.isAuthenticated() )
		{
			databaseMemberView.getDetailsView().updateLdapDetails( ldapMemberView.getDetailsView() );
			try
			{
				Set<Long> groupIds = userService.allowedGroups( ldapService.getGroups( ldapMemberView ) );

				databaseMemberView.setGroups( groupIds );
				return userService.assembleRightsAndResources( databaseMemberView, true );
			}
			catch ( UserException e )
			{
				throw new UserAuthenticationException( e.getMessage(), AuthenticationExceptionReasonCodes.FAILED );
			}
		}
		throw new UserAuthenticationException( "Login LDAP user failed.", AuthenticationExceptionReasonCodes.NOT_AUTHORIZED );
	}

	private MemberView localUserLogin( MemberView member, String password ) throws UserAuthenticationException
	{
		if ( userService.authenticateUser( member, password ) )
		{
			try
			{
				return userService.assembleRightsAndResources( member, true );
			}
			catch ( UserException e )
			{
				throw new UserAuthenticationException( e.getMessage(), AuthenticationExceptionReasonCodes.FAILED );
			}
		}

		throw new UserAuthenticationException( "Local user login not authenticated.", AuthenticationExceptionReasonCodes.NOT_AUTHORIZED );
	}

	private MemberView groupUserLogin( MemberView memberView, String username, String password ) throws UserAuthenticationException
	{
		try
		{
			if ( memberView == null )
			{
				memberView = lookupUserInLdap( username, LDAPField.SAM_ACCOUNT_NAME );

				UserDetailsView detailsView = memberView.getDetailsView();
				AuthenticationToken token = ldapService.bind( detailsView.getPrincipalName(), detailsView.getDistinguishedName(), memberView.getName(), password );
				if ( !token.isAuthenticated() )
				{
					throw new UserAuthenticationException( "LDAP group user login failed.", AuthenticationExceptionReasonCodes.NOT_AUTHORIZED );
				}
			}
			Set<Long> groupsIds = userService.allowedGroups( ldapService.getGroups( memberView ) );

			memberView.setGroups( groupsIds );
			boolean membersGroupNotInCommand = groupsIds.isEmpty();

			if ( membersGroupNotInCommand )
			{
				throw new UserAuthenticationException( "User was authenticated but no ldap groups are in Command.", AuthenticationExceptionReasonCodes.LDAP_GROUP_NOT_IN_CES );
			}

			memberView.setType( MemberTypeEnum.GROUP_USER );
			if ( memberView.getDetailsView() != null )
			{
				memberView.getDetailsView().setActive( true );

				userService.createMember( memberView, password );

				return userService.assembleRightsAndResources( memberView, true );
			}
		}
		catch ( LdapDownException e )
		{
			throw e;
		}
		catch ( UndeclaredThrowableException e )
		{
			throw new UserAuthenticationException( "Generic DB connection error", AuthenticationExceptionReasonCodes.DATABASE_CONNECTION_LOST );
		}
		catch ( UserException e )
		{
			throw new UserAuthenticationException( e.getMessage(), AuthenticationExceptionReasonCodes.FAILED );
		}
		return null;
	}

	public UserDetails loadUserByUsername( String username ) throws UsernameNotFoundException, DataAccessException
	{
		MemberView foundMember = userService.getMember( username );
		if ( foundMember == null )
		{
			throw new UsernameNotFoundException( "User " + username + " not found in database in digest authentication." );
		}

		if ( !foundMember.getType().equals( MemberTypeEnum.LOCAL_USER ) )
		{
			throw new UsernameNotFoundException( "User " + username + " is not a local user in database." );
		}
		List<GrantedAuthority> authorities = new ArrayList();
		for ( RightEnum right : foundMember.getAssembledRights() )
			authorities.add( new SimpleGrantedAuthority( "ROLE_" + right.toString() ) );
		for ( String appRight : foundMember.getAssembledAppRights() )
		{
			authorities.add( new SimpleGrantedAuthority( "ROLE_" + appRight ) );
		}
		return new DigestInfoView( foundMember.getName(), new String( Hex.encode( foundMember.getHash() ) ), authorities, foundMember.getDetailsView().isActive() );
	}

	class DigestInfoView implements UserDetails
	{
		private static final long serialVersionUID = 5047119660292418895L;

		private String username;
		private String password;
		private boolean isEnabled;
		private List<GrantedAuthority> authorities;

		public DigestInfoView( String username, String password, List<GrantedAuthority> authorities, boolean isEnabled )
		{
			this.username = username;
			this.password = password;
			this.authorities = authorities;
			this.isEnabled = isEnabled;
		}

		public List<GrantedAuthority> getAuthorities()
		{
			return authorities;
		}

		public String getPassword()
		{
			return password;
		}

		public String getUsername()
		{
			return username;
		}

		public boolean isAccountNonExpired()
		{
			return false;
		}

		public boolean isAccountNonLocked()
		{
			return false;
		}

		public boolean isCredentialsNonExpired()
		{
			return false;
		}

		public boolean isEnabled()
		{
			return isEnabled;
		}
	}

	public void setUserService( UserService userService )
	{
		this.userService = userService;
	}

	public void setLdapService( LDAPService ldapService )
	{
		this.ldapService = ldapService;
	}

	public void setEventRegistry( EventRegistry eventRegistry )
	{
		this.eventRegistry = eventRegistry;
	}

	public void setSessionService( SessionService sessionService )
	{
		this.sessionService = sessionService;
	}
}

