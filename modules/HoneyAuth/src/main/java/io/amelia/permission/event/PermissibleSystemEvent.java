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

public class PermissibleSystemEvent extends PermissibleEvent
{
	protected Action action;

	public PermissibleSystemEvent( Action action )
	{
		super( action.toString() );

		this.action = action;
	}

	public Action getAction()
	{
		return action;
	}

	public enum Action
	{
		BACKEND_CHANGED,
		RELOADED,
		WORLDINHERITANCE_CHANGED,
		DEFAULTGROUP_CHANGED,
		DEBUGMODE_TOGGLE,
		REINJECT_PERMISSIBLES,
	}
}
