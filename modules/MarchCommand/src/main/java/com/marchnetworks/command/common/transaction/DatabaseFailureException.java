package com.marchnetworks.command.common.transaction;

public class DatabaseFailureException extends UnrecoverableException
{
	private static final long serialVersionUID = 4093940107524903290L;

	public DatabaseFailureException()
	{
	}

	public DatabaseFailureException( String msg )
	{
		super( msg );
	}

	public DatabaseFailureException( Throwable cause )
	{
		super( cause );
	}

	public DatabaseFailureException( String msg, Throwable cause )
	{
		super( msg, cause );
	}
}
