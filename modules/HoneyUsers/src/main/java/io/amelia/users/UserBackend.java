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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import io.amelia.foundation.ConfigRegistry;
import io.amelia.lang.ConfigException;

public abstract class UserBackend
{
	public static final UserBackend MEMORY = new UserBackendMemory();

	public static final UserMeta USER_NULL = new UserMeta( MEMORY, "", "null", true );
	public static final UserMeta USER_ROOT = new UserMeta( MEMORY, "", "root", true );

	private static volatile List<UserBackend> backends = new CopyOnWriteArrayList<>();

	public static boolean isNullUser( User user )
	{
		return USER_NULL.getUserId().equalsIgnoreCase( user.getUserId() );
	}

	public static boolean isRootUser( User user )
	{
		return USER_ROOT.getUserId().equalsIgnoreCase( user.getUserId() );
	}

	public static Stream<UserBackend> getBackends()
	{
		return backends.stream();
	}

	public static Optional<UserBackend> getBackend( String name )
	{
		return getBackends().filter( backend -> name.equalsIgnoreCase( backend.name() ) ).findAny();
	}

	public static UserBackend getDefaultBackend()
	{
		return getBackends().filter( UserBackend::isDefault ).filter( UserBackend::isEnabled ).findAny().orElse( MEMORY );
	}

	private final String name;

	public UserBackend( String name )
	{
		this.name = name;
	}

	public String name()
	{
		return name;
	}

	public boolean isBuiltin()
	{
		return this instanceof UserBackendMemory || this instanceof UserBackendFileSystem;
	}

	public abstract boolean isEnabled();

	public boolean isDefault()
	{
		return name.equals( ConfigRegistry.config.getValue( Users.Config.USERS_BACKEND_DEFAULT ) );
	}

	public void setDefault() throws ConfigException.Error
	{
		ConfigRegistry.config.setValue( Users.Config.USERS_BACKEND_DEFAULT, name() );
	}

	public static class UserBackendFileSystem extends UserBackend
	{
		public UserBackendFileSystem( String name )
		{
			super( name );
		}
	}

	public static class UserBackendMemory extends UserBackend
	{
		public UserBackendMemory()
		{
			super( "memory" );
		}

		@Override
		public boolean isEnabled()
		{
			return true;
		}
	}
}
