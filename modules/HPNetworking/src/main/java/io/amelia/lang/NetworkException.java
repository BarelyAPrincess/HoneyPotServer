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

public class NetworkException
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

	public static Ignorable ignorable( Throwable cause )
	{
		return new Ignorable( cause );
	}

	public static Ignorable ignorable( String message, Throwable cause )
	{
		return new Ignorable( message, cause );
	}

	public static Notice notice( String message, Throwable cause )
	{
		return new Notice( message, cause );
	}

	public static Notice notice( Throwable cause )
	{
		return new Notice( cause );
	}

	public static Notice notice( String message )
	{
		return new Notice( message );
	}

	public static Runtime runtime( String message )
	{
		return new Runtime( message );
	}

	public static Runtime runtime( Throwable cause )
	{
		return new Runtime( cause );
	}

	public static Runtime runtime( String message, Throwable cause )
	{
		return new Runtime( message, cause );
	}

	private NetworkException()
	{
		// Static
	}

	public static class Ignorable extends ApplicationException.Runtime
	{
		private static final long serialVersionUID = 5522301956671473324L;

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

	public static class Notice extends ApplicationException.Error
	{
		private static final long serialVersionUID = 5522301956671473324L;

		public Notice()
		{
			super();
		}

		public Notice( String message )
		{
			super( message );
		}

		public Notice( String message, Throwable cause )
		{
			super( message, cause );
		}

		public Notice( Throwable cause )
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
