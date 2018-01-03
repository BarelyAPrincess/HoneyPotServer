package io.amelia.lang;

public class SynchronizeException extends ApplicationException.Error
{
	public SynchronizeException( String message )
	{
		super( message );
	}

	public SynchronizeException( String message, Throwable cause )
	{
		super( message, cause );
	}

	public SynchronizeException( Throwable cause )
	{
		super( cause );
	}
}
