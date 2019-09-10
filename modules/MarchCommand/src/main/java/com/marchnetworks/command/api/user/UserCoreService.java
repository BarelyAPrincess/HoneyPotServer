package com.marchnetworks.command.api.user;

import com.marchnetworks.command.common.user.UserException;
import com.marchnetworks.command.common.user.data.MemberView;
import com.marchnetworks.command.common.user.data.ProfileView;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface UserCoreService
{
	boolean hasUserAccess( String paramString ) throws UserException;

	MemberView getMember( String paramString );

	MemberView getUser( String paramString ) throws UserException;

	boolean isSuperAdmin( String paramString );

	MemberView createMember( MemberView paramMemberView, String paramString ) throws UserException;

	List<MemberView> listAllMembers();

	ProfileView getProfile( Long paramLong );

	ProfileView createProfile( ProfileView paramProfileView ) throws UserException;

	List<ProfileView> listAllProfiles();

	void addAppRights( String paramString, Set<String> paramSet ) throws UserException;

	Map<String, Set<String>> getAllAppRights();

	List<String> getGroups( MemberView paramMemberView );

	void addNewAppRights( String paramString, Set<String> paramSet ) throws UserException;

	ProfileView getProfileForUser( String paramString ) throws UserException;
}
