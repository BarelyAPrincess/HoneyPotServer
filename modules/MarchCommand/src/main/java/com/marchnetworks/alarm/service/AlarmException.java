package com.marchnetworks.alarm.service;

import javax.xml.ws.WebFault;

@WebFault( name = "AlarmFault" )
public class AlarmException extends Exception
{
	AlarmExceptionTypeEnum error;

	public AlarmException( AlarmExceptionTypeEnum err )
	{
		error = err;
	}

	public AlarmException( AlarmExceptionTypeEnum err, String message )
	{
		super( message );
		error = err;
	}

	public AlarmException( AlarmExceptionTypeEnum err, Throwable cause )
	{
		super( cause );
		error = err;
	}

	public AlarmException( AlarmExceptionTypeEnum err, String message, Throwable cause )
	{
		super( message, cause );
		error = err;
	}

	public AlarmExceptionTypeEnum getError()
	{
		return error;
	}

	public void setError( AlarmExceptionTypeEnum error )
	{
		this.error = error;
	}

	public String toString()
	{
		return "AlarmException [error=" + error + "]";
	}
}
