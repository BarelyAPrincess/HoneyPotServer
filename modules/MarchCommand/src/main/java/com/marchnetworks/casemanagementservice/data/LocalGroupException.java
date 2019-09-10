package com.marchnetworks.casemanagementservice.data;

public class LocalGroupException extends Exception
{
	private static final long serialVersionUID = 1L;

	private LocalGroupExceptionType error;

	public LocalGroupException()
	{
	}

	public enum LocalGroupExceptionType
	{
		LOCAL_GROUP_NOT_FOUND,
		LOCAL_GROUP_DATA_CORRUPTED,
		LOCAL_GROUP_NAME_NOT_SET,
		LOCAL_GROUP_NAME_ALREADY_EXISTS;

		private LocalGroupExceptionType()
		{
		}
	}

	public LocalGroupException( LocalGroupExceptionType exceptionType )
	{
		error = exceptionType;
	}

	public LocalGroupException( String message )
	{
		super( message );
	}

	public LocalGroupException( String message, LocalGroupExceptionType exceptionType )
	{
		super( message );
		error = exceptionType;
	}

	public LocalGroupException( Throwable cause )
	{
		super( cause );
	}

	public LocalGroupException( Throwable cause, LocalGroupExceptionType exceptionType )
	{
		super( cause );
		error = exceptionType;
	}

	public LocalGroupException( String message, Throwable cause )
	{
		super( message, cause );
	}

	public LocalGroupException( String message, Throwable cause, LocalGroupExceptionType exceptionType )
	{
		super( message, cause );
		error = exceptionType;
	}

	public LocalGroupExceptionType getError()
	{
		return error;
	}
}
