package com.marchnetworks.esm.util;

import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.user.data.MemberTypeEnum;
import com.marchnetworks.command.common.user.data.ProfileView;
import com.marchnetworks.command.common.user.data.RightEnum;
import com.marchnetworks.command.common.user.data.deprecated.MemberResourceTypeEnum;
import com.marchnetworks.command.common.user.data.deprecated.MemberResourceView;
import com.marchnetworks.command.common.user.data.deprecated.MemberView;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.utils.CommonUtils;
import com.marchnetworks.management.user.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DeprecationUtils
{
	private static final UserService userService = ( UserService ) ApplicationContextSupport.getBean( "userServiceProxy_internal" );

	public static com.marchnetworks.command.common.user.data.deprecated.MemberView toOldMemberView( com.marchnetworks.command.common.user.data.MemberView newMemberView, boolean assemble )
	{
		if ( newMemberView == null )
		{
			return null;
		}

		com.marchnetworks.command.common.user.data.deprecated.MemberView oldMemberView = new com.marchnetworks.command.common.user.data.deprecated.MemberView();

		oldMemberView.setName( newMemberView.getName() );
		oldMemberView.setDetailsView( newMemberView.getDetailsView() );
		oldMemberView.setTermsAccepted( newMemberView.getTermsAccepted() );
		oldMemberView.setType( newMemberView.getType() );

		if ( newMemberView.getProfileId() != null )
		{
			ProfileView profile = userService.getProfile( newMemberView.getProfileId() );
			oldMemberView.setProfile( profile );
		}

		List<MemberResourceView> resources = new ArrayList();

		if ( ( newMemberView.getType() == MemberTypeEnum.LOCAL_USER ) || ( newMemberView.getType() == MemberTypeEnum.GROUP ) )
		{
			newMemberView.addAssembledSystemRoots( newMemberView.getSystemRoots() );
			newMemberView.addAssembledLogicalRoots( newMemberView.getLogicalRoots() );
		}

		if ( ( !assemble ) && ( newMemberView.getType() == MemberTypeEnum.LDAP_USER ) )
		{
			newMemberView.clearAssembledData();
			newMemberView.addAssembledSystemRoots( newMemberView.getSystemRoots() );
			newMemberView.addAssembledLogicalRoots( newMemberView.getLogicalRoots() );
		}

		for ( Long systemId : newMemberView.getAssembledSystemRoots() )
		{
			resources.add( new MemberResourceView( MemberResourceTypeEnum.SYSTEM, systemId ) );
		}

		for ( Long logicalId : newMemberView.getAssembledLogicalRoots() )
		{
			resources.add( new MemberResourceView( MemberResourceTypeEnum.LOGICAL, logicalId ) );
		}

		if ( newMemberView.getPersonalRoot() != null )
		{
			resources.add( new MemberResourceView( MemberResourceTypeEnum.PERSONAL, newMemberView.getPersonalRoot() ) );
		}

		if ( ( ( newMemberView.getType() == MemberTypeEnum.GROUP_USER ) || ( newMemberView.getType() == MemberTypeEnum.LDAP_USER ) ) && ( assemble ) )
		{
			if ( newMemberView.getProfileId() == null )
			{
				oldMemberView.setProfile( new ProfileView() );
			}

			oldMemberView.getProfile().getProfileRights().addAll( newMemberView.getAssembledRights() );
			oldMemberView.getProfile().getProfileAppRights().addAll( newMemberView.getAssembledAppRights() );
			oldMemberView.getProfile().getAppProfileData().addAll( newMemberView.getAssembledAppData() );
		}

		oldMemberView.setResources( resources );

		String clientVersion = ( String ) CommonAppUtils.getDetailParameter( "x-client-version" );
		if ( ( clientVersion != null ) && ( CommonUtils.compareVersions( clientVersion, "2.3" ) == -1 ) )
		{
			ProfileView profile = oldMemberView.getProfile();
			if ( profile != null )
			{
				Set<RightEnum> rights = profile.getProfileRights();
				rights.remove( RightEnum.PRIVACY_UNMASK );
			}
		}

		return oldMemberView;
	}

	public static com.marchnetworks.command.common.user.data.MemberView toNewMemberView( com.marchnetworks.command.common.user.data.deprecated.MemberView oldView )
	{
		if ( oldView == null )
		{
			return null;
		}

		com.marchnetworks.command.common.user.data.MemberView newMemberView = new com.marchnetworks.command.common.user.data.MemberView();

		newMemberView.setName( oldView.getName() );
		ProfileView oldProfile = oldView.getProfile();
		if ( oldProfile != null )
		{
			newMemberView.setProfileId( Long.valueOf( oldProfile.getProfileId() ) );
		}
		newMemberView.setDetailsView( oldView.getDetailsView() );
		newMemberView.setTermsAccepted( oldView.getTermsAccepted() );
		newMemberView.setType( oldView.getType() );

		for ( MemberResourceView view : oldView.getResources() )
		{
			if ( view.getResourceType() == MemberResourceTypeEnum.SYSTEM )
			{
				newMemberView.addSystemRoot( view.getResourceId() );
			}
			else if ( view.getResourceType() == MemberResourceTypeEnum.LOGICAL )
			{
				newMemberView.addLogicalRoot( view.getResourceId() );
			}
			else
			{
				newMemberView.setPersonalRoot( view.getResourceId() );
			}
		}
		return newMemberView;
	}

	public static List<MemberView> convertNewMemberViewList( List<com.marchnetworks.command.common.user.data.MemberView> newList )
	{
		if ( newList == null )
		{
			return null;
		}

		List<MemberView> oldList = new ArrayList( newList.size() );

		for ( com.marchnetworks.command.common.user.data.MemberView memberView : newList )
		{
			oldList.add( toOldMemberView( memberView, false ) );
		}

		return oldList;
	}

	public static List<com.marchnetworks.command.common.user.data.MemberView> convertOldMemberViewList( List<MemberView> oldList )
	{
		if ( oldList == null )
		{
			return null;
		}

		List<com.marchnetworks.command.common.user.data.MemberView> newList = new ArrayList( oldList.size() );

		for ( com.marchnetworks.command.common.user.data.deprecated.MemberView memberView : oldList )
		{
			newList.add( toNewMemberView( memberView ) );
		}

		return newList;
	}
}
