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

public abstract class ApplicationException extends Exception implements IException
{
	public static ApplicationException fatal( String message )
	{
		return new Error( ReportingLevel.E_ERROR, message );
	}

	public static ApplicationException fatal( String message, Throwable cause )
	{
		return new Error( ReportingLevel.E_ERROR, message, cause );
	}

	public static ApplicationException fatal( Throwable cause )
	{
		return new Error( ReportingLevel.E_ERROR, cause );
	}

	public static Ignorable ignorable( String message )
	{
		return new Ignorable( ReportingLevel.E_IGNORABLE, message );
	}

	public static Ignorable ignorable( Throwable cause )
	{
		return new Ignorable( ReportingLevel.E_IGNORABLE, cause );
	}

	public static Ignorable ignorable( String message, Throwable cause )
	{
		return new Ignorable( ReportingLevel.E_IGNORABLE, message, cause );
	}

	public static ApplicationException notice( String message, Throwable cause )
	{
		return new Error( ReportingLevel.E_NOTICE, message, cause );
	}

	public static ApplicationException notice( Throwable cause )
	{
		return new Error( ReportingLevel.E_NOTICE, cause );
	}

	public static ApplicationException notice( String message )
	{
		return new Error( ReportingLevel.E_NOTICE, message );
	}

	protected final ReportingLevel level;

	public ApplicationException( ReportingLevel level )
	{
		this.level = level;
	}

	public ApplicationException( ReportingLevel level, String message )
	{
		super( message );
		this.level = level;
	}

	public ApplicationException( ReportingLevel level, String message, Throwable cause )
	{
		super( message, cause );
		this.level = level;

		if ( cause.getClass().isAssignableFrom( getClass() ) )
			throw new IllegalArgumentException( "The cause argument can't be same class. {cause: " + cause.getClass() + ", this: " + getClass() + "}" );
	}

	public ApplicationException( ReportingLevel level, Throwable cause )
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
		// return String.format( "Exception %s thrown in file '%s' at line %s: '%s'", getClass().getName(), getStackTrace()[0].getFileName(), getStackTrace()[0].getLineNumber(), super.getMessage() );
	}

	public boolean hasCause()
	{
		return getCause() != null;
	}

	@Override
	public ReportingLevel reportingLevel()
	{
		return level;
	}

	public static class Error extends ApplicationException
	{
		public Error( ReportingLevel level )
		{
			super( level );
		}

		public Error( ReportingLevel level, String message )
		{
			super( level, message );
		}

		public Error( ReportingLevel level, String message, Throwable cause )
		{
			super( level, message, cause );
		}

		public Error( ReportingLevel level, Throwable cause )
		{
			super( level, cause );
		}

		@Override
		public ReportingLevel handle( ExceptionReport report, ExceptionContext context )
		{
			return null;
		}
	}

	public static class Ignorable extends RuntimeException implements IException
	{
		private ReportingLevel level;

		public Ignorable( ReportingLevel level )
		{
			this.level = level;
		}

		public Ignorable( ReportingLevel level, String message )
		{
			super( message );
			this.level = level;
		}

		public Ignorable( ReportingLevel level, String message, Throwable cause )
		{
			super( message, cause );
			this.level = level;
		}

		public Ignorable( ReportingLevel level, Throwable cause )
		{
			super( cause );
			this.level = level;
		}

		@Override
		public ReportingLevel reportingLevel()
		{
			return level;
		}

		@Override
		public ReportingLevel handle( ExceptionReport report, ExceptionContext context )
		{
			return null;
		}

		@Override
		public boolean isIgnorable()
		{
			return level.isIgnorable();
		}
	}

	public static class Runtime extends RuntimeException implements IException
	{
		protected final ReportingLevel level;

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
		public ReportingLevel reportingLevel()
		{
			return null;
		}

		@Override
		public ReportingLevel handle( ExceptionReport report, ExceptionContext context )
		{
			return null;
		}

		@Override
		public boolean isIgnorable()
		{
			return level.isIgnorable();
		}
	}

	@Override
	public boolean isIgnorable()
	{
		return level.isIgnorable();
	}
}
