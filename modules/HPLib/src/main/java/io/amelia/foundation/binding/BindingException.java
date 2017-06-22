package io.amelia.foundation.binding;

import io.amelia.lang.ApplicationException;
import io.amelia.lang.ExceptionContext;
import io.amelia.lang.ExceptionReport;
import io.amelia.lang.ReportingLevel;

public class BindingException extends ApplicationException
{
	public BindingException()
	{
		super( ReportingLevel.E_ERROR );
	}

	public BindingException( String message )
	{
		super( ReportingLevel.E_ERROR, message );
	}

	public BindingException( String message, Throwable cause )
	{
		super( ReportingLevel.E_ERROR, message, cause );
	}

	public BindingException( Throwable cause )
	{
		super( ReportingLevel.E_ERROR, cause );
	}

	@Override
	public ReportingLevel handle( ExceptionReport report, ExceptionContext context )
	{
		return null;
	}
}
