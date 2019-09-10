package com.marchnetworks.security.authentication;

import com.marchnetworks.command.spring.security.SessionAuthenticationToken;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CommandSessionSecurityContextRepository extends HttpSessionSecurityContextRepository
{
	public boolean containsContext( HttpServletRequest request )
	{
		SecurityContext context = SecurityContextHolder.getContext();

		Authentication authentication = context.getAuthentication();
		if ( ( authentication != null ) && ( ( authentication instanceof SessionAuthenticationToken ) ) && ( authentication.isAuthenticated() ) )
		{
			return true;
		}
		return super.containsContext( request );
	}

	public void saveContext( SecurityContext context, HttpServletRequest request, HttpServletResponse response )
	{
		Authentication authentication = context.getAuthentication();
		if ( ( authentication != null ) && ( ( authentication instanceof SessionAuthenticationToken ) ) && ( authentication.isAuthenticated() ) )
		{
			return;
		}
		super.saveContext( context, request, response );
	}
}

