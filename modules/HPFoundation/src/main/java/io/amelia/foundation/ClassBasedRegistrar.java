/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation;

public class ClassBasedRegistrar implements RegistrarBase
{
	protected Class<?> cls;

	public ClassBasedRegistrar( Class<?> cls )
	{
		this.cls = cls;
	}

	public String getName()
	{
		return cls == null ? null : cls.getSimpleName();
	}
}
