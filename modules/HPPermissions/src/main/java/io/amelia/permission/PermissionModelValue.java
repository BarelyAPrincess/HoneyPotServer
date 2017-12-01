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

import io.amelia.permission.lang.PermissionValueException;
import io.amelia.support.Strs;

import java.util.HashSet;
import java.util.Set;

/**
 * References the model value held by a permission
 * Unset or default permissions will have no value and be permission type {@link PermissionType#DEFAULT}
 */
@SuppressWarnings( "unchecked" )
public class PermissionModelValue
{
	private final String name;
	private final Permission perm;
	private final PermissionType type;
	private PermissionValue value;
	private Object valueDefault;

	public PermissionModelValue( String name, PermissionType type, Permission perm )
	{
		this.name = name;
		this.type = type;
		this.perm = perm;
	}

	public PermissionValue createValue( Object value )
	{
		if ( value instanceof PermissionValue )
			value = ( ( PermissionValue ) value ).getValue();

		try
		{
			Object obj = type.cast( value );
			if ( obj == null && value == null )
				throw new PermissionValueException( "The assigned value must not be null." );
			if ( obj == null )
				throw new ClassCastException();
			return new PermissionValue( this, obj );
		}
		catch ( ClassCastException e )
		{
			throw new PermissionValueException( String.format( "Can't cast %s to type %s for permission %s.", value.getClass(), type, perm.getNamespace() ) );
		}
	}

	public PermissionValue getModelValue()
	{
		return value;
	}

	public PermissionType getType()
	{
		return type;
	}

	@Override
	public String toString()
	{
		return String.format( "PermissionModelValue{name=%s,type=%s,value=%s,default=%s}", name, type, value == null ? null : value.getValue(), valueDefault );
	}
}
