package com.marchnetworks.license.exception;

public class ServerIdCompareException extends Exception
{
	private static final long serialVersionUID = 1697744201477608927L;

	public ServerIdCompareException( String msg )
	{
		super( msg );
	}

	public ServerIdCompareException( String msg, Throwable t )
	{
		super( msg, t );
	}
}
