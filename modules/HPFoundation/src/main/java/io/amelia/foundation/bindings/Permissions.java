/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <theameliadewitt@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation.binding;

public class Permissions
{
	public static final String NAMESPACE_PERMISSIONS = "io.amelia.permissions";

	public static PermissionBinding get()
	{
		return Bindings.getSystemNamespace().getFacadeBinding( NAMESPACE_PERMISSIONS, PermissionBinding.class );
	}


}
