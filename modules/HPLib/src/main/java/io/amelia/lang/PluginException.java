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
public class PluginException extends ApplicationException
{
	private static final long serialVersionUID = -985004348649679626L;

	public PluginException()
	{
		super( ReportingLevel.E_ERROR );
	}

	public PluginException( String message )
	{
		super( ReportingLevel.E_ERROR, message );
	}

	public PluginException( String message, Throwable cause )
	{
		super( ReportingLevel.E_ERROR, message, cause );
	}

	public PluginException( Throwable cause )
	{
		super( ReportingLevel.E_ERROR, cause );
	}

	@Override
	public ReportingLevel handle( ExceptionReport report, ExceptionContext context )
	{
		return null;
	}
}
