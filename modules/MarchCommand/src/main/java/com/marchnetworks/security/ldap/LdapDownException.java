package com.marchnetworks.security.ldap;

import com.marchnetworks.command.api.security.AuthenticationExceptionReasonCodes;
import com.marchnetworks.command.api.security.UserAuthenticationException;

public class LdapDownException extends UserAuthenticationException
{
	public LdapDownException( String message )
	{
		super( message, AuthenticationExceptionReasonCodes.LDAP_CONNECTION_FAILED );
	}

	public LdapDownException( String message, Throwable cause )
	{
		super( message, cause );
		setReasonCode( AuthenticationExceptionReasonCodes.LDAP_CONNECTION_FAILED );
	}
}

