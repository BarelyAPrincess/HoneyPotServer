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

public class PermissionValueException extends PermissionException
{
	private static final long serialVersionUID = -4762649378128218189L;

	public PermissionValueException( String message )
	{
		super( message );
	}

	public PermissionValueException( String message, Object... objs )
	{
		super( String.format( message, objs ) );
	}
}
