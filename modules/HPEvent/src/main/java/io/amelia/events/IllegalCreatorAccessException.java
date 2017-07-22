/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.events;

/**
 * Thrown when a plugin attempts to interact with the server when it is not enabled
 */
@SuppressWarnings( "serial" )
public class IllegalCreatorAccessException extends RuntimeException
{
	
	/**
	 * Creates a new instance of <code>IllegalPluginAccessException</code> without detail message.
	 */
	public IllegalCreatorAccessException()
	{
	}
	
	/**
	 * Constructs an instance of <code>IllegalPluginAccessException</code> with the specified detail message.
	 * 
	 * @param msg
	 *            the detail message.
	 */
	public IllegalCreatorAccessException( String msg )
	{
		super( msg );
	}
}
