/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation.binding;

import io.amelia.lang.ApplicationException;

public class BindingException
{
	private BindingException()
	{

	}

	public static class Denied extends ApplicationException.Ignorable
	{
		public Denied()
		{
			super();
		}

		public Denied( String message )
		{
			super( message );
		}

		public Denied( String message, Throwable cause )
		{
			super( message, cause );
		}

		public Denied( Throwable cause )
		{
			super( cause );
		}
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
