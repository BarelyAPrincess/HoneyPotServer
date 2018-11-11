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

import io.amelia.users.UserPrincipal;

/**
 * UserPrincipal Exception Container
 */
public class UserException
{
	private UserException()
	{
		// Static Access
	}

	public static class Error extends ApplicationException.Error
	{
		private static final long serialVersionUID = 5522301956671473324L;

		private final UserPrincipal userPrincipal;

		public Error( UserPrincipal userPrincipal )
		{
			super();
			this.userPrincipal = userPrincipal;
		}

		public Error( UserPrincipal userPrincipal, String message )
		{
			super( message );
			this.userPrincipal = userPrincipal;
		}

		public Error( UserPrincipal userPrincipal, String message, Throwable cause )
		{
			super( message, cause );
			this.userPrincipal = userPrincipal;
		}

		public Error( UserPrincipal userPrincipal, Throwable cause )
		{
			super( cause );
			this.userPrincipal = userPrincipal;
		}

		public Error( UserPrincipal userPrincipal, ReportingLevel level )
		{
			super( level );
			this.userPrincipal = userPrincipal;
		}

		public Error( UserPrincipal userPrincipal, ReportingLevel level, String message )
		{
			super( level, message );
			this.userPrincipal = userPrincipal;
		}

		public Error( UserPrincipal userPrincipal, ReportingLevel level, String message, Throwable cause )
		{
			super( level, message, cause );
			this.userPrincipal = userPrincipal;
		}

		public Error( UserPrincipal userPrincipal, ReportingLevel level, Throwable cause )
		{
			super( level, cause );
			this.userPrincipal = userPrincipal;
		}

		public Error( UserPrincipal userPrincipal, DescriptiveReason descriptiveReason, Throwable cause )
		{
			this( userPrincipal, descriptiveReason.getReportingLevel(), descriptiveReason.getReasonMessage(), cause );
		}

		public Error( UserPrincipal userPrincipal, DescriptiveReason descriptiveReason )
		{
			this( userPrincipal, descriptiveReason.getReportingLevel(), descriptiveReason.getReasonMessage() );
		}

		public UserPrincipal getUserPrincipal()
		{
			return userPrincipal;
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
