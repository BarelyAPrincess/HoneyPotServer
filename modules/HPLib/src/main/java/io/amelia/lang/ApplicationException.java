/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.lang;

import javax.annotation.Nonnull;

public final class ApplicationException
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

	private ApplicationException()
	{
		// Static
	}

	public static class Error extends Exception implements ExceptionContext
	{
		protected final ReportingLevel level;

		public Error()
		{
			this( ReportingLevel.E_ERROR );
		}

		public Error( String message )
		{
			this( ReportingLevel.E_ERROR, message );
		}

		public Error( String message, Throwable cause )
		{
			this( ReportingLevel.E_ERROR, message, cause );
		}

		public Error( Throwable cause )
		{
			this( ReportingLevel.E_ERROR, cause );
		}

		public Error( ReportingLevel level )
		{
			this.level = level;
		}

		public Error( ReportingLevel level, String message )
		{
			super( message );
			this.level = level;
		}

		public Error( ReportingLevel level, String message, Throwable cause )
		{
			super( message, cause );
			this.level = level;

			if ( cause.getClass().isAssignableFrom( getClass() ) )
				throw new IllegalArgumentException( "The cause argument can't be same class. {cause: " + cause.getClass() + ", this: " + getClass() + "}" );
		}

		public Error( ReportingLevel level, Throwable cause )
		{
			super( cause );
			this.level = level;

			if ( cause.getClass().isAssignableFrom( getClass() ) )
				throw new IllegalArgumentException( "The cause argument can't be same class. {cause: " + cause.getClass() + ", this: " + getClass() + "}" );
		}

		@Override
		public String getMessage()
		{
			return super.getMessage();
			// return String.format( "Exception %s thrown in file '%s' at line %s: '%s'", getClass().getProductName(), getStackTrace()[0].getFileName(), getStackTrace()[0].getLineNumber(), super.getMessage() );
		}

		@Override
		public ReportingLevel getReportingLevel()
		{
			return level;
		}

		@Nonnull
		@Override
		public Throwable getThrowable()
		{
			return this;
		}

		@Override
		public ReportingLevel handle( ExceptionReport report, ExceptionRegistrar registrar )
		{
			return null;
		}

		public boolean hasCause()
		{
			return getCause() != null;
		}

		@Override
		public boolean isIgnorable()
		{
			return level.isIgnorable();
		}
	}

	public static class Ignorable extends Runtime
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

	public static class Notice extends Error
	{
		public Notice()
		{
			super( ReportingLevel.E_NOTICE );
		}

		public Notice( String message )
		{
			super( ReportingLevel.E_NOTICE, message );
		}

		public Notice( String message, Throwable cause )
		{
			super( ReportingLevel.E_NOTICE, message, cause );
		}

		public Notice( Throwable cause )
		{
			super( ReportingLevel.E_NOTICE, cause );
		}
	}

	public static class Runtime extends RuntimeException implements ExceptionContext
	{
		protected final ReportingLevel level;

		public Runtime()
		{
			this( ReportingLevel.E_USER_ERROR );
		}

		public Runtime( String message )
		{
			this( ReportingLevel.E_USER_ERROR, message );
		}

		public Runtime( String message, Throwable cause )
		{
			this( ReportingLevel.E_USER_ERROR, message, cause );
		}

		public Runtime( Throwable cause )
		{
			this( ReportingLevel.E_USER_ERROR, cause );
		}

		public Runtime( ReportingLevel level )
		{
			this.level = level;
		}

		public Runtime( ReportingLevel level, String message )
		{
			super( message );
			this.level = level;
		}

		public Runtime( ReportingLevel level, String message, Throwable cause )
		{
			super( message, cause );
			this.level = level;
		}

		public Runtime( ReportingLevel level, Throwable cause )
		{
			super( cause );
			this.level = level;
		}

		@Override
		public ReportingLevel getReportingLevel()
		{
			return null;
		}

		@Nonnull
		@Override
		public Throwable getThrowable()
		{
			return this;
		}

		@Override
		public ReportingLevel handle( ExceptionReport report, ExceptionRegistrar registrar )
		{
			return null;
		}

		public boolean hasCause()
		{
			return getCause() != null;
		}

		@Override
		public boolean isIgnorable()
		{
			return level.isIgnorable();
		}
	}
}
