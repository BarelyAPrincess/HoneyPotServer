/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <theameliadewitt@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation.tasks;

import io.amelia.lang.ApplicationException;
import io.amelia.lang.ReportingLevel;

/**
 * Thrown when a creator attempts to interact with the server when it is not enabled
 */
@SuppressWarnings( "serial" )
public class IllegalTaskCreatorAccessException extends ApplicationException.Runtime
{
	/**
	 * Creates a new instance of <code>IllegalPluginAccessException</code> without detail message.
	 */
	public IllegalTaskCreatorAccessException()
	{
		super( ReportingLevel.E_STRICT );
	}

	/**
	 * Constructs an instance of <code>IllegalPluginAccessException</code> with the specified detail message.
	 *
	 * @param msg the detail message.
	 */
	public IllegalTaskCreatorAccessException( String msg )
	{
		super( ReportingLevel.E_STRICT, msg );
	}
}
