/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.permission.event;

import io.amelia.permission.PermissibleEntity;

public class PermissibleEntityEvent extends PermissibleEvent
{
	protected Action action;
	protected PermissibleEntity entity;

	public PermissibleEntityEvent( PermissibleEntity entity, Action action )
	{
		super( action.toString() );

		this.entity = entity;
		this.action = action;
	}

	public Action getAction()
	{
		return action;
	}

	public PermissibleEntity getEntity()
	{
		return entity;
	}

	public enum Action
	{
		PERMISSIONS_CHANGED,
		OPTIONS_CHANGED,
		INHERITANCE_CHANGED,
		INFO_CHANGED,
		TIMEDPERMISSION_EXPIRED,
		RANK_CHANGED,
		DEFAULTGROUP_CHANGED,
		WEIGHT_CHANGED,
		SAVED,
		REMOVED,
		TIMEDGROUP_EXPIRED,
	}
}
