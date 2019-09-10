/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.lang;

/**
 * Thrown for almost all Session Exceptions
 */
public class SessionException
{
	public static SessionException.Error error( String message )
	{
		return new SessionException.Error( message );
	}

	public static SessionException.Error error( String message, Throwable cause )
	{
		return new SessionException.Error( message, cause );
	}

	public static SessionException.Error error( Throwable cause )
	{
		return new SessionException.Error( cause );
	}

	public static SessionException.Runtime runtime( String message )
	{
		return new SessionException.Runtime( message );
	}

	public static SessionException.Runtime runtime( String message, Throwable cause )
	{
		return new SessionException.Runtime( message, cause );
	}

	public static SessionException.Runtime runtime( Throwable cause )
	{
		return new SessionException.Runtime( cause );
	}

	private SessionException()
	{
		// Static
	}

	public static class Error extends ApplicationException.Error
	{
		private static final long serialVersionUID = 5522301956671473324L;

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

	public static class Runtime extends ApplicationException.Runtime
	{
		private static final long serialVersionUID = 5522301956671473324L;

		public Runtime()
		{
			super();
		}

		public Runtime( String message )
		{
			super( message );
		}

		public Runtime( String message, Throwable cause )
		{
			super( message, cause );
		}

		public Runtime( Throwable cause )
		{
			super( cause );
		}
	}
}
