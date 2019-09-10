package com.marchnetworks.management.user;

import com.marchnetworks.app.data.App;
import com.marchnetworks.app.service.AppManager;
import com.marchnetworks.app.service.OsgiService;
import com.marchnetworks.audit.data.AuditEventNameEnum;
import com.marchnetworks.audit.data.AuditView;
import com.marchnetworks.audit.data.AuditView.Builder;
import com.marchnetworks.audit.events.AuditEvent;
import com.marchnetworks.com.util.AuditLogDetailsHelper;
import com.marchnetworks.command.api.security.AppAuthenticationService;
import com.marchnetworks.command.api.topology.GenericStorageCoreService;
import com.marchnetworks.command.api.user.UserCoreService;
import com.marchnetworks.command.api.user.rightexport.UserRightExportTemplate;
import com.marchnetworks.command.api.user.rightexport.UserRightTemplateProvider;
import com.marchnetworks.command.common.CollectionUtils;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.topology.ResourceRootType;
import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.topology.data.LinkResource;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.command.common.topology.data.ResourcePathNode;
import com.marchnetworks.command.common.user.UserException;
import com.marchnetworks.command.common.user.UserExceptionTypeEnum;
import com.marchnetworks.command.common.user.data.AppProfileData;
import com.marchnetworks.command.common.user.data.MemberTypeEnum;
import com.marchnetworks.command.common.user.data.MemberView;
import com.marchnetworks.command.common.user.data.ProfileView;
import com.marchnetworks.command.common.user.data.RightEnum;
import com.marchnetworks.command.common.user.data.UserDetailsView;
import com.marchnetworks.command.export.ExporterCoreService;
import com.marchnetworks.command.export.ExporterException;
import com.marchnetworks.common.config.ConfigProperty;
import com.marchnetworks.common.crypto.CryptoUtils;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.utils.CommonUtils;
import com.marchnetworks.esm.common.dao.MemberDAO;
import com.marchnetworks.esm.common.dao.ProfileDAO;
import com.marchnetworks.esm.common.model.MemberEntity;
import com.marchnetworks.esm.common.model.ProfileEntity;
import com.marchnetworks.esm.common.model.UserDetailsEntity;
import com.marchnetworks.management.data.FileStorageView;
import com.marchnetworks.management.file.service.FileStorageException;
import com.marchnetworks.management.file.service.FileStorageService;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;
import com.marchnetworks.management.topology.data.ResourceType;
import com.marchnetworks.management.user.events.ProfileCreatedEvent;
import com.marchnetworks.management.user.events.ProfileRemovedEvent;
import com.marchnetworks.management.user.events.ProfileUpdatedEvent;
import com.marchnetworks.management.user.events.UserCreatedEvent;
import com.marchnetworks.management.user.events.UserLogoffEvent;
import com.marchnetworks.management.user.events.UserRemovedEvent;
import com.marchnetworks.management.user.events.UserUpdatedEvent;
import com.marchnetworks.security.ldap.LDAPService;
import com.marchnetworks.server.event.EventRegistry;
import com.marchnetworks.shared.config.CommonConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.CommunicationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserServiceImpl implements UserService, UserCoreService, UserRightTemplateProvider
{
	private static final Logger LOG = LoggerFactory.getLogger( UserServiceImpl.class );

	private static final int DEFAULT_LOGIN_TIMEOUT = 24;

	private EventRegistry eventRegistry;

	private ProfileDAO profileDAO;
	private MemberDAO memberDAO;
	private FileStorageService fileService;
	private ResourceTopologyServiceIF resourceTopologyService;
	private GenericStorageCoreService genericStorageService;
	private LDAPService ldapService;
	private ExporterCoreService exporterService;
	private AppManager appManager;
	private CommonConfiguration commonConfiguration;
	private OsgiService osgiService;
	private static Map<String, Set<String>> appsRights = Collections.synchronizedMap( new HashMap() );

	public MemberView createMember( MemberView member, String aPassword ) throws UserException
	{
		MemberView returnMemberView = null;

		validateMemberView( member );

		String activeUser = getActiveUser();
		if ( activeUser != null )
		{
			MemberView currentUser = getUser( activeUser );

			if ( ( !currentUser.isTopLevelUser() ) && ( !Collections.disjoint( member.getAllAssignedRoots(), currentUser.getAllRoots( false ) ) ) )
			{
				throw new UserException( "Member " + currentUser + " cannot assign their own root(s) to " + member.getName(), UserExceptionTypeEnum.CANNOT_SET_TERRITORY_ROOTS_TO_USER );
			}
		}

		MemberEntity memberModel = fillMember( member, aPassword, null, null );
		memberDAO.create( memberModel );

		UserCreatedEvent userCreated = new UserCreatedEvent( memberModel.getName() );

		eventRegistry.sendEventAfterTransactionCommits( userCreated );
		returnMemberView = memberModel.toDataObject();

		LOG.info( "User saved: " + returnMemberView.getName() );

		auditMember( returnMemberView, AuditEventNameEnum.USER_CREATE );

		return returnMemberView;
	}

	private void validateMemberView( MemberView member ) throws UserException
	{
		MemberEntity aMember = memberDAO.findMemberByName( member.getName() );

		if ( aMember != null )
		{
			if ( aMember.getMemberType().equals( MemberTypeEnum.GROUP_USER ) )
			{
				deleteMember( member );
				memberDAO.flush();
			}
			else
			{
				throw new UserException( "Member " + member.getName() + " already exists on DB." );
			}
		}
		else
		{
			if ( ( member.getName() == null ) || ( member.getName().trim().isEmpty() ) )
				throw new UserException( "Member name cannot be empty" );
			if ( member.getName().equalsIgnoreCase( "_server_" ) )
				throw new UserException( "Member _server_ could not be created. The name is invalid" );
			if ( member.getType() == null )
				throw new UserException( "Member " + member.getName() + " does not have a member type specified." );
			if ( ( member.getType().equals( MemberTypeEnum.LOCAL_USER ) ) || ( member.getType().equals( MemberTypeEnum.LDAP_USER ) ) || ( member.getType().equals( MemberTypeEnum.GROUP ) ) )
			{
				Long profileId = member.getProfileId();
				if ( ( member.getProfileId() == null ) || ( profileId == null ) || ( profileDAO.findById( profileId ) == null ) )
				{
					throw new UserException( "Member " + member.getName() + " requires a profile and does not have one associated." );
				}
			}
		}
	}

	private MemberEntity fillMember( MemberView memberView, String aPassword, byte[] hashedPassword, byte[] salt )
	{
		MemberEntity memberModel = new MemberEntity();

		if ( ( memberView.getPersonalRoot() == null ) && ( !memberView.getType().equals( MemberTypeEnum.GROUP ) ) )
		{
			Resource res = getResourceTopologyService().createPersonalResource( memberView.getName() );
			memberView.setPersonalRoot( res.getId() );
		}

		if ( !memberView.getType().equals( MemberTypeEnum.GROUP ) )
		{
			if ( memberView.getHash() == null )
			{
				byte[] hash = getHash( memberView, aPassword );
				memberView.setHash( hash );
			}
			else
			{
				memberView.setHash( hashedPassword );
				memberView.setSalt( salt );
			}
		}

		if ( ( memberView.getType() == MemberTypeEnum.LOCAL_USER ) || ( memberView.getType() == MemberTypeEnum.GROUP ) )
		{
			ProfileView profile = getProfile( memberView.getProfileId() );
			memberView.setAssembledRights( profile.getProfileRights() );
			memberView.setAssembledAppRights( profile.getProfileAppRights() );
			memberView.setAssembledAppData( profile.getAppProfileData() );
		}

		memberModel.readFromDataObject( memberView );

		if ( ( !memberView.getType().equals( MemberTypeEnum.GROUP ) ) && ( memberView.getDetailsView() != null ) )
		{
			UserDetailsEntity theDetails = new UserDetailsEntity();
			theDetails.readFromDataObject( memberView.getDetailsView() );
			memberModel.setUserDetails( theDetails );
		}

		if ( memberView.getProfileId() != null )
		{
			memberModel.setProfileId( memberView.getProfileId() );
		}

		return memberModel;
	}

	public MemberView updateMember( MemberView member, String aPassword ) throws UserException
	{
		LOG.debug( "Web Service update member" );
		if ( member == null )
		{
			throw new UserException( "Null MemberView passed on updateMember call.", UserExceptionTypeEnum.USER_NOT_FOUND );
		}

		String currentUserName = getActiveUser();
		MemberView currentUser = getUser( currentUserName );

		boolean passwordChanged = false;
		boolean modifyingSelf = currentUserName == member.getName();

		if ( aPassword != null )
		{
			passwordChanged = true;
		}

		boolean forceLogoff = false;
		MemberEntity memberModel = memberDAO.findMemberByName( member.getName() );

		if ( memberModel == null )
		{
			LOG.warn( "Member {} not found on DB.", member.getName() );
			throw new UserException( "User " + member.getName() + " not found." );
		}

		if ( !hasUserAccess( memberModel, true ) )
		{
			throw new UserException( "Current member does not have access rights to change member " + member.getName() );
		}

		member.addAssembledSystemRoots( member.getSystemRoots() );
		member.addAssembledLogicalRoots( member.getLogicalRoots() );

		Set<Long> systemIds = removeRedundantResources( member.getSystemRoots() );
		Set<Long> logicalIds = removeRedundantResources( member.getLogicalRoots() );
		Set<Long> dbSystemResources = memberModel.getSystemRoots();
		Set<Long> dbLogicalResources = memberModel.getLogicalRoots();
		boolean systemResourcesChanged = !systemIds.equals( dbSystemResources );
		boolean logicalResourcesChanged = !logicalIds.equals( dbLogicalResources );
		if ( ( systemResourcesChanged ) || ( logicalResourcesChanged ) )
		{
			if ( modifyingSelf )
			{
				throw new UserException( "User: " + member.getName() + " cannot modify their own resources", UserExceptionTypeEnum.CANNOT_MODIFY_SELF_TERRITORY_ASSIGMENT );
			}

			if ( !currentUser.isTopLevelUser() )
			{
				if ( !Collections.disjoint( member.getAllRoots( false ), currentUser.getAllRoots( false ) ) )
				{
					throw new UserException( "Member " + currentUser + " cannot assign their own root(s) to " + member.getName(), UserExceptionTypeEnum.CANNOT_SET_TERRITORY_ROOTS_TO_USER );
				}
			}

			forceLogoff = true;
		}

		String certId = null;
		String oldCertId = null;
		if ( ( member.getDetailsView() != null ) && ( member.getDetailsView().getCertificateId() != null ) )
		{
			certId = member.getDetailsView().getCertificateId();
		}
		if ( ( memberModel.getUserDetails() != null ) && ( memberModel.getUserDetails().getCertificate() != null ) )
		{
			oldCertId = memberModel.getUserDetails().getCertificate();
		}
		if ( ( certId != null ) && ( !certId.equals( oldCertId ) ) )
		{
			if ( modifyingSelf )
			{
				throw new UserException( "User: " + member.getName() + " cannot modify their own certificate", UserExceptionTypeEnum.CANNOT_MODIFY_SELF_CERTIFICATE_ASSIGMENT );
			}
			forceLogoff = true;
		}

		if ( ( memberModel.getUserDetails() != null ) && ( member.getDetailsView() != null ) && ( member.getDetailsView().getCardLogout() != memberModel.getUserDetails().getCardLogout() ) )
		{
			forceLogoff = true;
		}

		if ( ( member.getDetailsView() != null ) && ( member.getDetailsView().getCertificateId() != null ) )
		{
			try
			{
				getFileService().getFileStorage( member.getDetailsView().getCertificateId() );
			}
			catch ( FileStorageException e )
			{
				throw new UserException( "Certificate not found when updating member, fileId " + member.getDetailsView().getCertificateId() );
			}
		}
		MemberEntity currentMember;

		if ( ( currentUser == null ) || ( currentUser.equals( member.getName() ) ) )
		{
			currentMember = memberModel;
		}
		else
		{
			currentMember = memberDAO.findMemberByName( currentUserName );
		}
		if ( currentMember == null )
		{
			throw new UserException( "Member " + currentUser + " does not exist in database and therefore cannot update members." );
		}

		if ( member.hasResources() )
		{
			for ( Long resourceId : member.getAllRoots( false ) )
			{
				try
				{
					getResourceTopologyService().getResource( resourceId );
				}
				catch ( TopologyException e )
				{
					throw new UserException( "User " + member.getName() + " could not add resource id " + resourceId + " as it does not exist in the database.", e );
				}

				if ( ( systemResourcesChanged ) && ( systemIds.contains( resourceId ) ) )
				{
					if ( !hasResourceAccess( resourceId, systemIds ) )
					{
						throw new UserException( "Member " + currentUser + " does not have access to system resource " + resourceId + " to change member " + member.getName() );
					}
				}
				else if ( ( logicalResourcesChanged ) && ( logicalIds.contains( resourceId ) ) && ( !hasResourceAccess( resourceId, logicalIds ) ) )
				{
					throw new UserException( "Member " + currentUser + " does not have access to logical resource " + resourceId + " to change member " + member.getName() );
				}
			}
		}

		if ( ( !memberModel.getMemberType().equals( MemberTypeEnum.GROUP ) ) && ( memberModel.getUserDetails().isActive() ) && ( !member.getDetailsView().isActive() ) )
		{
			forceLogoff = true;
		}

		if ( passwordChanged )
		{
			updateMemberPassword( member, aPassword );
		}
		else
		{
			member.setSalt( memberModel.getSalt() );
			member.setHash( memberModel.getHash() );
		}

		Long aProfile = memberModel.getProfileId();
		if ( ( aProfile != null ) && ( !aProfile.equals( member.getProfileId() ) ) )
		{
			if ( modifyingSelf )
			{
				throw new UserException( "User: " + member.getName() + " cannot change their own profile assignment", UserExceptionTypeEnum.CANNOT_MODIFY_SELF_PROFILE_ASSIGMENT );
			}
			forceLogoff = true;
			if ( ( member.getType() == MemberTypeEnum.LOCAL_USER ) || ( member.getType() == MemberTypeEnum.GROUP ) )
			{
				ProfileView profile = getProfile( member.getProfileId() );
				member.setAssembledRights( profile.getProfileRights() );
				member.setAssembledAppRights( profile.getProfileAppRights() );
				member.setAssembledAppData( profile.getAppProfileData() );
			}
			memberModel.setProfileId( member.getProfileId() );
		}

		if ( ( modifyingSelf ) && ( currentUser.getDetailsView().isActive() != member.getDetailsView().isActive() ) )
		{
			throw new UserException( "User: " + member.getName() + " cannot modify their own status", UserExceptionTypeEnum.CANNOT_MODIFY_SELF_STATUS );
		}

		memberModel.readFromDataObject( member );

		try
		{
			if ( !memberModel.getMemberType().equals( MemberTypeEnum.GROUP ) )
			{
				if ( forceLogoff )
				{
					UserLogoffEvent userLogoff = new UserLogoffEvent( memberModel.getName() );
					eventRegistry.sendEventAfterTransactionCommits( userLogoff );
				}
			}
			else
			{
				ArrayList<String> groupIds = new ArrayList();
				groupIds.add( memberModel.getId().toString() );
				List<MemberEntity> membersInGroup = memberDAO.findByGroupId( memberModel.getId() );
				logoffMembers( membersInGroup );
			}

			UserUpdatedEvent userUpdated = new UserUpdatedEvent( memberModel.getName() );
			eventRegistry.sendEventAfterTransactionCommits( userUpdated );

			MemberView memberView = memberModel.toDataObject();
			auditMember( memberView, AuditEventNameEnum.USER_UPDATE, passwordChanged );

			return memberView;
		}
		catch ( Exception e )
		{
			LOG.warn( "Error updating user: " + member.getDetailsView().getFullname(), e );
			throw new UserException( "Could not update member " + member.getName() );
		}
	}

	private boolean hasResourceAccess( Long resourceId, Set<Long> rootIds )
	{
		for ( Long rootId : rootIds )
		{
			if ( getResourceTopologyService().isChild( rootId, resourceId ) )
			{
				return true;
			}
		}
		return false;
	}

	private void logoffMembers( List<MemberEntity> members )
	{
		for ( MemberEntity member : members )
		{
			UserLogoffEvent userLogoff = new UserLogoffEvent( member.getName() );
			eventRegistry.sendEventAfterTransactionCommits( userLogoff );
		}
	}

	public MemberView getUser( String userName ) throws UserException
	{
		MemberEntity foundMember = memberDAO.findMemberByName( CommonUtils.getShortUsername( userName ) );

		if ( foundMember.getMemberType().equals( MemberTypeEnum.GROUP ) )
		{
			throw new UserException( "Error retreiving authenticated user object: object not found, or attempting to login as a group." );
		}

		MemberView foundMemberView = foundMember.toDataObject();

		return foundMemberView;
	}

	public boolean isSuperAdmin( String userName )
	{
		ProfileView profile;

		try
		{
			profile = getProfileForUser( userName );
		}
		catch ( UserException e )
		{
			return false;
		}

		return profile == null ? false : profile.isSuperAdmin();
	}

	public boolean hasUserAccess( String userName ) throws UserException
	{
		MemberEntity foundMember = memberDAO.findMemberByName( userName );
		if ( foundMember == null )
		{
			throw new UserException( "Member " + userName + " not found on database." );
		}

		return hasUserAccess( foundMember, false );
	}

	private boolean hasUserAccess( MemberEntity member, boolean includeGroupUsers ) throws UserException
	{
		String currentUser = getActiveUser();
		if ( CommonAppUtils.isNullOrEmptyString( currentUser ) )
		{
			return true;
		}
		MemberView memberView = member.toDataObject();
		MemberView currentMember = getUser( currentUser );
		return hasUserAccess( currentMember, memberView, includeGroupUsers );
	}

	private boolean hasUserAccess( MemberView currentMember, MemberView member, boolean includeGroupUsers ) throws UserException
	{
		ProfileView currentMemberProfile = getProfile( currentMember.getProfileId() );
		ProfileView memberProfile = getProfile( member.getProfileId() );

		if ( ( currentMemberProfile != null ) && ( currentMemberProfile.isSuperAdmin() ) )
		{
			return true;
		}

		if ( ( memberProfile != null ) && ( memberProfile.isSuperAdmin() ) )
		{
			return false;
		}

		if ( member.getName().equals( currentMember.getName() ) )
		{
			return true;
		}

		if ( ( !currentMember.hasResources() ) && ( !member.hasResources() ) )
		{
			return false;
		}

		if ( ( !includeGroupUsers ) && ( member.getType() == MemberTypeEnum.GROUP_USER ) )
		{
			return false;
		}

		if ( !member.hasResources() )
		{
			return true;
		}

		Set<Long> currentMemberRoots = currentMember.getAllRoots( false );
		Set<Long> memberRoots = member.getAllRoots( false );

		if ( ( !currentMember.isTopLevelUser() ) && ( !member.isTopLevelUser() ) && ( !Collections.disjoint( currentMemberRoots, memberRoots ) ) )
		{
			return false;
		}

		for ( Long root : memberRoots )
		{
			if ( !hasResourceAccess( root, currentMemberRoots ) )
			{
				return false;
			}
		}

		return true;
	}

	public ProfileView createProfile( ProfileView profileView ) throws UserException
	{
		checkForTopLevelAccess();

		ProfileEntity profile = new ProfileEntity();
		profile.readFromDataObject( profileView );
		profile.removeAllRights();
		profile.removeAllAppRights();
		for ( RightEnum re : profileView.getProfileRights() )
		{
			profile.addRight( re );
		}
		for ( String ar : profileView.getProfileAppRights() )
		{
			profile.addAppRight( ar );
		}

		profileDAO.create( profile );
		LOG.debug( "Successful creation of new Profile" );

		ProfileCreatedEvent profileCreated = new ProfileCreatedEvent();
		profileCreated.setProfileID( profile.getId().toString() );

		eventRegistry.sendEventAfterTransactionCommits( profileCreated );

		ProfileView result = profile.toDataObject();
		auditProfile( result, AuditEventNameEnum.PROFILE_CREATE );
		return result;
	}

	public void addAppRights( String appId, Set<String> rights ) throws UserException
	{
		if ( LOG.isDebugEnabled() )
		{
			LOG.debug( "Application {} is going to add its specific rights, which are {}", appId, rights );
		}

		appsRights.put( appId, rights );

		for ( ProfileView profile : listAllProfiles() )
		{
			boolean updateProfile = false;
			Iterator<AppProfileData> iterator;
			if ( profile.isSuperAdmin() )
			{
				Set<AppProfileData> appProfileData = profile.getAppProfileData();

				if ( ( appProfileData == null ) || ( appProfileData.isEmpty() ) || ( !appProfileDataContainsAppId( appProfileData, appId ) ) )
				{
					profile.getAppProfileData().add( new AppProfileData( appId, rights ) );
					updateProfile = true;
				}
				else
				{
					for ( iterator = appProfileData.iterator(); iterator.hasNext(); )
					{
						AppProfileData data = ( AppProfileData ) iterator.next();
						if ( data.getAppId().equals( appId ) )
						{
							if ( !data.getAppRights().equals( rights ) )
							{
								data.setAppRights( rights );
								updateProfile = true;
							}

						}
					}
				}
			}
			else if ( ( profile.getAppProfileData() != null ) && ( !profile.getAppProfileData().isEmpty() ) )
			{
				for ( AppProfileData data : profile.getAppProfileData() )
				{
					if ( ( data.getAppId().equals( appId ) ) && ( data.getAppRights().retainAll( rights ) ) )
					{
						updateProfile = true;
						break;
					}
				}
			}

			if ( updateProfile )
			{
				updateProfile( profile, false );
			}
		}
	}

	private boolean appProfileDataContainsAppId( Set<AppProfileData> appProfileData, String appId )
	{
		for ( Iterator<AppProfileData> iterator = appProfileData.iterator(); iterator.hasNext(); )
		{
			AppProfileData data = ( AppProfileData ) iterator.next();
			if ( data.getAppId().equals( appId ) )
			{
				return true;
			}
		}
		return false;
	}

	public void addNewAppRights( String appId, Set<String> rights ) throws UserException
	{
		List<ProfileView> profiles = listAllProfiles();

		for ( ProfileView profile : profiles )
		{
			if ( ( !profile.isSuperAdmin() ) && ( profile.getProfileAppRights().contains( appId ) ) )
			{
				Set<String> existingAppRights = profile.getAppRights( appId );
				if ( existingAppRights == null )
				{
					profile.getAppProfileData().add( new AppProfileData( appId, rights ) );
				}
				else
				{
					existingAppRights.addAll( rights );
				}
				updateProfile( profile, false );
			}
		}
	}

	public ProfileView updateProfile( ProfileView profileView, boolean accessCheck ) throws UserException
	{
		if ( accessCheck )
		{
			checkForTopLevelAccess();
		}

		ProfileEntity profile = ( ProfileEntity ) profileDAO.findById( Long.valueOf( profileView.getProfileId() ) );

		if ( profile == null )
		{
			return createProfile( profileView );
		}

		profile.setName( profileView.getName() );
		profile.setDescription( profileView.getDescription() );
		profile.setSuperAdmin( profileView.isSuperAdmin() );
		profile.setSimplifiedUI( Boolean.valueOf( profileView.isSimplifiedUI() ) );

		Set<RightEnum> original = new HashSet();
		Set<String> originalAppRights = new HashSet();

		original.addAll( profile.getRights() );
		originalAppRights.addAll( profile.getAppRights() );

		profile.removeAllRights();

		for ( RightEnum re : profileView.getProfileRights() )
		{
			profile.addRight( re );
		}

		profile.setAppRights( profileView.getProfileAppRights() );

		boolean equalAppData = true;
		Set<String> oldAppData = new HashSet();
		Set<String> newAppData = new HashSet();

		if ( profile.getAppProfileData() != null )
		{
			for ( AppProfileData oldData : profile.getAppProfileData() )
			{
				if ( oldData.getAppRights() != null )
				{
					oldAppData.addAll( oldData.getAppRights() );
				}
			}
		}

		if ( profileView.getAppProfileData() != null )
		{
			for ( AppProfileData newData : profileView.getAppProfileData() )
			{
				if ( newData.getAppRights() != null )
				{
					newAppData.addAll( newData.getAppRights() );
				}
			}
		}

		equalAppData = oldAppData.equals( newAppData );

		profile.setAppProfileData( profileView.getAppProfileData() );

		if ( ( !original.equals( profile.getRights() ) ) || ( !originalAppRights.equals( profile.getAppRights() ) ) || ( !equalAppData ) )
		{
			Long id = Long.valueOf( profileView.getProfileId() );
			logoffUsersForProfiles( Collections.singletonList( id ) );
		}

		ProfileUpdatedEvent profileUpdated = new ProfileUpdatedEvent();
		profileUpdated.setProfileID( profile.getId().toString() );

		eventRegistry.sendEventAfterTransactionCommits( profileUpdated );

		ProfileView result = profile.toDataObject();
		auditProfile( result, AuditEventNameEnum.PROFILE_UPDATE );
		return result;
	}

	public ProfileView deleteProfile( ProfileView profileView ) throws UserException
	{
		checkForTopLevelAccess();
		try
		{
			ProfileEntity profile = ( ProfileEntity ) profileDAO.findById( Long.valueOf( profileView.getProfileId() ) );
			if ( profile == null )
			{
				LOG.warn( "Profile {} not found on DB.", profileView.getProfileId() );
				throw new UserException( "Profile " + profileView.getProfileId() + " not found." );
			}
			profileDAO.delete( profile );

			ProfileRemovedEvent profileRemoved = new ProfileRemovedEvent();
			profileRemoved.setProfileID( profile.getId().toString() );

			eventRegistry.sendEventAfterTransactionCommits( profileRemoved );

			ProfileView result = profile.toDataObject();
			auditProfile( result, AuditEventNameEnum.PROFILE_REMOVED );
			return result;
		}
		catch ( Exception e )
		{
			LOG.warn( "Error deleting profile ID: " + Long.valueOf( profileView.getProfileId() ) );
			throw new UserException( "Could not delete profile " + profileView );
		}
	}

	public ProfileView getProfile( Long profileId )
	{
		if ( profileId == null )
		{
			return null;
		}
		try
		{
			LOG.debug( "Querying profile ID: " + profileId );
			ProfileEntity profileFound = ( ProfileEntity ) profileDAO.findByIdDetached( Long.valueOf( profileId.longValue() ) );

			return profileFound.toDataObject();
		}
		catch ( Exception e )
		{
			LOG.warn( "Error querying profile ID: " + profileId );
		}
		return null;
	}

	public void addRightToProfile( String profileName, RightEnum right )
	{
		ProfileEntity profile = profileDAO.findByName( profileName );
		if ( profile != null )
		{
			Set<RightEnum> rights = profile.getRights();
			rights.add( right );
		}
	}

	public void processAppInstalled( String guid )
	{
		ProfileEntity profile = profileDAO.findSuperAdminProfile();
		App app = appManager.getApp( guid );

		if ( app.getHasClientApp() )
		{
			profile.addAppRight( guid );
		}
	}

	public void processAppStarted( String guid )
	{
		List<ProfileEntity> profiles = profileDAO.findAllDetached();

		List<Long> ids = new ArrayList();
		for ( ProfileEntity profile : profiles )
		{
			Set<String> rights = profile.getAppRights();
			if ( rights.contains( guid ) )
			{
				ids.add( profile.getId() );
			}
		}

		logoffUsersForProfiles( ids );
	}

	private void logoffUsersForProfiles( List<Long> profileIds )
	{
		if ( profileIds.isEmpty() )
		{
			return;
		}
		List<MemberEntity> membersWithProfile = memberDAO.findAllMembersByProfileIdsDetached( profileIds );

		if ( membersWithProfile != null )
		{
			for ( MemberEntity member : membersWithProfile )
			{
				if ( member.getMemberType().equals( MemberTypeEnum.GROUP ) )
				{
					List<MemberEntity> membersInGroup = memberDAO.findByGroupId( member.getId() );
					logoffMembers( membersInGroup );
				}
				UserLogoffEvent userLogoff = new UserLogoffEvent( member.getName() );
				eventRegistry.sendEventAfterTransactionCommits( userLogoff );
			}
		}
	}

	public RightEnum[] getAllProfileRightsEnum()
	{
		return RightEnum.values();
	}

	public List<ProfileView> listAllProfiles()
	{
		try
		{
			List<ProfileEntity> list = profileDAO.findAll();
			List<ProfileView> viewList = new ArrayList();

			for ( int i = 0; i < list.size(); i++ )
			{
				ProfileView tempView = ( ( ProfileEntity ) list.get( i ) ).toDataObject();
				viewList.add( tempView );
			}
			return viewList;
		}
		catch ( Exception e )
		{
			LOG.warn( "Error listing system profiles ", e );
		}
		return null;
	}

	private String getActiveUser()
	{
		String authenticatedUser = CommonAppUtils.getUsernameFromSecurityContext();
		return authenticatedUser;
	}

	public MemberView deleteMember( MemberView member ) throws UserException
	{
		LOG.debug( "Web Service delete user" );
		if ( member == null )
		{
			throw new UserException( "Null MemberView passed on deleteMember call." );
		}

		String currentMember = getActiveUser();

		if ( member.getName().equals( currentMember ) )
		{
			String error = "Current user cannot delete own user record.";
			LOG.warn( error );
			throw new UserException( error );
		}

		MemberEntity memberModel = memberDAO.findMemberByName( member.getName() );

		if ( memberModel == null )
		{
			LOG.warn( "User {} not found on DB.", member.getName() );
			throw new UserException( "User " + member.getName() + " not found." );
		}

		if ( ( !memberModel.getMemberType().equals( MemberTypeEnum.GROUP_USER ) ) && ( !hasUserAccess( memberModel, false ) ) )
		{
			throw new UserException( "Current member does not have access rights to delete member " + member.getName() );
		}
		try
		{
			if ( memberModel.getPersonalResource() != null )
			{
				Resource resource = getResourceTopologyService().getResource( memberModel.getPersonalRootId() );
				if ( resource != null )
				{
					getResourceTopologyService().removeUserPersonalRoot( resource );
				}
			}
		}
		catch ( TopologyException e )
		{
			LOG.warn( "Error deleting user: " + member.getDetailsView().getFullname(), e );
			throw new UserException( "Error removing personal resource data for  " + member.getName() );
		}

		memberDAO.deleteMember( memberModel );

		genericStorageService.deleteUserStore( memberModel.getName() );

		if ( memberModel.getMemberType().equals( MemberTypeEnum.GROUP ) )
		{
			List<MemberEntity> membersInGroup = memberDAO.findByGroupId( memberModel.getId() );

			for ( MemberEntity memberEntity : membersInGroup )
			{
				memberEntity.removeGroup( memberModel.getId() );
			}
			logoffMembers( membersInGroup );
		}

		UserRemovedEvent userRemoved = new UserRemovedEvent( memberModel.getName() );
		eventRegistry.sendEventAfterTransactionCommits( userRemoved );

		UserLogoffEvent userLogoff = new UserLogoffEvent( memberModel.getName() );
		eventRegistry.sendEventAfterTransactionCommits( userLogoff );

		MemberView memberView = memberModel.toDataObject();
		auditMember( memberView, AuditEventNameEnum.USER_REMOVED );

		return memberView;
	}

	public MemberView getMember( String userName )
	{
		LOG.debug( "Web Service get/retrieve User instance" );
		MemberEntity foundMember = null;
		try
		{
			foundMember = memberDAO.findMemberByName( CommonUtils.getShortUsername( userName ) );
			if ( foundMember == null )
			{
				LOG.warn( "Error retrieving user instance: " + userName );
				return null;
			}

			if ( hasUserAccess( foundMember, false ) )
			{
				return foundMember.toDataObject();
			}
			return null;
		}
		catch ( UserException e )
		{
			LOG.debug( "Error retrieving user instance: " + userName, e );
		}
		return null;
	}

	public List<MemberView> searchLdap( String userName, int maxResults ) throws UserException
	{
		try
		{
			List<MemberView> foundUsers = ldapService.lookupUsers( userName, maxResults );
			if ( foundUsers == null )
			{
				return null;
			}
			return foundUsers;
		}
		catch ( Exception ce )
		{
			LOG.warn( "Error searching users from LDAP by User Common Name due to {}", ce.getCause() != null ? ce.getCause().getMessage() : ce.getMessage() );
			if ( ( ce.getCause() != null ) && ( ce.getCause().getClass().equals( CommunicationException.class ) ) )
			{
				throw new UserException( "Could not communicate with LDAP server." );
			}
			throw new UserException( "Error searching users from LDAP." );
		}
	}

	public List<MemberView> listAllMembers()
	{
		LOG.debug( "Web Service list all members" );
		try
		{
			List<MemberEntity> list = memberDAO.findAll();
			if ( ( list != null ) && ( !list.isEmpty() ) )
			{
				LOG.debug( "User list first entry {} ", list.get( 0 ) );
			}
			List<MemberView> viewList = new ArrayList();
			for ( MemberEntity member : list )
			{
				viewList.add( member.toDataObject() );
			}
			return viewList;
		}
		catch ( Exception e )
		{
			LOG.warn( "Error listing users of the system ", e );
		}
		return null;
	}

	public List<MemberView> listNotDeletedMembers( boolean includeGroupUsers ) throws UserException
	{
		List<MemberEntity> originalMemberList = memberDAO.findAll();

		LOG.debug( "Web Service list not deleted users" );
		String currentUser = getActiveUser();
		MemberEntity member = memberDAO.findMemberByName( currentUser );
		if ( member == null )
		{
			throw new UserException( "Could not find user: " + currentUser );
		}
		List<MemberView> accessibleMemberList = new ArrayList();
		for ( MemberEntity tempMember : originalMemberList )
		{
			MemberView memberView = tempMember.toDataObject();
			if ( hasUserAccess( member.toDataObject(), memberView, includeGroupUsers ) )
			{
				accessibleMemberList.add( memberView );
			}
		}
		return accessibleMemberList;
	}

	public String[] listUserAccess() throws UserException
	{
		String currentUser = getActiveUser();
		MemberEntity currentMember = memberDAO.findMemberByName( currentUser );
		MemberView memberView = currentMember.toDataObject();

		if ( memberView == null )
		{
			throw new UserException( "Could not find login cache for user " + currentUser );
		}

		List<MemberEntity> members = memberDAO.findAll();
		List<String> visibleMembers = new ArrayList();
		for ( MemberEntity member : members )
		{
			if ( hasUserAccess( memberView, member.toDataObject(), true ) )
			{
				visibleMembers.add( member.getName() );
			}
		}
		return ( String[] ) visibleMembers.toArray( new String[visibleMembers.size()] );
	}

	public void removeTerritoryResourceFromMembers( Long resourceId )
	{
		if ( resourceId != null )
		{
			List<MemberEntity> membersWithResource = memberDAO.findAllMembersByRootResource( resourceId );
			if ( membersWithResource != null )
			{
				for ( MemberEntity aMember : membersWithResource )
				{
					if ( aMember.getMemberType().equals( MemberTypeEnum.GROUP ) )
					{
						List<MemberEntity> logoffMembers = memberDAO.findByGroupId( aMember.getId() );
						logoffMembers( logoffMembers );
					}
					LOG.debug( "Removing resource for resource id {}", resourceId );
					aMember.removeResource( resourceId );

					UserLogoffEvent userLogoff = new UserLogoffEvent( aMember.getName() );
					eventRegistry.sendEventAfterTransactionCommits( userLogoff );
					UserUpdatedEvent userUpdated = new UserUpdatedEvent( aMember.getName() );
					eventRegistry.sendEventAfterTransactionCommits( userUpdated );
				}
			}
		}
	}

	public void replaceChildResource( Long parentResourceId, Long childResourceId )
	{
		List<MemberEntity> membersWithBothResources = memberDAO.findAllMembersByRootResource( parentResourceId );

		for ( MemberEntity member : membersWithBothResources )
		{
			if ( member.removeResource( childResourceId ) )
			{
				UserLogoffEvent userLogoff = new UserLogoffEvent( member.getName() );
				eventRegistry.sendEventAfterTransactionCommits( userLogoff );
				UserUpdatedEvent userUpdated = new UserUpdatedEvent( member.getName() );
				eventRegistry.sendEventAfterTransactionCommits( userUpdated );
			}
		}
	}

	public void verifyMembersPersonalResources()
	{
		LOG.debug( "Updating users' personal tree" );
		List<MemberEntity> membersWithPersonalTree = memberDAO.findAllMembersWithPersonalResource();
		for ( MemberEntity member : membersWithPersonalTree )
		{
			updatePersonalResourcesFromMember( member );
		}
		LOG.debug( "Finished updating users' personal tree" );
	}

	public void verifyMemberPersonalResources( String memberName )
	{
		LOG.debug( "Updating user {} personal tree", memberName );
		MemberEntity member = memberDAO.findMemberByName( memberName );
		if ( member != null )
		{
			updatePersonalResourcesFromMember( member );
		}
		LOG.debug( "Finished updating user {} personal tree", memberName );
	}

	private void updatePersonalResourcesFromMember( MemberEntity member )
	{
		if ( member.getType() == MemberTypeEnum.GROUP )
		{
			return;
		}

		if ( member.getUserDetails().isSuperAdmin() )
		{
			return;
		}

		if ( member.getPersonalResource() == null )
		{
			return;
		}

		MemberView memberView = member.toDataObject();

		try
		{
			Resource personalRoot = getResourceTopologyService().getResource( memberView.getPersonalRoot() );
			if ( personalRoot == null )
			{
				return;
			}

			List<Resource> personalTreeResources = personalRoot.createFilteredResourceList( new Class[] {LinkResource.class} );
			if ( personalTreeResources.isEmpty() )
			{
				return;
			}

			Set<Long> userResourceIds = new HashSet<Long>();
			for ( Long resourceId : memberView.getAllRoots( false ) )
			{
				Resource tempResource = getResourceTopologyService().getResource( resourceId );
				if ( memberView.getAssembledSystemRoots().contains( resourceId ) )
				{
					userResourceIds.addAll( tempResource.getAllResourceAssociationIds() );
				}
				else
				{
					for ( Resource logicalResource : tempResource.createFilteredResourceList( new Class[] {LinkResource.class} ) )
					{
						Collections.addAll( userResourceIds, ( ( LinkResource ) logicalResource ).getLinkedResourceIds() );
					}
				}
			}

			for ( Resource resource : personalTreeResources )
			{
				LinkResource personalTreeItem = ( LinkResource ) resource;

				if ( personalTreeItem.getLinkedResourceIds().length > 0 )
				{
					for ( Long linkedId : personalTreeItem.getLinkedResourceIds() )
					{
						if ( !userResourceIds.contains( linkedId ) )
						{
							getResourceTopologyService().removeResource( resource.getId() );
						}
					}
				}
			}
		}
		catch ( TopologyException e )
		{
			LOG.warn( "Could not update personal tree resources for user {} Cause {}", new Object[] {memberView.getName(), e} );
		}
	}

	public void certRemoved( String fileStorageId )
	{
		List<MemberView> memberList = listAllMembers();
		for ( MemberView aMemberView : memberList )
		{
			if ( ( aMemberView.getDetailsView() != null ) && ( aMemberView.getDetailsView().getCertificateId() != null ) && ( aMemberView.getDetailsView().getCertificateId().equals( fileStorageId ) ) )
			{
				MemberEntity memberModel = memberDAO.findMemberByName( aMemberView.getName() );

				memberModel.getUserDetails().setCardLogout( false );
				memberModel.getUserDetails().setCertificate( null );

				UserLogoffEvent userLogoff = new UserLogoffEvent( memberModel.getName() );
				eventRegistry.sendEventAfterTransactionCommits( userLogoff );

				UserUpdatedEvent userUpdated = new UserUpdatedEvent( memberModel.getName() );
				eventRegistry.sendEventAfterTransactionCommits( userUpdated );
			}
		}
	}

	public void validateCertificates( String[] anArray )
	{
		MemberView aMember = getMember( getActiveUser() );
		if ( !aMember.getDetailsView().getCardLogout() )
		{
			return;
		}
		try
		{
			FileStorageView fsCert = getFileService().getFileStorage( aMember.getDetailsView().getCertificateId() );
			String base64EncodedCertId = fsCert.getProperty( "CERTIFICATE_ID" );
			for ( String userCertString : anArray )
			{
				if ( userCertString.equals( base64EncodedCertId ) )
				{
					return;
				}
			}
		}
		catch ( FileStorageException e )
		{
			LOG.warn( "Error loading certificate file: " + e.getMessage() );
		}

		UserLogoffEvent userLogoff = new UserLogoffEvent( aMember.getName() );
		eventRegistry.sendEventAfterTransactionCommits( userLogoff );
	}

	public Set<Long> allowedGroups( List<String> groups )
	{
		Set<Long> groupIds = new HashSet();

		if ( ( groups == null ) || ( groups.isEmpty() ) )
		{
			return groupIds;
		}

		List<MemberEntity> theGroups = memberDAO.findGroupByName( groups );
		for ( MemberEntity aMember : theGroups )
		{
			groupIds.add( aMember.getId() );
		}
		return groupIds;
	}

	public MemberView assembleRightsAndResources( MemberView memberView, boolean updateLoginTime ) throws UserException
	{
		MemberEntity member = memberDAO.findMemberByName( memberView.getName() );
		ProfileView profile = getProfile( memberView.getProfileId() );

		if ( ( member.getType() == MemberTypeEnum.LOCAL_USER ) || ( member.getType() == MemberTypeEnum.GROUP ) )
		{
			memberView.setAssembledRights( profile.getProfileRights() );
			memberView.setAssembledAppRights( profile.getProfileAppRights() );
			memberView.setAssembledAppData( profile.getAppProfileData() );
			memberView.setAssembledSystemRoots( memberView.getSystemRoots() );
			memberView.setAssembledLogicalRoots( memberView.getLogicalRoots() );
		}
		else
		{
			if ( ( memberView.getGroups().isEmpty() ) && ( memberView.getType().equals( MemberTypeEnum.GROUP_USER ) ) )
			{
				deleteMember( memberView );
				throw new UserException( "Group user " + memberView.getName() + " found in database, but no longer belongs to any group." );
			}

			memberView.clearAssembledData();

			if ( updateLoginTime )
			{
				memberView.setLastLoginTime( new Date() );
			}

			if ( memberView.getType() == MemberTypeEnum.LDAP_USER )
			{
				memberView.addAssembledRights( profile.getProfileRights() );
				memberView.addAssembledAppRights( profile.getProfileAppRights() );
				memberView.addAssembledAppData( profile.getAppProfileData() );
				memberView.addAssembledSystemRoots( memberView.getSystemRoots() );
				memberView.addAssembledLogicalRoots( memberView.getLogicalRoots() );
			}

			for ( Long groupMemberId : memberView.getGroups() )
			{
				MemberEntity group = ( MemberEntity ) memberDAO.findById( groupMemberId );

				if ( group == null )
				{
					throw new UserException( "Could not load user group with the name: " + groupMemberId );
				}

				ProfileView groupProfile = getProfile( group.getProfileId() );

				memberView.addAssembledRights( groupProfile.getProfileRights() );
				memberView.addAssembledAppRights( groupProfile.getProfileAppRights() );
				memberView.addAppProfileData( groupProfile.getAppProfileData() );

				Set<Long> mergedSystemRoots = mergeRootResources( memberView.getAssembledSystemRoots(), group.getAssembledSystemRoots() );
				Set<Long> mergedLogicalRoots = mergeRootResources( memberView.getAssembledLogicalRoots(), group.getAssembledLogicalRoots() );

				memberView.setAssembledSystemRoots( mergedSystemRoots );
				memberView.setAssembledLogicalRoots( mergedLogicalRoots );
			}
		}

		modifyMemberForApps( memberView );

		member.readFromDataObject( memberView );

		return memberView;
	}

	public void updateMemberDetailsView( MemberView aView )
	{
		if ( ( aView == null ) || ( aView.getName() == null ) || ( aView.getDetailsView() == null ) )
		{
			return;
		}
		MemberEntity memberModel = memberDAO.findMemberByName( aView.getName() );

		if ( memberModel != null )
		{
			aView.getDetailsView().setAdmin( memberModel.getUserDetails().isSuperAdmin() );
			aView.getDetailsView().setActive( memberModel.getUserDetails().isActive() );
			memberModel.getUserDetails().readFromDataObject( aView.getDetailsView() );
			aView.setGroups( memberModel.getGroups() );
		}
	}

	public List<String> findMembersNames( String... usernames )
	{
		if ( usernames == null )
		{
			return null;
		}

		for ( int i = 0; i < usernames.length; i++ )
		{
			usernames[i] = CommonUtils.getShortUsername( usernames[i] );
		}
		return memberDAO.findMembersNames( usernames );
	}

	private void auditMember( MemberView affectedMember, AuditEventNameEnum auditEventName, boolean passwordChanged )
	{
		if ( CommonAppUtils.getUsernameFromSecurityContext() != null )
		{
			AuditView.Builder auditBuilder = new AuditView.Builder( auditEventName.getName() ).addDetailsPair( "target_user", affectedMember.getName() );

			if ( !auditEventName.equals( AuditEventNameEnum.USER_REMOVED ) )
			{
				if ( affectedMember.getProfileId() != null )
				{
					ProfileView profile = getProfile( affectedMember.getProfileId() );
					auditBuilder.addDetailsPair( "profile", profile.getName() );
				}

				StringBuilder tempStringBuilder = new StringBuilder();
				List<Long> territoryResources = null;
				for ( int c = 0; c < 2; c++ )
				{
					if ( c == 0 )
					{
						territoryResources = new ArrayList( affectedMember.getAssembledSystemRoots() );
					}
					else
					{
						territoryResources = new ArrayList( affectedMember.getAssembledLogicalRoots() );
					}
					for ( int i = 0; i < territoryResources.size(); )
					{
						Long resourceId = ( Long ) territoryResources.get( i );
						tempStringBuilder.append( AuditLogDetailsHelper.findResourcePath( resourceId ) );
						i++;
						if ( i != territoryResources.size() )
						{
							tempStringBuilder.append( "," );
						}
					}
					if ( c == 0 )
					{
						auditBuilder.addDetailsPair( "system_territory", tempStringBuilder.toString() );
					}
					else
					{
						auditBuilder.addDetailsPair( "logical_territory", tempStringBuilder.toString() );
					}
					tempStringBuilder = new StringBuilder();
				}
				if ( passwordChanged )
				{
					auditBuilder.addDetailsPair( "password_modified", "yes" );
				}
				UserDetailsView details = affectedMember.getDetailsView();
				if ( details != null )
				{
					if ( details.isActive() )
					{
						auditBuilder.addDetailsPair( "status", "enabled" );
					}
					else
					{
						auditBuilder.addDetailsPair( "status", "disabled" );
					}
					if ( details.getCertificateId() != null )
					{
						auditBuilder.addDetailsPair( "certificate", "enabled" );
						if ( details.getCardLogout() )
						{
							auditBuilder.addDetailsPair( "certificate_logout", "yes" );
						}
					}
				}
			}
			eventRegistry.sendEventAfterTransactionCommits( new AuditEvent( auditBuilder.build() ) );
		}
	}

	protected void auditMember( MemberView affectedMember, AuditEventNameEnum auditEventName )
	{
		auditMember( affectedMember, auditEventName, false );
	}

	protected void auditProfile( ProfileView profile, AuditEventNameEnum auditEventName )
	{
		if ( CommonAppUtils.getUsernameFromSecurityContext() != null )
		{
			AuditView.Builder auditBuilder = new AuditView.Builder( auditEventName.getName() ).addDetailsPair( "profile", profile.getName() );
			if ( !auditEventName.equals( AuditEventNameEnum.PROFILE_REMOVED ) )
			{
				int i = 0;
				StringBuilder sb = new StringBuilder();
				for ( RightEnum profileRight : profile.getProfileRights() )
				{
					sb.append( profileRight.name() );
					i++;
					if ( i != profile.getProfileRights().size() )
					{
						sb.append( "," );
					}
				}
				if ( profile.getProfileAppRights() != null )
				{
					List<String> apps = new ArrayList();
					for ( String appId : profile.getProfileAppRights() )
					{
						String appName = appManager.getAppName( appId );
						apps.add( appName );
					}
					sb.append( "," );
					sb.append( CollectionUtils.collectionToString( apps, "," ) );
				}
				auditBuilder.addDetailsPair( "user_rights", sb.toString() );
				sb = new StringBuilder();
				i = 0;
				Set<AppProfileData> profileData = profile.getAppProfileData();
				if ( profileData != null )
				{
					for ( AppProfileData data : profileData )
					{
						sb.append( CollectionUtils.collectionToString( data.getAppRights(), "," ) );
						if ( i != data.getAppRights().size() )
						{
							sb.append( "," );
						}
						i++;
					}
					auditBuilder.addDetailsPair( "user_app_rights", sb.toString() );
				}
			}
			eventRegistry.sendEventAfterTransactionCommits( new AuditEvent( auditBuilder.build() ) );
		}
	}

	public void processAppUninstalled( String guid )
	{
		List<ProfileEntity> profiles = profileDAO.findAll();

		List<Long> ids = new ArrayList();
		for ( ProfileEntity profile : profiles )
		{
			Set<String> rights = profile.getAppRights();
			if ( rights.contains( guid ) )
			{
				ids.add( profile.getId() );
			}
		}

		logoffUsersForProfiles( ids );

		for ( ProfileEntity profile : profiles )
		{
			profile.removeByGuid( guid );
		}

		appsRights.remove( guid );
	}

	public void processAppUpgraded( String guid )
	{
		List<ProfileEntity> profiles = profileDAO.findAllDetached();

		List<Long> ids = new ArrayList();
		for ( ProfileEntity profile : profiles )
		{
			Set<String> rights = profile.getAppRights();
			if ( rights.contains( guid ) )
			{
				ids.add( profile.getId() );
			}
		}

		logoffUsersForProfiles( ids );
	}

	public ProfileView getProfileForUser( String username ) throws UserException
	{
		MemberView member = getMember( username );

		if ( member == null )
		{
			throw new UserException( "User " + username + " not found." );
		}

		if ( member.getProfileId() == null )
		{
			LOG.warn( "Error querying profile for user " + username );
			return null;
		}

		ProfileView profile = getProfile( member.getProfileId() );

		return profile;
	}

	public List<UserRightExportTemplate> getUserRightTemplates()
	{
		List<UserRightExportTemplate> userRightsTemplates = new ArrayList();

		userRightsTemplates.add( new UserRightExportTemplate( RightEnum.MONITOR_SYSTEM_TREE.getProperty(), RightEnum.MONITOR_SYSTEM_TREE.name(), RightEnum.MANAGE_SYSTEM_TREE.name(), null ) );

		userRightsTemplates.add( new UserRightExportTemplate( RightEnum.MONITOR_LOGICAL_TREE.getProperty(), RightEnum.MONITOR_LOGICAL_TREE.name(), RightEnum.MANAGE_LOGICAL_TREE.name(), null ) );

		userRightsTemplates.add( new UserRightExportTemplate( RightEnum.PERSONAL_TREE.getProperty(), RightEnum.PERSONAL_TREE.name(), null ) );

		userRightsTemplates.add( new UserRightExportTemplate( RightEnum.LIVE_VIDEO.getProperty(), RightEnum.LIVE_VIDEO.name(), null ) );

		userRightsTemplates.add( new UserRightExportTemplate( RightEnum.ARCHIVE_VIDEO.getProperty(), RightEnum.ARCHIVE_VIDEO.name(), null ) );

		userRightsTemplates.add( new UserRightExportTemplate( RightEnum.EXPORT_NATIVE.getProperty(), RightEnum.EXPORT_NATIVE.name(), null ) );

		userRightsTemplates.add( new UserRightExportTemplate( RightEnum.EXPORT_MP4.getProperty(), RightEnum.EXPORT_MP4.name(), null ) );

		userRightsTemplates.add( new UserRightExportTemplate( RightEnum.PTZ_CONTROL.getProperty(), RightEnum.PTZ_CONTROL.name(), null ) );

		userRightsTemplates.add( new UserRightExportTemplate( RightEnum.HEALTH_MONITORING.getProperty(), RightEnum.HEALTH_MONITORING.name(), null ) );

		userRightsTemplates.add( new UserRightExportTemplate( RightEnum.MANAGE_ALARMS.getProperty(), RightEnum.MANAGE_ALARMS.name(), null ) );

		userRightsTemplates.add( new UserRightExportTemplate( RightEnum.MASS_CONFIGURATION.getProperty(), RightEnum.MASS_CONFIGURATION.name(), null ) );

		userRightsTemplates.add( new UserRightExportTemplate( RightEnum.MANAGE_USERS.getProperty(), RightEnum.MANAGE_USERS.name(), null ) );

		userRightsTemplates.add( new UserRightExportTemplate( RightEnum.MANAGE_DEVICES.getProperty(), RightEnum.MANAGE_DEVICES.name(), null ) );

		userRightsTemplates.add( new UserRightExportTemplate( RightEnum.ACCESS_LOGS.getProperty(), RightEnum.ACCESS_LOGS.name(), null ) );

		userRightsTemplates.add( new UserRightExportTemplate( RightEnum.MANAGE_APPS.getProperty(), RightEnum.MANAGE_APPS.name(), null ) );

		userRightsTemplates.add( new UserRightExportTemplate( RightEnum.EXPORT_LOCAL.getProperty(), RightEnum.EXPORT_LOCAL.name(), null ) );

		userRightsTemplates.add( new UserRightExportTemplate( RightEnum.MANAGE_CASE_MANAGEMENT.getProperty(), RightEnum.MANAGE_CASE_MANAGEMENT.name(), null ) );

		userRightsTemplates.add( new UserRightExportTemplate( RightEnum.PRIVACY_UNMASK.getProperty(), RightEnum.PRIVACY_UNMASK.name(), null ) );

		userRightsTemplates.add( new UserRightExportTemplate( "Layout", "Simplified", "Simplified", "Enhanced", null ) );

		return userRightsTemplates;
	}

	public byte[] exportUserRights() throws UserException, TopologyException
	{
		List<MemberView> visibleMembers = listNotDeletedMembers( false );
		List<UserRightExportTemplate> exportTemplates = getUserRightsExportTemplates();

		List<String> headers = new ArrayList();
		headers.add( "User" );
		headers.add( "User Type" );
		headers.add( "System Tree Resources" );
		headers.add( "Logical Tree Resources" );

		for ( UserRightExportTemplate tmpl : exportTemplates )
		{
			headers.add( tmpl.getHeader() );
		}

		Map<String, List<String>> profilesRightsMap = getAllProfilesRightsMap( exportTemplates );
		List<List<String>> userDataList = new ArrayList();

		ResourceType[] filter = {ResourceType.GROUP};
		List<Resource> resourcesList = getResourceTopologyService().getResourcesForUser( ResourceRootType.SYSTEM_LOGICAL, filter );
		Set<Long> accessibleFolders = new HashSet( resourcesList.size() );
		for ( Resource resource : resourcesList )
		{
			accessibleFolders.add( resource.getId() );
		}

		for ( MemberView member : visibleMembers )
		{
			List<String> userData = new ArrayList();
			userData.add( member.getName() );
			userData.add( member.getType().getValue() );

			List<String> resources = new ArrayList();

			for ( Long resourceId : member.getSystemRoots() )
			{
				resources.add( getFullFolderPathString( resourceId, accessibleFolders ) );
			}

			userData.add( CollectionUtils.collectionToString( resources, "," ) );

			resources = new ArrayList();

			for ( Long resourceId : member.getLogicalRoots() )
			{
				resources.add( getFullFolderPathString( resourceId, accessibleFolders ) );
			}

			userData.add( CollectionUtils.collectionToString( resources, "," ) );

			userData.addAll( ( Collection ) profilesRightsMap.get( member.getProfileId().toString() ) );

			userDataList.add( userData );
		}

		byte[] userRightsXls = null;
		try
		{
			userRightsXls = exporterService.exportData( headers, userDataList );
		}
		catch ( ExporterException e )
		{
			throw new UserException( e.getMessage() );
		}

		return userRightsXls;
	}

	private String getFullFolderPathString( Long folderId, Set<Long> allowedFolders )
	{
		List<ResourcePathNode> nodes = getResourceTopologyService().getResourcePath( folderId );
		List<String> paths = new LinkedList();

		for ( ResourcePathNode node : nodes )
		{
			if ( allowedFolders.contains( node.getId() ) )
			{
				paths.add( node.getName() );
			}
		}

		Collections.reverse( paths );

		String resourcePath = CollectionUtils.collectionToString( paths, "/" );

		return resourcePath;
	}

	private Map<String, List<String>> getAllProfilesRightsMap( List<UserRightExportTemplate> exportTemplates )
	{
		List<ProfileView> allProfiles = listAllProfiles();
		Map<String, List<String>> profilesRightsMap = new HashMap();

		for ( ProfileView profileView : allProfiles )
		{
			List<String> profileGeneralRightsList = new ArrayList();

			for ( RightEnum rightEnum : profileView.getProfileRights() )
			{
				profileGeneralRightsList.add( rightEnum.name() );
			}

			profileGeneralRightsList.add( profileView.isSimplifiedUI() ? "Simplified" : "Enhanced" );

			Map<String, List<String>> profileAppsRightsMap = new HashMap();
			Set<String> profileAppRights = profileView.getProfileAppRights();

			for ( String appId : profileAppRights )
			{
				Set<String> appRights = profileView.getAppRights( appId );
				List<String> appRightsList = new ArrayList();
				appRightsList.add( appId );

				if ( appRights != null )
				{
					for ( String right : appRights )
					{
						appRightsList.add( right );
					}
				}

				profileAppsRightsMap.put( appId, appRightsList );
			}

			List<String> exportList = new ArrayList();

			for ( UserRightExportTemplate rightTemplate : exportTemplates )
			{
				List<String> currentRightsList = ( rightTemplate.getAppId() == null ) || ( profileAppsRightsMap.get( rightTemplate.getAppId() ) == null ) ? profileGeneralRightsList : ( List ) profileAppsRightsMap.get( rightTemplate.getAppId() );
				String displayString = "";

				switch ( rightTemplate.getDisplayType() )
				{
					case YES_NO:
						displayString = currentRightsList.contains( rightTemplate.getTargetRight() ) ? "Yes" : "No";
						break;
					case VIEW_EDIT:
						if ( currentRightsList.contains( rightTemplate.getEditTargetRight() ) )
						{
							displayString = "Edit";
						}
						else if ( currentRightsList.contains( rightTemplate.getViewTargetRight() ) )
						{
							displayString = "View";
						}
						else
						{
							displayString = "Hidden";
						}
						break;
					case TWO_STATE:
						displayString = currentRightsList.contains( rightTemplate.getTargetRight() ) ? rightTemplate.getRightAssignedValue() : rightTemplate.getRightNotAssignedValue();
				}

				exportList.add( displayString );
			}

			profilesRightsMap.put( profileView.getProfileId(), exportList );
		}

		return profilesRightsMap;
	}

	private List<UserRightExportTemplate> getUserRightsExportTemplates()
	{
		List<UserRightTemplateProvider> userRightsProviders = new ArrayList();
		userRightsProviders.add( this );

		List<UserRightTemplateProvider> templateProviders = getOsgiService().getServices( UserRightTemplateProvider.class );
		if ( templateProviders != null )
		{
			userRightsProviders.addAll( templateProviders );
		}

		List<UserRightExportTemplate> exportTemplates = new ArrayList();

		for ( UserRightTemplateProvider provider : userRightsProviders )
		{
			List<UserRightExportTemplate> providerTemplates = provider.getUserRightTemplates();
			if ( providerTemplates != null )
			{
				exportTemplates.addAll( providerTemplates );
			}
		}
		return exportTemplates;
	}

	public void acceptTerms() throws UserException
	{
		String userName = CommonAppUtils.getUsernameFromSecurityContext();
		MemberEntity member = memberDAO.findMemberByName( CommonUtils.getShortUsername( userName ) );

		if ( member == null )
		{
			throw new UserException( "Could not find member: " + userName );
		}

		member.setTermsAccepted( Boolean.valueOf( true ) );
	}

	public boolean hasRight( RightEnum right )
	{
		String userName = CommonAppUtils.getUsernameFromSecurityContext();
		if ( userName != null )
		{
			try
			{
				MemberView member = getUser( userName );
				return member.hasRight( right );
			}
			catch ( UserException e )
			{
				LOG.debug( "User " + userName + " not found. Error details:" + e.getMessage() );
				return false;
			}
		}
		return true;
	}

	public Map<String, Set<String>> getAllAppRights()
	{
		return appsRights;
	}

	public List<String> getGroups( MemberView member )
	{
		List<String> groupNames = new ArrayList();

		for ( Long groupId : member.getGroups() )
		{
			MemberEntity group = ( MemberEntity ) memberDAO.findByIdDetached( groupId );
			groupNames.add( group.getName() );
		}

		return groupNames;
	}

	public byte[] getHash( MemberView member, String password )
	{
		if ( member.getSalt() == null )
		{
			byte[] salt = CryptoUtils.generateSalt();
			member.setSalt( salt );
		}

		if ( password == null )
		{
			password = "";
		}
		String function = commonConfiguration.getProperty( ConfigProperty.CRYPTOGRAPHIC_HASH_FUNCTION );
		String realm = commonConfiguration.getProperty( ConfigProperty.REALM );

		return CryptoUtils.getHash( member, password, function, realm );
	}

	public boolean authenticateUser( MemberView foundMember, String password )
	{
		Date currentTime = new Date();
		int LOGIN_TIMEOUT = getLoginTimeoutFromConfig();
		if ( ( !foundMember.getType().equals( MemberTypeEnum.LOCAL_USER ) ) && ( foundMember.getLastLoginTime() == null ) )
			return false;
		if ( ( !foundMember.getType().equals( MemberTypeEnum.LOCAL_USER ) ) && ( currentTime.getTime() - foundMember.getLastLoginTime().getTime() > LOGIN_TIMEOUT ) )
		{
			return false;
		}
		if ( password == null )
		{
			password = "";
		}
		byte[] hash = getHash( foundMember, password );

		return Arrays.equals( hash, foundMember.getHash() );
	}

	public boolean checkDeletions( List<Long> resourceIds )
	{
		String userName = CommonAppUtils.getUsernameFromSecurityContext();
		if ( userName != null )
		{
			try
			{
				MemberView memberView = getUser( userName );
				Set<Long> userRoots = memberView.getAllRoots( false );
				for ( Long resourceId : resourceIds )
				{
					if ( userRoots.contains( resourceId ) )
						return false;
				}
			}
			catch ( UserException e )
			{
				LOG.error( "Error when looking up user {}. Details: {}", userName, e );
			}
		}
		return true;
	}

	private int getLoginTimeoutFromConfig()
	{
		if ( commonConfiguration.getProperty( ConfigProperty.LOGIN_TIMEOUT ) != null )
		{
			return Integer.valueOf( commonConfiguration.getProperty( ConfigProperty.LOGIN_TIMEOUT ) ).intValue() * 3600000;
		}
		return 86400000;
	}

	private void modifyMemberForApps( MemberView member )
	{
		OsgiService osgiService = ( OsgiService ) ApplicationContextSupport.getBean( "osgiManager" );
		for ( AppAuthenticationService service : osgiService.getServices( AppAuthenticationService.class ) )
		{
			service.modifyMember( member );
		}
	}

	public void updateMemberPassword( MemberView memberView, String newPassword )
	{
		MemberEntity member = memberDAO.findMemberByName( memberView.getName() );
		byte[] hash = getHash( memberView, newPassword );
		byte[] salt = memberView.getSalt();
		memberView.setHash( hash );
		member.setHash( hash );
		member.setSalt( salt );
	}

	private Set<Long> mergeRootResources( Set<Long> originalResources, Set<Long> resourcesToMerge )
	{
		Set<Long> originalResourcesCopy = new HashSet( originalResources );
		Set<Long> distinctResources = new HashSet( resourcesToMerge );

		if ( originalResources.isEmpty() )
		{
			return distinctResources;
		}
		if ( resourcesToMerge.isEmpty() )
		{
			return originalResourcesCopy;
		}

		distinctResources.removeAll( originalResourcesCopy );

		for ( Iterator i$ = distinctResources.iterator(); i$.hasNext(); )
		{
			Long mergingResource = ( Long ) i$.next();
			for ( Long memberSystemRoot : originalResources )
			{
				if ( getResourceTopologyService().isChild( memberSystemRoot, mergingResource ) )
				{
					break;
				}

				if ( getResourceTopologyService().isChild( mergingResource, memberSystemRoot ) )
				{
					originalResourcesCopy.remove( memberSystemRoot );
					originalResourcesCopy.add( mergingResource );
				}
				else
				{
					originalResourcesCopy.add( mergingResource );
				}
			}
		}

		return originalResourcesCopy;
	}

	private Set<Long> removeRedundantResources( Set<Long> rootResources )
	{
		if ( ( rootResources == null ) || ( rootResources.isEmpty() ) )
		{
			return rootResources;
		}

		Set<Long> newRoots = new HashSet( rootResources );

		for ( Iterator i$ = rootResources.iterator(); i$.hasNext(); )
		{
			Long rootResource = ( Long ) i$.next();

			if ( newRoots.contains( rootResource ) )
			{
				for ( Iterator<Long> iterator = newRoots.iterator(); iterator.hasNext(); )
				{
					Long resource = ( Long ) iterator.next();
					if ( ( resource != rootResource ) && ( getResourceTopologyService().isChild( rootResource, resource ) ) )
						iterator.remove();
				}
			}
		}

		return newRoots;
	}

	private void checkForTopLevelAccess() throws UserException
	{
		String username = CommonAppUtils.getUsernameFromSecurityContext();
		MemberView member = null;
		try
		{
			member = getUser( username );
		}
		catch ( UserException e )
		{
			throw new UserException( "Could not find user: " + username );
		}

		if ( !member.isTopLevelUser() )
		{
			throw new UserException( "User: " + username + " does not have access privileges for this operation.", UserExceptionTypeEnum.INSUFFICIENT_ACCESS_RIGHTS );
		}
	}

	public void setMemberDAO( MemberDAO memberDAO )
	{
		this.memberDAO = memberDAO;
	}

	public void setEventRegistry( EventRegistry eventRegistry )
	{
		this.eventRegistry = eventRegistry;
	}

	public void setProfileDAO( ProfileDAO profileDAO )
	{
		this.profileDAO = profileDAO;
	}

	public FileStorageService getFileService()
	{
		if ( fileService == null )
		{
			fileService = ( ( FileStorageService ) ApplicationContextSupport.getBean( "fileStorageService" ) );
		}
		return fileService;
	}

	public ResourceTopologyServiceIF getResourceTopologyService()
	{
		if ( resourceTopologyService == null )
		{
			resourceTopologyService = ( ( ResourceTopologyServiceIF ) ApplicationContextSupport.getBean( "resourceTopologyService_internal" ) );
		}
		return resourceTopologyService;
	}

	public void setGenericStorageService( GenericStorageCoreService genericStorageService )
	{
		this.genericStorageService = genericStorageService;
	}

	public void setLdapService( LDAPService ldapService )
	{
		this.ldapService = ldapService;
	}

	public void setExporterService( ExporterCoreService exporterService )
	{
		this.exporterService = exporterService;
	}

	public void setAppManager( AppManager appManager )
	{
		this.appManager = appManager;
	}

	public void setCommonConfiguration( CommonConfiguration commonConfiguration )
	{
		this.commonConfiguration = commonConfiguration;
	}

	public OsgiService getOsgiService()
	{
		if ( osgiService == null )
		{
			osgiService = ( ( OsgiService ) ApplicationContextSupport.getBean( "osgiManager" ) );
		}
		return osgiService;
	}
}

