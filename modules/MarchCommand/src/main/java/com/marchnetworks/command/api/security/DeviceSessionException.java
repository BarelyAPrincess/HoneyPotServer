package com.marchnetworks.command.api.security;

public class DeviceSessionException extends Exception
{
	private static final long serialVersionUID = -630912680879894042L;

	public DeviceSessionException()
	{
	}

	public DeviceSessionException( String msg )
	{
		super( msg );
	}

	public DeviceSessionException( Throwable cause )
	{
		super( cause );
	}
}
