package com.marchnetworks.management.user;

import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.user.UserException;
import com.marchnetworks.command.common.user.data.MemberView;
import com.marchnetworks.command.common.user.data.ProfileView;
import com.marchnetworks.command.common.user.data.RightEnum;

import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract interface UserService
{
	public abstract MemberView createMember( MemberView paramMemberView, String paramString ) throws UserException;

	public abstract MemberView updateMember( MemberView paramMemberView, String paramString ) throws UserException;

	public abstract MemberView deleteMember( MemberView paramMemberView ) throws UserException;

	public abstract MemberView getMember( String paramString );

	public abstract MemberView getUser( String paramString ) throws UserException;

	public abstract List<MemberView> searchLdap( String paramString, int paramInt ) throws UserException;

	public abstract List<MemberView> listAllMembers();

	public abstract void removeTerritoryResourceFromMembers( Long paramLong );

	public abstract void verifyMembersPersonalResources();

	public abstract void verifyMemberPersonalResources( String paramString );

	public abstract boolean isSuperAdmin( String paramString );

	public abstract boolean hasUserAccess( String paramString ) throws UserException;

	public abstract String[] listUserAccess() throws UserException;

	public abstract ProfileView createProfile( ProfileView paramProfileView ) throws UserException;

	public abstract ProfileView deleteProfile( ProfileView paramProfileView ) throws UserException;

	public abstract ProfileView getProfile( Long paramLong );

	public abstract void addRightToProfile( String paramString, RightEnum paramRightEnum );

	public abstract RightEnum[] getAllProfileRightsEnum();

	public abstract List<ProfileView> listAllProfiles();

	public abstract void certRemoved( String paramString );

	public abstract void validateCertificates( String[] paramArrayOfString );

	public abstract Set<Long> allowedGroups( List<String> paramList );

	public abstract MemberView assembleRightsAndResources( MemberView paramMemberView, boolean paramBoolean ) throws UserException;

	public abstract void updateMemberDetailsView( MemberView paramMemberView );

	public abstract List<String> findMembersNames( String... paramVarArgs );

	public abstract void processAppUninstalled( String paramString );

	public abstract void processAppInstalled( String paramString );

	public abstract void processAppStarted( String paramString );

	public abstract void processAppUpgraded( String paramString );

	public abstract byte[] exportUserRights() throws UserException, TopologyException;

	public abstract Map<String, Set<String>> getAllAppRights();

	public abstract void acceptTerms() throws UserException;

	public abstract boolean hasRight( RightEnum paramRightEnum );

	public abstract List<String> getGroups( MemberView paramMemberView );

	public abstract List<MemberView> listNotDeletedMembers( boolean paramBoolean ) throws UserException;

	public abstract void replaceChildResource( Long paramLong1, Long paramLong2 );

	public abstract void updateMemberPassword( MemberView paramMemberView, String paramString );

	public abstract boolean authenticateUser( MemberView paramMemberView, String paramString );

	public abstract ProfileView getProfileForUser( String paramString ) throws UserException;

	public abstract ProfileView updateProfile( ProfileView paramProfileView, boolean paramBoolean ) throws UserException;

	public abstract boolean checkDeletions( List<Long> paramList );
}

