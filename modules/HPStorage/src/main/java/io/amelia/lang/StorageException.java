package io.amelia.lang;

public class StorageException
{
	public static Error error( String message )
	{
		return new Error( message );
	}

	public static Error error( String message, Throwable cause )
	{
		return new Error( message, cause );
	}

	public static Error error( Throwable cause )
	{
		return new Error( cause );
	}

	public static Ignorable ignorable( String message )
	{
		return new Ignorable( message );
	}

	public static Ignorable ignorable( String message, Throwable cause )
	{
		return new Ignorable( message, cause );
	}

	public static Ignorable ignorable( Throwable cause )
	{
		return new Ignorable( cause );
	}

	private StorageException()
	{
		// Static Access
	}

	public static class Error extends ApplicationException.Error
	{
		public Error()
		{
			super();
		}

		public Error( String message )
		{
			super( message );
		}

		public Error( String message, Throwable cause )
		{
			super( message, cause );
		}

		public Error( Throwable cause )
		{
			super( cause );
		}
	}

	public static class Ignorable extends ApplicationException.Ignorable
	{
		public Ignorable()
		{
			super();
		}

		public Ignorable( String message )
		{
			super( message );
		}

		public Ignorable( String message, Throwable cause )
		{
			super( message, cause );
		}

		public Ignorable( Throwable cause )
		{
			super( cause );
		}
	}
}
