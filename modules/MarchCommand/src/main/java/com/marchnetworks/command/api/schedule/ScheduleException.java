package com.marchnetworks.command.api.schedule;

import javax.xml.ws.WebFault;

@WebFault( name = "ScheduleException" )
public class ScheduleException extends Exception
{
	private ScheduleExceptionType error;

	public ScheduleException()
	{
	}

	public ScheduleException( ScheduleExceptionType error, Throwable cause )
	{
		super( cause );
		this.error = error;
	}

	public ScheduleException( ScheduleExceptionType error, String message )
	{
		super( message );
		this.error = error;
	}

	public ScheduleException( ScheduleExceptionType error, String message, Throwable cause )
	{
		super( message, cause );
		this.error = error;
	}

	public ScheduleExceptionType getError()
	{
		return error;
	}

	public void setError( ScheduleExceptionType error )
	{
		this.error = error;
	}
}
