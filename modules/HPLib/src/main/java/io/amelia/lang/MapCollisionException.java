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
 * Thrown when two maps are joined and have conflicting keys
 */
public class MapCollisionException extends ApplicationException
{
	public MapCollisionException()
	{
		super( ReportingLevel.E_IGNORABLE );
	}

	@Override
	public ReportingLevel handle( ExceptionReport report, ExceptionContext context )
	{
		return null;
	}
}
