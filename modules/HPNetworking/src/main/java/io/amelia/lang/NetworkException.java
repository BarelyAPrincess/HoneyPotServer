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

import com.sun.istack.internal.NotNull;

public class NetworkException extends ApplicationException
{
	private static final long serialVersionUID = 5522301956671473324L;

	public NetworkException( @NotNull String message )
	{
		super( ReportingLevel.E_ERROR, message );
	}

	public NetworkException( @NotNull String message, @NotNull Throwable cause )
	{
		super( ReportingLevel.E_ERROR, message, cause );
	}

	public NetworkException( @NotNull Throwable cause )
	{
		super( ReportingLevel.E_ERROR, cause );
	}

	@Override
	public ReportingLevel handle( ExceptionReport report, ExceptionContext context )
	{
		return null;
	}
}
