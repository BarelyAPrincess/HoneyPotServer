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

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;

import io.amelia.data.ContainerListener;
import io.amelia.data.parcel.Parcel;
import io.amelia.users.auth.UserCredentials;

/**
 * Provides the starting point for all users and synchronizes them with their specified backend.
 * We aim for as memory usage and references to be kept at a minimum.
 *
 * UserBackend (The Backend) -> UserContext (The User Details) -> UserMeta (The User Processed) -> UserInstance (The User Logged In and can have multiple instances)
 */
public class UserContext
{
	public static final List<String> IGNORED_KEYS = Arrays.asList( "userid", "locationid" );

	private UserCredentials lastUsedCredentials = null;
	private WeakReference<UserMeta> userMeta;

	private final Parcel parcel = Parcel.empty();
	private final UserBackend backend;
	private final String locationId;
	private final String userId;

	public UserContext( UserBackend backend, String locationId, String userId )
	{
		this.backend = backend;
		this.locationId = locationId;
		this.userId = userId;

		parcel.addChildAddBeforeListener( parcel -> {
			if ( IGNORED_KEYS.contains( parcel.getName().toLowerCase() ) )
				throw new IllegalArgumentException( parcel.getName() + " is a disallowed key and should be set directly on the UserContext." );
		}, ContainerListener.Flags.SYNCHRONIZED );
	}

	public void setLastUsedCredentials( UserCredentials lastUsedCredentials )
	{
		this.lastUsedCredentials = lastUsedCredentials;
	}

	public UserCredentials getLastUsedCredentials()
	{
		return lastUsedCredentials;
	}

	public String getUserId()
	{
		return userId;
	}

	public String getLocationId()
	{
		return locationId;
	}

	public UserBackend getBackend()
	{
		return backend;
	}

	public Parcel getParcel()
	{
		return parcel;
	}

	public UserMeta getUserMeta()
	{
		if ( userMeta == null || userMeta.get() == null )
			userMeta = new WeakReference<>( new UserMeta( this ) );
		return userMeta.get();
	}

	public void save()
	{
		backend.save( this );
	}
}
