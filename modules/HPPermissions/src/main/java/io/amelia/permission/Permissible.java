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

import io.amelia.permission.lang.PermissionDeniedException;

public abstract class Permissible
{
	/**
	 * Used to reference the PermissibleEntity for the Permissible object.
	 */
	private PermissibleEntity entity = null;

	private boolean checkEntity()
	{
		if ( AccountType.isNoneAccount( entity ) )
		{
			String id = getId();
			if ( UtilObjects.isEmpty( id ) )
				throw new IllegalStateException( "getId() must return a valid (non-empty) entity Id." );
			entity = PermissionDispatcher.i().getEntity( id );
		}

		if ( entity == null )
			entity = AccountType.ACCOUNT_NONE.getPermissibleEntity();

		return !AccountType.isNoneAccount( entity );
	}

	public final PermissionResult checkPermission( String perm )
	{
		perm = PermissionDispatcher.parseNode( perm );
		return checkPermission( PermissionDispatcher.i().createNode( perm ) );
	}

	public final PermissionResult checkPermission( String perm, References refs )
	{
		perm = PermissionDispatcher.parseNode( perm );
		return checkPermission( PermissionDispatcher.i().createNode( perm ), refs );
	}

	public final PermissionResult checkPermission( Permission perm, References refs )
	{
		PermissibleEntity entity = getPermissibleEntity();
		return entity.checkPermission( perm, refs );
	}

	public final PermissionResult checkPermission( String perm, String... refs )
	{
		return checkPermission( perm, References.format( refs ) );
	}

	public final PermissionResult checkPermission( Permission perm, String... refs )
	{
		return checkPermission( perm, References.format( refs ) );
	}

	public final PermissionResult checkPermission( Permission perm )
	{
		return checkPermission( perm, References.format( "" ) );
	}

	public final void destroyEntity()
	{
		entity = AccountType.ACCOUNT_NONE.getPermissibleEntity();
	}

	/**
	 * Get the unique identifier for this Permissible
	 *
	 * @return String a unique identifier
	 */
	public abstract String getId();

	public final PermissibleEntity getPermissibleEntity()
	{
		checkEntity();
		return entity;
	}

	public final boolean isAdmin()
	{
		if ( !checkEntity() )
			return false;

		return entity.isAdmin();
	}

	public final boolean isBanned()
	{
		if ( !checkEntity() )
			return false;

		return entity.isBanned();
	}

	/**
	 * Is this permissible on the OP list.
	 *
	 * @return true if OP
	 */
	public final boolean isOp()
	{
		if ( !checkEntity() )
			return false;

		return entity.isOp();
	}

	public final boolean isWhitelisted()
	{
		if ( !checkEntity() )
			return false;

		return entity.isWhitelisted();
	}

	/**
	 * -1, everybody, everyone = Allow All!
	 * 0, op, root | sys.op = OP Only!
	 * admin | sys.admin = Admin Only!
	 */
	public final PermissionResult requirePermission( String req, References refs ) throws PermissionDeniedException
	{
		req = PermissionDispatcher.parseNode( req );
		return requirePermission( PermissionDispatcher.i().createNode( req ), refs );
	}

	public final PermissionResult requirePermission( String req, String... refs ) throws PermissionDeniedException
	{
		req = PermissionDispatcher.parseNode( req );
		return requirePermission( PermissionDispatcher.i().createNode( req ), References.format( refs ) );
	}

	public final PermissionResult requirePermission( Permission req, String... refs ) throws PermissionDeniedException
	{
		return requirePermission( req, References.format( refs ) );
	}

	public final PermissionResult requirePermission( Permission req, References refs ) throws PermissionDeniedException
	{
		PermissionResult result = checkPermission( req );

		if ( result.getPermission() != PermissionDefault.EVERYBODY.getNode() )
		{
			if ( result.getEntity() == null || AccountType.isNoneAccount( result.getEntity() ) )
				throw new PermissionDeniedException( PermissionDeniedException.PermissionDeniedReason.LOGIN_PAGE );

			if ( !result.isTrue() )
			{
				if ( result.getPermission() == PermissionDefault.OP.getNode() )
					throw new PermissionDeniedException( PermissionDeniedException.PermissionDeniedReason.OP_ONLY );

				result.recalculatePermissions( refs );
				if ( result.isTrue() )
					return result;

				throw new PermissionDeniedException( PermissionDeniedException.PermissionDeniedReason.DENIED.setPermission( req ) );
			}
		}

		return result;
	}
}
