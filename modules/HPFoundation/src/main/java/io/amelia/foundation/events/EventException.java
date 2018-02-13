/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation.events;

import io.amelia.lang.ApplicationException;

public class EventException
{
	private EventException()
	{
		// Private Wrapper Class
	}

	public static class Error extends ApplicationException.Error
	{
		private static final long serialVersionUID = 3532808232324183999L;

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
		private static final long serialVersionUID = 3532808232324183999L;

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
		private static final long serialVersionUID = 3532808232324183999L;

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
}
