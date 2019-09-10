package com.marchnetworks.management.config.service;

import javax.xml.ws.WebFault;

@WebFault
public class ConfigurationException extends Exception
{
	private static final long serialVersionUID = 3878834897948598379L;
	private ConfigurationExceptionType error = ConfigurationExceptionType.NO_ERROR;

	public ConfigurationExceptionType getErrorType()
	{
		return error;
	}

	public void setErrorType( ConfigurationExceptionType error )
	{
		this.error = error;
	}

	public ConfigurationException()
	{
	}

	public ConfigurationException( ConfigurationExceptionType error, String msg )
	{
		super( msg );
		this.error = error;
	}

	public ConfigurationException( String msg )
	{
		super( msg );
	}

	public ConfigurationException( Throwable throwable )
	{
		super( throwable );
	}

	public ConfigurationException( String msg, Throwable cause )
	{
		super( msg, cause );
	}
}
