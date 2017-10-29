package io.amelia.lang;

public class SynchronizeException extends ApplicationException
{
	public SynchronizeException( String message )
	{
		super( ReportingLevel.E_ERROR, message );
	}

	public SynchronizeException( String message, Throwable cause )
	{
		super( ReportingLevel.E_ERROR, message, cause );
	}

	public SynchronizeException( Throwable cause )
	{
		super( ReportingLevel.E_ERROR, cause );
	}

	@Override
	public ReportingLevel handle( ExceptionReport report, ExceptionContext context )
	{
		return null;
	}
}
