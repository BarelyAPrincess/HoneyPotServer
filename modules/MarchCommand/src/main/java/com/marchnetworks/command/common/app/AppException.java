package com.marchnetworks.command.common.app;

import javax.xml.ws.WebFault;

@WebFault( name = "AppFault" )
public class AppException extends RuntimeException
{
	AppExceptionTypeEnum error;

	public AppException( AppExceptionTypeEnum err )
	{
		error = err;
	}

	public AppException( AppExceptionTypeEnum err, String message )
	{
		super( message );
		error = err;
	}

	public AppException( AppExceptionTypeEnum err, Throwable cause )
	{
		super( cause );
		error = err;
	}

	public AppException( AppExceptionTypeEnum err, String message, Throwable cause )
	{
		super( message, cause );
		error = err;
	}

	public AppExceptionTypeEnum getError()
	{
		return error;
	}

	public void setError( AppExceptionTypeEnum error )
	{
		this.error = error;
	}

	public String toString()
	{
		return "AppException [error=" + error + "] " + getMessage();
	}
}
