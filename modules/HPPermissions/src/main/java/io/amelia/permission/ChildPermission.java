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

import io.amelia.support.Objs;

public final class ChildPermission implements Comparable<ChildPermission>
{
	private final PermissibleEntity entity;
	private final Permission perm;
	private final PermissionValue value;
	private final int weight;

	/**
	 * References a permission state/value against an entity
	 *
	 * @param entity The PermissibleEntity
	 * @param perm   The permission this value ordains to
	 * @param value  The custom value assigned to this permission. Can be null to use default assigned value.
	 * @param weight The sorting weight of this ChildPermission
	 */
	public ChildPermission( PermissibleEntity entity, Permission perm, PermissionValue value, int weight )
	{
		Objs.notNull( entity );
		Objs.notNull( perm );
		Objs.notNull( value );

		this.entity = entity;
		this.perm = perm;
		this.value = value;
		this.weight = weight;
	}

	@Override
	public int compareTo( ChildPermission child )
	{
		if ( getWeight() == -1 && child.getWeight() == -1 )
			return 0;
		if ( getWeight() == -1 )
			return -1;
		if ( child.getWeight() == -1 )
			return 1;
		return getWeight() - child.getWeight();
	}

	public Boolean getBoolean()
	{
		if ( getType() == PermissionType.BOOL )
			return ( Boolean ) value.getValue();

		return null;
	}

	public Integer getInt()
	{
		if ( getType() == PermissionType.INT )
			return ( Integer ) value.getValue();

		return null;
	}

	public <T> T getObject()
	{
		return value.getValue();
	}

	public Permission getPermission()
	{
		return perm;
	}

	public References getReferences()
	{
		return entity.getPermissionReferences( perm );
	}

	public String getString()
	{
		if ( getType() == PermissionType.ENUM || getType() == PermissionType.VAR )
			return ( String ) value.getValue();

		return null;
	}

	public PermissionType getType()
	{
		return perm.getType();
	}

	public PermissionValue getValue()
	{
		return value;
	}

	public int getWeight()
	{
		return weight;
	}

	public boolean isInherited()
	{
		return weight >= 0;
	}

	@Override
	public String toString()
	{
		return String.format( "ChildPermission{entity=%s,node=%s,value=%s,weight=%s}", entity.getId(), perm.getNamespace(), value.getValue(), weight );
	}
}
