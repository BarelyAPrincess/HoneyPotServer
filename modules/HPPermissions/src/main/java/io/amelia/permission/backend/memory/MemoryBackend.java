/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.permission.backend.memory;

import io.amelia.permission.PermissibleEntity;
import io.amelia.permission.PermissibleGroup;
import io.amelia.permission.Permission;
import io.amelia.permission.PermissionBackend;
import io.amelia.permission.PermissionDispatcher;
import io.amelia.permission.References;
import io.amelia.permission.lang.PermissionBackendException;

import java.util.ArrayList;
import java.util.Collection;

/*
 * Memory Backend
 * Zero Persistence. Does not attempt to save any changes.
 */
public class MemoryBackend extends PermissionBackend
{
	private static MemoryBackend backend;

	public static MemoryBackend getBackend()
	{
		return backend;
	}

	public MemoryBackend()
	{
		super();
		backend = this;
	}

	@Override
	public void commit()
	{
		// Nothing to do here!
	}

	@Override
	public PermissibleGroup getDefaultGroup( References refs )
	{
		return PermissionDispatcher.getGroup( "Default" );
	}

	@Override
	public PermissibleEntity getEntity( String name )
	{
		return new MemoryEntity( name );
	}

	@Override
	public Collection<String> getEntityNames()
	{
		return new ArrayList();
	}

	@Override
	public Collection<String> getEntityNames( int type )
	{
		return new ArrayList();
	}

	@Override
	public PermissibleGroup getGroup( String name )
	{
		return new MemoryGroup( name );
	}

	@Override
	public Collection<String> getGroupNames()
	{
		return new ArrayList();
	}

	@Override
	public void initialize() throws PermissionBackendException
	{
		// Nothing to do here!
	}

	@Override
	public void loadEntities()
	{
		// Nothing to do here!
	}

	@Override
	public void loadGroups()
	{
		// Nothing to do here!
	}

	@Override
	public void loadPermissions()
	{
		// Nothing to do here!
	}

	@Override
	public void reloadBackend() throws PermissionBackendException
	{
		// Nothing to do here!
	}

	@Override
	public void setDefaultGroup( String child, References refs )
	{
		// Nothing to do here!
	}

	@Override
	public void nodeCommit( Permission perm )
	{
		PermissionDispatcher.L.fine( "MemoryPermission nodes can not be saved. Sorry for the inconvenience. Might you consider changing permission backend. :(" );
	}

	@Override
	public void nodeDestroy( Permission perm )
	{
		// Nothing to do here!
	}

	@Override
	public void nodeReload( Permission perm )
	{
		// Nothing to do here!
	}
}
