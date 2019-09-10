package com.marchnetworks.esm.system.user;

import com.marchnetworks.app.service.AppManager;
import com.marchnetworks.command.api.initialization.InitializationListener;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.topology.data.DefaultRootResource;
import com.marchnetworks.command.common.user.UserException;
import com.marchnetworks.command.common.user.data.MemberTypeEnum;
import com.marchnetworks.command.common.user.data.MemberView;
import com.marchnetworks.command.common.user.data.RightEnum;
import com.marchnetworks.command.common.user.data.UserDetailsView;
import com.marchnetworks.common.config.AppConfig;
import com.marchnetworks.common.config.AppConfigImpl;
import com.marchnetworks.common.config.ConfigProperty;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.esm.common.dao.MemberDAO;
import com.marchnetworks.esm.common.dao.ProfileDAO;
import com.marchnetworks.esm.common.model.MemberEntity;
import com.marchnetworks.esm.common.model.ProfileEntity;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;
import com.marchnetworks.management.user.UserService;
import com.marchnetworks.notification.service.NotificationService;
import com.marchnetworks.security.ldap.LDAPService;
import com.marchnetworks.shared.config.CommonConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class UserManagementInitService implements InitializationListener
{
	private static final Logger LOG = LoggerFactory.getLogger( UserManagementInitService.class );

	private ProfileDAO profileDAO;

	private MemberDAO memberDAO;
	private ResourceTopologyServiceIF resourceTopologyService;
	private CommonConfiguration configuration;
	private UserService userService;
	private LDAPService ldapService;
	private AppManager appManager;
	private NotificationService notificationService;

	public void onAppInitialized()
	{
		AppConfig ac = AppConfigImpl.getInstance();
		String adminName = ac.getProperty( ConfigProperty.ADMIN_USERNAME );
		String adminType = ac.getProperty( ConfigProperty.ADMIN_TYPE );

		List<ProfileEntity> profiles = profileDAO.findAll();
		if ( profiles.isEmpty() )
		{
			addDefaultSuperAdminProfile();
			addDefaultAdminProfile();
			addDefaultGuardProfile();
			addDefaultMaintainerProfile();
		}

		Long adminProfileId = null;
		for ( ProfileEntity aProfile : profiles )
		{
			if ( aProfile.getName().equals( "Super Administrator Profile" ) )
			{
				adminProfileId = aProfile.getId();
				Set<RightEnum> rootRights = new HashSet();
				for ( RightEnum right : RightEnum.values() )
				{
					rootRights.add( right );
				}

				Set<String> rootAppRights = new LinkedHashSet();

				List<String> appInfo = getAppManager().getAppGuids();

				for ( String app : appInfo )
				{
					rootAppRights.add( app );
				}

				aProfile.setAppRights( rootAppRights );
				aProfile.setRights( rootRights );
				profileDAO.flush();
			}
		}

		List<MemberEntity> members = memberDAO.findAllMembersByProfileId( adminProfileId );
		MemberTypeEnum theType = MemberTypeEnum.LDAP_USER;

		if ( adminType.equalsIgnoreCase( "local" ) )
		{
			theType = MemberTypeEnum.LOCAL_USER;
		}

		if ( members.isEmpty() )
		{
			createDefaultAdmin();
		}
		else if ( ( !( ( MemberEntity ) members.get( 0 ) ).getName().equals( adminName ) ) || ( !( ( MemberEntity ) members.get( 0 ) ).getMemberType().equals( theType ) ) )
		{
			MemberView aMemberView = new MemberView();
			aMemberView.setName( ( ( MemberEntity ) members.get( 0 ) ).getName() );
			try
			{
				userService.deleteMember( aMemberView );
			}
			catch ( UserException e )
			{
				LOG.debug( "Could not delete old super administrator member." );
			}
			createDefaultAdmin();
			notificationService.updateRecipientsAndUsername( ( ( MemberEntity ) members.get( 0 ) ).getName(), adminName );
		}
		if ( adminType.equalsIgnoreCase( "local" ) )
		{
			MemberView aView = new MemberView();
			aView.setName( adminName );
			userService.updateMemberPassword( aView, getConfiguration().getAdminPasswordFromConfig() );
		}
		LOG.debug( "Finished checks for User Management Service." );
	}

	private void addDefaultSuperAdminProfile()
	{
		ProfileEntity rootProfile = new ProfileEntity( "Super Administrator Profile" );
		rootProfile.setDescription( "Ability to manage system with full access rights" );
		rootProfile.setSuperAdmin( true );
		Set<RightEnum> rootRights = new HashSet();
		for ( RightEnum right : RightEnum.values() )
		{
			rootRights.add( right );
		}

		Set<String> rootAppRights = new LinkedHashSet();

		List<String> appInfo = getAppManager().getAppGuids();

		for ( String app : appInfo )
		{
			rootAppRights.add( app );
		}

		rootProfile.setAppRights( rootAppRights );
		rootProfile.setRights( rootRights );
		rootProfile.setSimplifiedUI( Boolean.valueOf( false ) );
		profileDAO.create( rootProfile );
	}

	private void addDefaultAdminProfile()
	{
		ProfileEntity rootProfile = new ProfileEntity( "Administrator" );
		rootProfile.setDescription( "An administrator of Command Enterprise" );
		rootProfile.setSuperAdmin( false );
		Set<RightEnum> rootRights = new HashSet();
		rootRights.add( RightEnum.LIVE_VIDEO );
		rootRights.add( RightEnum.ARCHIVE_VIDEO );
		rootRights.add( RightEnum.EXPORT_MP4 );
		rootRights.add( RightEnum.EXPORT_LOCAL );
		rootRights.add( RightEnum.PTZ_CONTROL );
		rootRights.add( RightEnum.HEALTH_MONITORING );
		rootRights.add( RightEnum.MASS_CONFIGURATION );
		rootRights.add( RightEnum.MANAGE_USERS );
		rootRights.add( RightEnum.MANAGE_APPS );
		rootRights.add( RightEnum.MANAGE_DEVICES );
		rootRights.add( RightEnum.MANAGE_SYSTEM_TREE );
		rootRights.add( RightEnum.MANAGE_LOGICAL_TREE );
		rootRights.add( RightEnum.MANAGE_ALARMS );
		rootRights.add( RightEnum.PERSONAL_TREE );
		rootRights.add( RightEnum.ACCESS_LOGS );
		rootRights.add( RightEnum.MANAGE_CASE_MANAGEMENT );
		rootRights.add( RightEnum.EXPORT_NATIVE );
		rootRights.add( RightEnum.PRIVACY_UNMASK );

		rootProfile.setRights( rootRights );
		rootProfile.setSimplifiedUI( Boolean.valueOf( false ) );

		profileDAO.create( rootProfile );
	}

	private void addDefaultGuardProfile()
	{
		ProfileEntity rootProfile = new ProfileEntity( "Guard" );
		rootProfile.setDescription( "Access to monitor live, control PTZ cameras, and review archive video" );
		rootProfile.setSuperAdmin( false );
		Set<RightEnum> rootRights = new HashSet();
		rootRights.add( RightEnum.LIVE_VIDEO );
		rootRights.add( RightEnum.ARCHIVE_VIDEO );
		rootRights.add( RightEnum.EXPORT_NATIVE );
		rootRights.add( RightEnum.PTZ_CONTROL );
		rootRights.add( RightEnum.MONITOR_LOGICAL_TREE );
		rootRights.add( RightEnum.PERSONAL_TREE );
		rootProfile.setRights( rootRights );
		rootProfile.setSimplifiedUI( Boolean.valueOf( true ) );
		profileDAO.create( rootProfile );
	}

	private void addDefaultMaintainerProfile()
	{
		ProfileEntity rootProfile = new ProfileEntity( "Maintainer" );
		rootProfile.setDescription( "Sufficient rights to maintain system." );
		rootProfile.setSuperAdmin( false );
		Set<RightEnum> rootRights = new HashSet();
		rootRights.add( RightEnum.LIVE_VIDEO );
		rootRights.add( RightEnum.HEALTH_MONITORING );
		rootRights.add( RightEnum.MASS_CONFIGURATION );
		rootRights.add( RightEnum.MANAGE_DEVICES );
		rootRights.add( RightEnum.MANAGE_SYSTEM_TREE );
		rootRights.add( RightEnum.MANAGE_LOGICAL_TREE );
		rootRights.add( RightEnum.PERSONAL_TREE );
		rootProfile.setRights( rootRights );
		rootProfile.setSimplifiedUI( Boolean.valueOf( true ) );
		profileDAO.create( rootProfile );
	}

	private void createDefaultAdmin()
	{
		String adminName = configuration.getProperty( ConfigProperty.ADMIN_USERNAME );
		String adminType = configuration.getProperty( ConfigProperty.ADMIN_TYPE );
		MemberView adminUserView = null;

		MemberEntity admin = memberDAO.findMemberByName( adminName );
		if ( admin != null )
		{
			try
			{
				MemberView aMemberView = new MemberView();
				aMemberView.setName( admin.getName() );
				userService.deleteMember( aMemberView );
			}
			catch ( UserException e )
			{
				LOG.debug( "Could not delete old super administrator member." );
			}
		}
		if ( !CommonAppUtils.isNullOrEmptyString( adminName ) )
		{

			LOG.debug( "Default admin account has not been setup, creating admin account" );
			try
			{
				if ( ( adminType == null ) || ( !adminType.contains( "local" ) ) )
				{
					try
					{
						adminUserView = ldapService.lookupUniqueUser( adminName );
						if ( adminUserView == null )
						{
							LOG.warn( "Unable to find username {} in LDAP, user either does not exist or not a unique entry." );
						}
					}
					catch ( Exception e )
					{
						LOG.warn( "Unable to find username {} in LDAP. Cause {}", new Object[] {adminName, e} );
					}
				}

				if ( adminUserView == null )
				{
					adminUserView = new MemberView();
				}

				if ( ( adminType != null ) && ( adminType.contains( "local" ) ) )
				{
					adminUserView.setType( MemberTypeEnum.LOCAL_USER );
					adminUserView.setDetailsView( new UserDetailsView() );
					adminUserView.setName( adminName );
					adminUserView.getDetailsView().setFullname( adminName );
				}
				else
				{
					adminUserView.setType( MemberTypeEnum.LDAP_USER );
				}

				adminUserView.getDetailsView().setActive( true );
				adminUserView.getDetailsView().setAdmin( true );

				ProfileEntity profile = profileDAO.findSuperAdminProfile();
				adminUserView.setProfileId( profile.getId() );

				adminUserView.setAssembledRights( profile.getRights() );
				adminUserView.setAssembledAppRights( profile.getAppRights() );
				adminUserView.setAssembledAppData( profile.getAppProfileData() );

				for ( DefaultRootResource rootResource : getResourceTopologyService().getDefaultRootResources() )
				{
					if ( "System".equalsIgnoreCase( rootResource.getKey() ) )
					{
						adminUserView.addSystemRoot( rootResource.getResource().getId() );
					}
					else
					{
						adminUserView.addLogicalRoot( rootResource.getResource().getId() );
					}
				}

				if ( adminUserView.getType().equals( MemberTypeEnum.LOCAL_USER ) )
				{
					String password = getConfiguration().getAdminPasswordFromConfig();
					userService.createMember( adminUserView, password );
				}
				else
				{
					userService.createMember( adminUserView, null );
				}
			}
			catch ( Exception ex )
			{
				LOG.error( "Failed to create default admin account: ", ex );
			}
		}
	}

	public CommonConfiguration getConfiguration()
	{
		return configuration;
	}

	public void setConfiguration( CommonConfiguration configuration )
	{
		this.configuration = configuration;
	}

	public ProfileDAO getProfileDAO()
	{
		return profileDAO;
	}

	public void setProfileDAO( ProfileDAO profileDAO )
	{
		this.profileDAO = profileDAO;
	}

	public MemberDAO getMemberDAO()
	{
		return memberDAO;
	}

	public void setMemberDAO( MemberDAO memberDAO )
	{
		this.memberDAO = memberDAO;
	}

	public ResourceTopologyServiceIF getResourceTopologyService()
	{
		if ( resourceTopologyService == null )
		{
			resourceTopologyService = ( ( ResourceTopologyServiceIF ) ApplicationContextSupport.getBean( "resourceTopologyServiceProxy_internal" ) );
		}
		return resourceTopologyService;
	}

	public void setUserService( UserService userService )
	{
		this.userService = userService;
	}

	public void setLdapService( LDAPService ldapService )
	{
		this.ldapService = ldapService;
	}

	public AppManager getAppManager()
	{
		if ( appManager == null )
		{
			appManager = ( ( AppManager ) ApplicationContextSupport.getBean( "appManager" ) );
		}
		return appManager;
	}

	public void setNotificationService( NotificationService notificationService )
	{
		this.notificationService = notificationService;
	}
}
