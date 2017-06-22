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
		return new DefaultApplicationException( ReportingLevel.E_ERROR, message );
	}

	public static ApplicationException fatal( String message, Throwable cause )
	{
		return new DefaultApplicationException( ReportingLevel.E_ERROR, message, cause );
	}

	public static ApplicationException fatal( Throwable cause )
	{
		return new DefaultApplicationException( ReportingLevel.E_ERROR, cause );
	}

	public static ApplicationException notice( String message )
	{
		return new DefaultApplicationException( ReportingLevel.E_NOTICE, message );
	}

	public static ApplicationException notice( String message, Throwable cause )
	{
		return new DefaultApplicationException( ReportingLevel.E_NOTICE, message, cause );
	}

	public static ApplicationException notice( Throwable cause )
	{
		return new DefaultApplicationException( ReportingLevel.E_NOTICE, cause );
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

	public static class DefaultApplicationException extends ApplicationException
	{
		DefaultApplicationException( ReportingLevel level )
		{
			super( level );
		}

		DefaultApplicationException( ReportingLevel level, String message )
		{
			super( level, message );
		}

		DefaultApplicationException( ReportingLevel level, String message, Throwable cause )
		{
			super( level, message, cause );
		}

		DefaultApplicationException( ReportingLevel level, Throwable cause )
		{
			super( level, cause );
		}

		@Override
		public ReportingLevel handle( ExceptionReport report, ExceptionContext context )
		{
			return level;
		}
	}

	@Override
	public boolean isIgnorable()
	{
		return level.isIgnorable();
	}
}
