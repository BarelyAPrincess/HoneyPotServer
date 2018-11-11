/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.lang;

/**
 * Covers all exceptions that could throw during plugin loads, unloads or etc.
 */
public class PluginException
{
	private PluginException()
	{
		// Private Wrapper Class
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

	public static class Internal extends ApplicationException.Runtime
	{
		public Internal()
		{
			super();
		}

		public Internal( String message )
		{
			super( message );
		}

		public Internal( String message, Throwable cause )
		{
			super( message, cause );
		}

		public Internal( Throwable cause )
		{
			super( cause );
		}
	}

	public static class Unconfigured extends Error
	{
		public Unconfigured( String message )
		{
			super( message );
		}
	}
}
