/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.lang;

/**
 * Thrown for almost all Session Exceptions
 */
public class SessionException
{
	public static NetworkException.Error error( String message )
	{
		return new NetworkException.Error( message );
	}

	public static NetworkException.Error error( String message, Throwable cause )
	{
		return new NetworkException.Error( message, cause );
	}

	public static NetworkException.Error error( Throwable cause )
	{
		return new NetworkException.Error( cause );
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
}
