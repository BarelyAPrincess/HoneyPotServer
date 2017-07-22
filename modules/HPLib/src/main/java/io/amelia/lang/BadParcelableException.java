package io.amelia.lang;

public class BadParcelableException extends RuntimeException implements IException
{
	public BadParcelableException()
	{
		super();
	}

	public BadParcelableException( String message )
	{
		super( message );
	}

	public BadParcelableException( String message, Throwable cause )
	{
		super( message, cause );
	}

	public BadParcelableException( Throwable cause )
	{
		super( cause );
	}

	@Override
	public ReportingLevel reportingLevel()
	{
		return ReportingLevel.E_ERROR;
	}

	@Override
	public ReportingLevel handle( ExceptionReport report, ExceptionContext context )
	{
		return null;
	}

	@Override
	public boolean isIgnorable()
	{
		return false;
	}
}
