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

public class NetworkException extends ApplicationException.Error
{
	private static final long serialVersionUID = 5522301956671473324L;

	public NetworkException( String message )
	{
		super( message );
	}

	public NetworkException( String message, Throwable cause )
	{
		super( message, cause );
	}

	public NetworkException( Throwable cause )
	{
		super( cause );
	}
}
