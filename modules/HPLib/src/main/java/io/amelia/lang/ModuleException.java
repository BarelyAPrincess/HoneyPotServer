package io.amelia.lang;

public class ModuleException extends ApplicationException
{
	public ModuleException()
	{
		super( ReportingLevel.E_USER_ERROR );
	}

	public ModuleException( String message )
	{
		super( ReportingLevel.E_USER_ERROR, message );
	}

	public ModuleException( String message, Throwable cause )
	{
		super( ReportingLevel.E_USER_ERROR, message, cause );
	}

	public ModuleException( Throwable cause )
	{
		super( ReportingLevel.E_USER_ERROR, cause );
	}

	@Override
	public ReportingLevel handle( ExceptionReport report, ExceptionContext context )
	{
		return null;
	}
}
