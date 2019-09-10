package com.marchnetworks.command.common.user;

import javax.xml.ws.WebFault;

@WebFault( name = "UserFault" )
public class UserException extends Exception
{
	private UserExceptionTypeEnum error;

	public UserException( String message )
	{
		super( message );
	}

	public UserException( String message, UserExceptionTypeEnum userExceptionType )
	{
		super( message );
		error = userExceptionType;
	}

	public UserException( String message, Throwable cause )
	{
		super( message, cause );
	}

	public UserExceptionTypeEnum getError()
	{
		return error;
	}
}
