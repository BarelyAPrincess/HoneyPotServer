/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.permission;

/**
 * References child values of assigned permissions
 */
public class PermissionValue
{
	private final PermissionModelValue model;
	private final Object value;

	public PermissionValue( final PermissionModelValue model, final Object value )
	{
		this.model = model;
		this.value = value;
	}

	public PermissionModelValue getModelValue()
	{
		return model;
	}

	@SuppressWarnings( "unchecked" )
	public <T> T getValue()
	{
		return ( T ) value;
	}

	@Override
	public String toString()
	{
		return String.format( "PermissionValue{model=%s,value=%s}", model, value );
	}
}
