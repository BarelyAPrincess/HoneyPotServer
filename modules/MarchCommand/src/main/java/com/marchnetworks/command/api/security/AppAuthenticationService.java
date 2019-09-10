package com.marchnetworks.command.api.security;

import com.marchnetworks.command.common.user.data.MemberView;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public interface AppAuthenticationService
{
	void authenticate( MemberView paramMemberView, UsernamePasswordAuthenticationToken paramUsernamePasswordAuthenticationToken ) throws UserAuthenticationException;

	void modifyMember( MemberView paramMemberView );
}
