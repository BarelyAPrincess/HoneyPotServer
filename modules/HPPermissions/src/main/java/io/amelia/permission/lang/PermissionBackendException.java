/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.permission.lang;

import io.amelia.lang.ApplicationException;
import io.amelia.lang.ExceptionContext;
import io.amelia.lang.ExceptionReport;
import io.amelia.lang.ReportingLevel;

/**
 * This exception is thrown when a permissions backend has issues loading
 */
public class PermissionBackendException extends ApplicationException.Runtime
{
	private static final long serialVersionUID = -133147199740089646L;

	public PermissionBackendException()
	{
		super( ReportingLevel.E_ERROR );
	}

	public PermissionBackendException( String message )
	{
		super( ReportingLevel.E_ERROR, message );
	}

	public PermissionBackendException( String message, Throwable cause )
	{
		super( ReportingLevel.E_ERROR, message, cause );
	}

	public PermissionBackendException( Throwable cause )
	{
		super( ReportingLevel.E_ERROR, cause );
	}

	@Override
	public ReportingLevel handle( ExceptionReport report, ExceptionContext context )
	{
		return null;
	}
}
