package com.marchnetworks.security.ldap;

import com.marchnetworks.command.common.user.data.MemberView;

import java.util.List;

public abstract interface LDAPService
{
	public abstract List<MemberView> lookupUsers( String paramString, int paramInt ) throws LdapException;

	public abstract List<String> getGroups( MemberView paramMemberView ) throws LdapException;

	public abstract MemberView lookupUniqueUser( String paramString ) throws LdapException;

	public abstract MemberView lookupUniqueUser( String paramString, LDAPField paramLDAPField ) throws LdapException;

	public abstract AuthenticationToken bind( String paramString1, String paramString2, String paramString3, String paramString4 ) throws LdapDownException, LdapException;
}

