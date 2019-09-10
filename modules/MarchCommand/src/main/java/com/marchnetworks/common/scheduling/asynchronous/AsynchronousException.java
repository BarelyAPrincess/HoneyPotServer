package com.marchnetworks.common.scheduling.asynchronous;

public class AsynchronousException extends Exception
{
	private AsynchronousException( String message )
	{
		super( message );
	}

	public static AsynchronousException interruptedException( String message )
	{
		return new AsynchronousException( message );
	}
}
