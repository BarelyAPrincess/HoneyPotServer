package com.marchnetworks.management.user;

import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.user.UserException;
import com.marchnetworks.command.common.user.data.ProfileView;
import com.marchnetworks.command.common.user.data.RightEnum;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.esm.util.DeprecationUtils;
import com.marchnetworks.management.app.AppRights;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import org.springframework.security.access.AccessDeniedException;

@WebService( serviceName = "UserService", name = "UserService", portName = "UserPort" )
public class UserWebService
{
	private static final Logger LOG = Logger.getLogger( UserWebService.class.getName() );

	private UserService service = ( UserService ) ApplicationContextSupport.getBean( "userServiceProxy" );
	private String accessDenied = "not_authorized";

	@Resource
	WebServiceContext wsContext;

	@WebMethod( operationName = "createMember" )
	public com.marchnetworks.command.common.user.data.deprecated.MemberView createMember( @WebParam( name = "member" ) com.marchnetworks.command.common.user.data.deprecated.MemberView member, @WebParam( name = "password" ) String aPassword ) throws UserException
	{
		LOG.fine( "Web Service create new user instance" );
		try
		{
			com.marchnetworks.command.common.user.data.MemberView newMember = DeprecationUtils.toNewMemberView( member );
			service.createMember( newMember, aPassword );
			return DeprecationUtils.toOldMemberView( newMember, false );
		}
		catch ( AccessDeniedException e )
		{
			throw new UserException( accessDenied );
		}
	}

	@WebMethod( operationName = "updateMember" )
	public com.marchnetworks.command.common.user.data.deprecated.MemberView updateMember( @WebParam( name = "member" ) com.marchnetworks.command.common.user.data.deprecated.MemberView member, @WebParam( name = "password" ) String aPassword ) throws UserException
	{
		LOG.fine( "Web Service update user" );
		try
		{
			com.marchnetworks.command.common.user.data.MemberView updatedMember = DeprecationUtils.toNewMemberView( member );
			service.updateMember( updatedMember, aPassword );
			return DeprecationUtils.toOldMemberView( updatedMember, false );
		}
		catch ( AccessDeniedException e )
		{
			throw new UserException( accessDenied );
		}
	}

	@WebMethod( operationName = "deleteMember" )
	public com.marchnetworks.command.common.user.data.deprecated.MemberView deleteMember( @WebParam( name = "member" ) com.marchnetworks.command.common.user.data.deprecated.MemberView member ) throws UserException
	{
		LOG.fine( "Web Service delete user" );
		try
		{
			com.marchnetworks.command.common.user.data.MemberView memberToDelete = DeprecationUtils.toNewMemberView( member );
			service.deleteMember( memberToDelete );
			return DeprecationUtils.toOldMemberView( memberToDelete, false );
		}
		catch ( AccessDeniedException e )
		{
			throw new UserException( accessDenied );
		}
	}

	@WebMethod( operationName = "getMember" )
	public com.marchnetworks.command.common.user.data.deprecated.MemberView getMember( @WebParam( name = "memberName" ) String memberName ) throws UserException
	{
		try
		{
			return DeprecationUtils.toOldMemberView( service.getMember( memberName ), false );
		}
		catch ( AccessDeniedException e )
		{
			throw new UserException( accessDenied );
		}
	}

	@WebMethod( operationName = "getUser" )
	public com.marchnetworks.command.common.user.data.deprecated.MemberView getUser( String aName ) throws UserException
	{
		String userName = aName;
		if ( aName == null )
		{
			userName = CommonAppUtils.getUsernameFromSecurityContext();
		}
		return DeprecationUtils.toOldMemberView( service.getUser( userName ), true );
	}

	@WebMethod( operationName = "searchLdap" )
	public List<com.marchnetworks.command.common.user.data.deprecated.MemberView> searchLdap( @WebParam( name = "userName" ) String userName, @WebParam( name = "maxResults" ) int maxResults ) throws UserException
	{
		try
		{
			List<com.marchnetworks.command.common.user.data.MemberView> members = service.searchLdap( userName, maxResults );
			return DeprecationUtils.convertNewMemberViewList( members );
		}
		catch ( AccessDeniedException e )
		{
			throw new UserException( accessDenied );
		}
	}

	@WebMethod( operationName = "listAllMembers" )
	public List<com.marchnetworks.command.common.user.data.deprecated.MemberView> listAllMembers() throws UserException
	{
		LOG.fine( "Web Service list all users" );
		try
		{
			List<com.marchnetworks.command.common.user.data.MemberView> members = service.listAllMembers();
			return DeprecationUtils.convertNewMemberViewList( members );
		}
		catch ( AccessDeniedException e )
		{
			throw new UserException( accessDenied );
		}
	}

	@WebMethod( operationName = "listNotDeletedMembers" )
	public List<com.marchnetworks.command.common.user.data.deprecated.MemberView> listNotDeletedMembers() throws UserException
	{
		LOG.fine( "Web Service list not deleted members" );
		try
		{
			List<com.marchnetworks.command.common.user.data.MemberView> members = service.listNotDeletedMembers( false );
			return DeprecationUtils.convertNewMemberViewList( members );
		}
		catch ( AccessDeniedException e )
		{
			throw new UserException( accessDenied );
		}
	}

	@WebMethod( operationName = "getAllMembers" )
	public List<com.marchnetworks.command.common.user.data.deprecated.MemberView> getAllMembers() throws UserException
	{
		LOG.fine( "Listing all members including Group Users" );
		try
		{
			List<com.marchnetworks.command.common.user.data.MemberView> newMembers = service.listNotDeletedMembers( true );
			return DeprecationUtils.convertNewMemberViewList( newMembers );
		}
		catch ( AccessDeniedException e )
		{
			throw new UserException( accessDenied );
		}
	}

	@WebMethod( operationName = "createProfile" )
	public ProfileView createProfile( @WebParam( name = "profile" ) ProfileView profile ) throws UserException
	{
		try
		{
			return service.createProfile( profile );
		}
		catch ( AccessDeniedException e )
		{
			throw new UserException( accessDenied );
		}
	}

	@WebMethod( operationName = "getProfile" )
	public ProfileView getProfile( @WebParam( name = "profileName" ) String profileName ) throws UserException
	{
		try
		{
			return service.getProfile( Long.valueOf( profileName ) );
		}
		catch ( AccessDeniedException e )
		{
			throw new UserException( accessDenied );
		}
	}

	@WebMethod( operationName = "modifyProfile" )
	public ProfileView modifyProfile( @WebParam( name = "profile" ) ProfileView profile ) throws UserException
	{
		try
		{
			return service.updateProfile( profile, true );
		}
		catch ( AccessDeniedException e )
		{
			throw new UserException( accessDenied );
		}
	}

	@WebMethod( operationName = "deleteProfile" )
	public ProfileView deleteProfile( @WebParam( name = "profile" ) ProfileView profile ) throws UserException
	{
		try
		{
			return service.deleteProfile( profile );
		}
		catch ( AccessDeniedException e )
		{
			throw new UserException( accessDenied );
		}
	}

	@WebMethod( operationName = "getAllProfileRights" )
	public RightEnum[] getAllProfileRightsEnum() throws UserException
	{
		try
		{
			return service.getAllProfileRightsEnum();
		}
		catch ( AccessDeniedException e )
		{
			throw new UserException( accessDenied );
		}
	}

	@WebMethod( operationName = "listAllProfiles" )
	public List<ProfileView> listAllProfiles() throws UserException
	{
		try
		{
			return service.listAllProfiles();
		}
		catch ( AccessDeniedException e )
		{
			throw new UserException( accessDenied );
		}
	}

	@WebMethod( operationName = "validateCertificates" )
	public void validateCertificates( @WebParam( name = "certIds" ) String[] anArray ) throws UserException
	{
		try
		{
			service.validateCertificates( anArray );
		}
		catch ( AccessDeniedException e )
		{
			throw new UserException( accessDenied );
		}
	}

	@WebMethod( operationName = "getAllAppRights" )
	public List<AppRights> getAllAppRights()
	{
		List<AppRights> appRights = new ArrayList();
		for ( Entry<String, Set<String>> entry : service.getAllAppRights().entrySet() )
		{
			appRights.add( new AppRights( ( String ) entry.getKey(), ( Set ) entry.getValue() ) );
		}
		return appRights;
	}

	@WebMethod( operationName = "acceptTerms" )
	public void acceptTerms() throws UserException
	{
		try
		{
			service.acceptTerms();
		}
		catch ( AccessDeniedException e )
		{
			throw new UserException( accessDenied );
		}
	}
}
