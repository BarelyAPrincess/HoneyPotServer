package com.marchnetworks.app.data;

public class AppParseException extends Exception
{
	private static final long serialVersionUID = 9006837601500241374L;

	public AppParseException()
	{
	}

	public AppParseException( String msg )
	{
		super( msg );
	}

	public AppParseException( Throwable cause )
	{
		super( cause );
	}

	public AppParseException( String msg, Throwable cause )
	{
		super( msg, cause );
	}
}
