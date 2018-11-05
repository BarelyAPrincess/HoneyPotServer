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

public class UserMeta implements User, Comparable<UserMeta>
{
	private final UserBackend backend;
	private final boolean keepLoaded;
	private boolean unloaded = false;
	private final String locationId;
	private final String userId;

	public UserMeta( UserBackend backend, String locationId, String userId, boolean keepLoaded )
	{
		this.backend = backend;
		this.locationId = locationId;
		this.userId = userId;
		this.keepLoaded = keepLoaded;
	}

	public UserBackend getBackend()
	{
		return backend;
	}

	@Override
	public int compareTo( UserMeta other )
	{
		return getUniqueId().compareToIgnoreCase( other.getUniqueId() );
	}

	public String getLocationId()
	{
		return locationId;
	}

	public String getUserId()
	{
		return userId;
	}

	public String getUniqueId()
	{
		return getLocationId() == null ? userId : getLocationId() + "_" + userId;
	}

	public boolean isAllowedToBeUnloaded()
	{
		return !keepLoaded;
	}

	/**
	 * Validate that the UserMeta is complete and ready for use
	 */
	public void validate() throws UserException.Error
	{
		if ( unloaded )
			throw new UserException.Error( this, "UserMeta has been unloaded!" );
	}

	public void unload()
	{
		if ( unloaded )
			return;
		Users.remove( this );
		unloaded = true;
	}
}
