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
 * Covers all exceptions that could throw during plugin loads, unloads or etc.
 */
public class PluginException
{
	private PluginException()
	{
		// Container Class
	}

	public static class Error extends ApplicationException.Error
	{
		public Error()
		{
			super( ReportingLevel.E_ERROR );
		}

		public Error( String message )
		{
			super( ReportingLevel.E_ERROR, message );
		}

		public Error( String message, Throwable cause )
		{
			super( ReportingLevel.E_ERROR, message, cause );
		}

		public Error( Throwable cause )
		{
			super( ReportingLevel.E_ERROR, cause );
		}
	}

	public static class Ignorable extends ApplicationException.Ignorable
	{
		public Ignorable()
		{
			super( ReportingLevel.E_IGNORABLE );
		}

		public Ignorable( String message )
		{
			super( ReportingLevel.E_IGNORABLE, message );
		}

		public Ignorable( String message, Throwable cause )
		{
			super( ReportingLevel.E_IGNORABLE, message, cause );
		}

		public Ignorable( Throwable cause )
		{
			super( ReportingLevel.E_IGNORABLE, cause );
		}
	}

	public static class Internal extends ApplicationException.Runtime implements IException
	{
		public Internal()
		{
			super( ReportingLevel.E_ERROR );
		}

		public Internal( String message )
		{
			super( ReportingLevel.E_ERROR, message );
		}

		public Internal( String message, Throwable cause )
		{
			super( ReportingLevel.E_ERROR, message, cause );
		}

		public Internal( Throwable cause )
		{
			super( ReportingLevel.E_ERROR, cause );
		}
	}
}
