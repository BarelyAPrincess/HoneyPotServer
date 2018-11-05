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

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.amelia.data.TypeBase;
import io.amelia.foundation.ConfigRegistry;
import io.amelia.lang.UserResult;
import io.amelia.lang.UserException;
import io.amelia.support.Encrypt;
import io.amelia.support.Strs;

public class Users
{
	private static volatile Set<UserMeta> users = new CopyOnWriteArraySet<>();

	static void remove( UserMeta userMeta )
	{
		users.remove( userMeta );
	}

	static void unload()
	{
		getUsers().filter( UserMeta::isAllowedToBeUnloaded ).forEach( UserMeta::unload );
	}

	static void remove( @Nonnull String userId )
	{
		remove( userId, "" );
	}

	static void remove( @Nonnull String userId, @Nonnull String locationId )
	{
		users.stream().filter( user -> userId.equals( user.getUserId() ) && ( locationId.equals( user.getLocationId() ) || "%".equals( locationId ) ) ).forEach( users::remove );
	}

	static void put( UserMeta userMeta ) throws UserException.Error
	{
		if ( Strs.matchesIgnoreCase( userMeta.getUniqueId(), "none", "default", "root" ) )
			return;
		if ( users.stream().anyMatch( user -> user.compareTo( userMeta ) == 0 ) )
			return;
		userMeta.validate();
		users.add( userMeta );
	}

	public static boolean userExists( @Nonnull String userId )
	{
		return userExists( userId, "" );
	}

	public static boolean userExists( @Nonnull String userId, @Nonnull String locationId )
	{
		return getUsers().anyMatch( user -> userId.equals( user.getUserId() ) && ( locationId.equals( user.getLocationId() ) || "%".equals( locationId ) ) );
	}

	public static String generateUniqueUserId()
	{
		String userId;

		do
			userId = Encrypt.rand( 8, true, true );
		while ( userExists( userId, "%" ) );

		return userId;
	}

	public static UserMeta createUser( @Nonnull String userId )
	{
		return createUser( userId, "" );
	}

	public static UserMeta createUser( @Nonnull String userId, @Nonnull String locationId )
	{
		return createUser( userId, locationId, UserBackend.getDefaultBackend() );
	}

	public static UserMeta createUser( @Nonnull String userId, @Nonnull String locationId, @Nonnull UserBackend userBackend )
	{
		if ( !userBackend.isEnabled() )
			throw new UserException.Error( UserResult.FEATURE_DISABLED, locationId, userId );
		return new UserMeta( type.getCreator().createUser( userId, locationId ) );
	}

	public static Stream<UserMeta> getUsers()
	{
		return users.stream();
	}

	public static boolean isDebugEnabled()
	{
		return ConfigRegistry.config.getValue( Config.USERS_DEBUG_ENABLED );
	}

	private Users()
	{
		// Static Access
	}

	public static class Config
	{
		public final static TypeBase USERS_BASE = new TypeBase( "users" );
		public final static TypeBase.TypeBoolean USERS_DEBUG_ENABLED = new TypeBase.TypeBoolean( USERS_BASE, "debugEnabled", false );

		public final static TypeBase USERS_BACKEND_BASE = new TypeBase( USERS_BASE, "backend" );
		public final static TypeBase.TypeString USERS_BACKEND_DEFAULT = new TypeBase.TypeString( USERS_BACKEND_BASE, "default", "local" );
	}
}
