/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.lang;

public class SynchronizeException extends ApplicationException.Error
{
	public SynchronizeException( String message )
	{
		super( message );
	}

	public SynchronizeException( String message, Throwable cause )
	{
		super( message, cause );
	}

	public SynchronizeException( Throwable cause )
	{
		super( cause );
	}
}
