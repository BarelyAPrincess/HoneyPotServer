package com.marchnetworks.command.api.notification;

import javax.xml.ws.WebFault;

@WebFault( name = "NotificationException" )
public class NotificationException extends Exception
{
	private NotificationExceptionType error;

	public NotificationException()
	{
	}

	public NotificationException( NotificationExceptionType error, Throwable cause )
	{
		super( cause );
		this.error = error;
	}

	public NotificationException( NotificationExceptionType error, String message )
	{
		super( message );
		this.error = error;
	}

	public NotificationException( NotificationExceptionType error, String message, Throwable cause )
	{
		super( message, cause );
		this.error = error;
	}

	public NotificationExceptionType getError()
	{
		return error;
	}

	public void setError( NotificationExceptionType error )
	{
		this.error = error;
	}
}
