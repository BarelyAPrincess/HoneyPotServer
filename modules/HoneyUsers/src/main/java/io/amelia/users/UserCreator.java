/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.users;

import io.amelia.lang.UserException;
import io.amelia.lang.DescriptiveReason;

abstract class UserCreator
{
	private final String name;
	private boolean isDefault;

	public UserCreator( String name, boolean isDefault )
	{
		this.name = name;
		this.isDefault = isDefault;

		if ( isDefault )
			HoneyUsers.getCreators().forEach( backend -> backend.isDefault = false );
	}

	public abstract UserContext create( String uuid ) throws UserException.Error;

	public abstract boolean hasUser( String uuid );

	public boolean isDefault()
	{
		return isDefault;
	}

	public abstract boolean isEnabled();

	public final boolean isMemory()
	{
		return this instanceof UserCreatorMemory;
	}

	public abstract void load();

	public abstract void loginBegin( UserContext userContext, UserPermissible userPermissible, String acctId, Object... credentials );

	public abstract void loginFailed( UserContext userContext, DescriptiveReason result );

	public abstract void loginSuccess( UserContext userContext );

	public abstract void loginSuccessInit( UserContext userContext, PermissibleEntity permissibleEntity );

	public String name()
	{
		return name;
	}

	public abstract void reload( UserContext userContext ) throws UserException.Error;

	public abstract UserResult resolve( String uuid );

	/**
	 * Attempts to save the supplied {@link UserContext}.
	 *
	 * @param userContext the savable User
	 * @throws UserException.Error per implementation
	 */
	public abstract void save( UserContext userContext ) throws UserException.Error;

	public void setDefault()
	{
		HoneyUsers.getCreators().forEach( backend -> backend.isDefault = false );
		this.isDefault = true;
	}
}
