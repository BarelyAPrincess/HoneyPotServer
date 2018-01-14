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

import io.amelia.permission.lang.PermissionException;
import io.amelia.permission.lang.PermissionValueException;
import io.amelia.foundation.tasks.Timings;
import io.amelia.support.Objs;

/**
 * Holds the union between {@link Permission} and {@link PermissibleEntity}<br>
 * Also provides access to {@link #assign(References)} and {@link #assign(Object, References)}
 */
public class PermissionResult
{
	public static final PermissionResult DUMMY = new PermissionResult( AccountType.ACCOUNT_NONE.getPermissibleEntity(), PermissionDefault.DEFAULT.getNode() );
	private final PermissibleEntity entity;
	private final Permission perm;
	protected long epoch = Timings.epoch();
	private ChildPermission childPerm = null;
	private References refs;

	PermissionResult( PermissibleEntity entity, Permission perm )
	{
		this( entity, perm, References.format( "" ) );
	}

	PermissionResult( PermissibleEntity entity, Permission perm, References refs )
	{
		Objs.notNull( entity );
		Objs.notNull( perm );

		this.entity = entity;
		this.perm = perm;
		this.refs = refs;
		childPerm = entity.getChildPermissionRecursive( perm, refs );
	}

	public PermissionResult assign()
	{
		return assign( null );
	}

	public PermissionResult assign( References refs )
	{
		return assign( null, refs );
	}

	public PermissionResult assign( Object val, References refs )
	{
		if ( refs == null )
			refs = References.format();

		entity.addPermission( perm, val, refs );

		recalculatePermissions();
		return this;
	}

	/**
	 * See {@link Permission#commit()}<br>
	 * Caution: will commit changes made to other child values of the same permission node
	 *
	 * @return The {@link PermissionResult} for chaining
	 */
	public PermissionResult commit()
	{
		perm.commit();
		entity.save();
		return this;
	}

	public PermissibleEntity getEntity()
	{
		return entity;
	}

	public int getInt()
	{
		return Objs.castToInt( getValue().getValue() );
	}

	public Permission getPermission()
	{
		return perm;
	}

	public References getReference()
	{
		return refs;
	}

	public String getString()
	{
		return Objs.castToString( getValue().getValue() );
	}

	public PermissionValue getValue()
	{
		if ( childPerm == null || childPerm.getValue() == null || !isAssigned() )
			return perm.getModel().getModelValue();

		return childPerm.getValue();
	}

	/**
	 * Returns a final object based on assignment of permission.
	 *
	 * @return Unassigned will return the default value.
	 */
	@SuppressWarnings( "unchecked" )
	public <T> T getValueObject()
	{
		Object obj;

		if ( isAssigned() )
		{
			if ( childPerm == null || childPerm.getValue() == null )
				obj = perm.getModel().getModelValue();
			else
				obj = childPerm.getObject();
		}
		else
			obj = perm.getModel().getValueDefault();

		try
		{
			return ( T ) obj;
		}
		catch ( ClassCastException e )
		{
			throw new PermissionValueException( String.format( "Can't cast %s to type", obj.getClass().getName() ), e );
		}
	}

	public int getWeight()
	{
		return childPerm == null ? 9999 : childPerm.getWeight();
	}

	/**
	 * @return was this entity assigned an custom value for this permission.
	 */
	public boolean hasValue()
	{
		return perm.getType() != PermissionType.DEFAULT && childPerm != null && childPerm.getValue() != null;
	}

	/**
	 * @return was this permission assigned to our entity?
	 */
	public boolean isAssigned()
	{
		return childPerm != null;
	}

	/**
	 * @return was this permission assigned to our entity thru a group? Will return false if not assigned.
	 */
	public boolean isInherited()
	{
		if ( !isAssigned() )
			return false;

		return childPerm.isInherited();
	}

	/**
	 * A safe version of isTrueWithException() in case you don't care to know if the permission is of type Boolean or not
	 *
	 * @return is this permission true
	 */
	public boolean isTrue()
	{
		try
		{
			return isTrueWithException();
		}
		catch ( PermissionException e )
		{
			return false;
		}
	}

	/**
	 * A safe version of isTrueWithException() in case you don't care to know if the permission is of type Boolean or not
	 *
	 * @param allowOps Return true if the entity is a server operator
	 * @return is this permission true
	 */
	public boolean isTrue( boolean allowOps )
	{
		try
		{
			return isTrueWithException( allowOps );
		}
		catch ( PermissionException e )
		{
			return false;
		}
	}

	/**
	 * Used strictly for BOOLEAN permission nodes.
	 *
	 * @return is this permission true
	 * @throws IllegalAccessException Thrown if this permission node is not of type Boolean
	 */
	public boolean isTrueWithException() throws PermissionException
	{
		return isTrueWithException( true );
	}

	/**
	 * Used strictly for BOOLEAN permission nodes.
	 *
	 * @param allowOps Return true if the entity is a server operator
	 * @return is this permission true
	 * @throws IllegalAccessException Thrown if this permission node is not of type Boolean
	 */
	public boolean isTrueWithException( boolean allowOps ) throws PermissionException
	{
		// Can't check true on anything but these types
		if ( perm.getType() != PermissionType.BOOL && perm.getType() != PermissionType.DEFAULT )
			throw new PermissionValueException( String.format( "The permission %s is not of type boolean.", perm.getNamespace() ) );

		// We can check and allow OPs but ONLY if we are not checking a PermissionDefault node, for one 'sys.op' is the node we check for OPs.
		if ( allowOps && PermissionGuard.allowOps && !perm.getNamespace().equals( PermissionDefault.OP.getNamespace() ) && entity.isOp() )
			return ( boolean ) ( perm.getType() == PermissionType.BOOL ? perm.getModel().getValue() : true );

		if ( perm.getType() == PermissionType.DEFAULT )
			return isAssigned();

		return getValueObject() == null ? false : Objs.castToBoolean( getValueObject() );
	}

	public PermissionResult recalculatePermissions()
	{
		return recalculatePermissions( refs );
	}

	public PermissionResult recalculatePermissions( References refs )
	{
		this.refs = refs;
		childPerm = entity.getChildPermissionRecursive( perm, refs );

		// Loader.getLogger().debug( "Recalculating permission " + perm.getNamespace() + " for " + entity.getId() + " with result " + ( childPerm != null ) );

		return this;
	}

	@Override
	public String toString()
	{
		return String.format( "PermissionResult{name=%s,value=%s,isAssigned=%s}", perm.getNamespace(), getValueObject(), isAssigned() );
	}

	public PermissionResult unassign()
	{
		return assign( null );
	}

	public PermissionResult unassign( References refs )
	{
		if ( refs == null )
			refs = References.format();

		entity.removePermission( perm, refs );

		recalculatePermissions();
		return this;
	}
}
