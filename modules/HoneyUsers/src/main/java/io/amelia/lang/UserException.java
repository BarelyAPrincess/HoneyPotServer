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

import io.amelia.users.User;

/**
 * User Exception Container
 */
public class UserException
{
	private UserException()
	{
		// Static
	}

	public static class Error extends ApplicationException.Error
	{
		private static final long serialVersionUID = 5522301956671473324L;

		private final User user;

		public Error( User user )
		{
			super();
			this.user = user;
		}

		public Error( User user, String message )
		{
			super( message );
			this.user = user;
		}

		public Error( User user, String message, Throwable cause )
		{
			super( message, cause );
			this.user = user;
		}

		public Error( User user, Throwable cause )
		{
			super( cause );
			this.user = user;
		}

		public Error( User user, ReportingLevel level )
		{
			super( level );
			this.user = user;
		}

		public Error( User user, ReportingLevel level, String message )
		{
			super( level, message );
			this.user = user;
		}

		public Error( User user, ReportingLevel level, String message, Throwable cause )
		{
			super( level, message, cause );
			this.user = user;
		}

		public Error( User user, ReportingLevel level, Throwable cause )
		{
			super( level, cause );
			this.user = user;
		}

		public User getUser()
		{
			return user;
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
