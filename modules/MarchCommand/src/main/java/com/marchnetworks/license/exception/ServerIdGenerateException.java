package com.marchnetworks.license.exception;

public class ServerIdGenerateException extends Exception
{
	private static final long serialVersionUID = -5749388700459317455L;
	private static final String DEFAULT_MSG = "Error generating Server Id";

	public ServerIdGenerateException()
	{
		super( "Error generating Server Id" );
	}

	public ServerIdGenerateException( Throwable cause )
	{
		super( "Error generating Server Id", cause );
	}

	public ServerIdGenerateException( String msg )
	{
		super( msg );
	}

	public ServerIdGenerateException( String msg, Throwable cause )
	{
		super( msg, cause );
	}
}
