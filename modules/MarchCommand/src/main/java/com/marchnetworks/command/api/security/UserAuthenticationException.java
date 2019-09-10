package com.marchnetworks.command.api.security;

import org.springframework.security.core.AuthenticationException;

public class UserAuthenticationException extends AuthenticationException
{
	private String userName;
	private String reasonCode;
	private static final long serialVersionUID = -8690816799164330974L;
	private static final String HEADER = "Unauthorized";

	public UserAuthenticationException( AuthenticationExceptionReasonCodes reason )
	{
		super( "Unauthorized" );
		setReasonCode( reason.getValue() );
	}

	public UserAuthenticationException( String message, Throwable cause )
	{
		super( message, cause );
	}

	public UserAuthenticationException( String message, AuthenticationExceptionReasonCodes reason )
	{
		super( message );
		reasonCode = reason.getValue();
	}

	public String getReasonCode()
	{
		return reasonCode;
	}

	public void setReasonCode( String reasonCode )
	{
		this.reasonCode = reasonCode;
	}

	public void setReasonCode( AuthenticationExceptionReasonCodes reason )
	{
		reasonCode = reason.getValue();
	}

	public String getUserName()
	{
		return userName;
	}

	public void setUserName( String userName )
	{
		this.userName = userName;
	}
}
