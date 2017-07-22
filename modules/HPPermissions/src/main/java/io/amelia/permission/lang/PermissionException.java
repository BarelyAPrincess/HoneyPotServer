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

import java.sql.SQLException;

public class PermissionException extends RuntimeException
{
	private static final long serialVersionUID = -7126640838300697969L;

	public PermissionException( SQLException e )
	{
		super( e );
	}

	public PermissionException( String message )
	{
		super( message );
	}
}
