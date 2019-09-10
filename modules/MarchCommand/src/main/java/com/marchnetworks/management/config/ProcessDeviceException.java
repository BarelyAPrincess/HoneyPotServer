package com.marchnetworks.management.config;

public class ProcessDeviceException extends Exception
{
	private static final long serialVersionUID = -3316988667887725995L;

	public ProcessDeviceException()
	{
	}

	public ProcessDeviceException( String msg )
	{
		super( msg );
	}

	public ProcessDeviceException( Throwable throwable )
	{
		super( throwable );
	}

	public ProcessDeviceException( String msg, Throwable cause )
	{
		super( msg, cause );
	}
}
