package com.marchnetworks.security.ldap;

import com.marchnetworks.command.api.security.AuthenticationExceptionReasonCodes;
import com.marchnetworks.command.api.security.UserAuthenticationException;

public class LdapException extends UserAuthenticationException
{
	public LdapException( String message )
	{
		super( message, AuthenticationExceptionReasonCodes.NOT_AUTHORIZED );
	}

	public LdapException( String message, Throwable throwable )
	{
		super( message, throwable );
	}

	public LdapException( AuthenticationExceptionReasonCodes code )
	{
		super( code );
	}
}

