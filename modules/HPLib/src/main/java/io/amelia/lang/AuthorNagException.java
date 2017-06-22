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

@SuppressWarnings( "serial" )
public class AuthorNagException extends RuntimeException
{
	private final String message;
	
	/**
	 * Constructs a new AuthorNagException based on the given Exception
	 * 
	 * @param message
	 *            Brief message explaining the cause of the exception
	 */
	public AuthorNagException( final String message )
	{
		this.message = message;
	}
	
	@Override
	public String getMessage()
	{
		return message;
	}
}
