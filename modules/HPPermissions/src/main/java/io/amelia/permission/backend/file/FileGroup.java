/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.permission.backend.file;

import com.chiorichan.configuration.ConfigurationSection;
import com.chiorichan.permission.ChildPermission;
import com.chiorichan.permission.PermissibleGroup;
import com.chiorichan.permission.Permission;
import com.chiorichan.permission.PermissionDispatcher;
import com.chiorichan.permission.PermissionNamespace;
import com.chiorichan.permission.PermissionType;
import com.chiorichan.permission.PermissionValue;
import com.chiorichan.permission.References;
import io.amelia.lang.EnumColor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;

public class FileGroup extends PermissibleGroup
{
	public FileGroup( String groupName )
	{
		super( groupName );
	}

	@Override
	public void reloadGroups()
	{
		if ( isDebug() )
			PermissionDispatcher.getLogger().info( EnumColor.YELLOW + "Groups being loaded for group " + getId() );

		clearGroups();
		clearTimedGroups();

		ConfigurationSection groups = FileBackend.getBackend().permissions.getConfigurationSection( "entities." + getId() + ".groups" );
		if ( groups != null )
			for ( String key : groups.getKeys( false ) )
				addGroup0( PermissionDispatcher.i().getGroup( key ), References.format( groups.getString( key ) ) );
	}

	@Override
	public void reloadPermissions()
	{
		if ( isDebug() )
			PermissionDispatcher.getLogger().info( EnumColor.YELLOW + "Permissions being loaded for entity " + getId() );

		ConfigurationSection permissions = FileBackend.getBackend().permissions.getConfigurationSection( "entities." + getId() + ".permissions" );
		clearPermissions();
		clearTimedPermissions();

		if ( permissions != null )
			for ( String ss : permissions.getKeys( false ) )
			{
				ConfigurationSection permission = permissions.getConfigurationSection( ss );
				PermissionNamespace ns = PermissionNamespace.parseString( ss.replaceAll( "/", "." ) );

				if ( !ns.containsOnlyValidChars() )
				{
					PermissionDispatcher.getLogger().warning( "We failed to add the permission %s to entity %s because it contained invalid characters, namespaces can only contain 0-9, a-z and _." );
					continue;
				}

				Collection<Permission> perms = ns.containsRegex() ? PermissionDispatcher.i().getNodes( ns ) : Arrays.asList( ns.createPermission() );

				for ( Permission perm : perms )
				{
					PermissionValue value = null;
					if ( permission.getString( "value" ) != null )
						value = perm.getModel().createValue( permission.getString( "value" ) );

					addPermission( new ChildPermission( this, perm, value, getWeight() ), References.format( permission.getString( "refs" ) ) );
				}
			}
	}

	@Override
	public void remove()
	{
		FileBackend.getBackend().permissions.getConfigurationSection( "groups", true ).set( getId(), null );
	}

	@Override
	public void save()
	{
		if ( isVirtual() )
			return;

		if ( isDebug() )
			PermissionDispatcher.getLogger().info( EnumColor.YELLOW + "Group " + getId() + " being saved to backend" );

		ConfigurationSection root = FileBackend.getBackend().permissions.getConfigurationSection( "entities." + getId(), true );

		Collection<ChildPermission> children = getChildPermissions( null );
		for ( ChildPermission child : children )
		{
			Permission perm = child.getPermission();
			ConfigurationSection sub = root.getConfigurationSection( "permissions." + perm.getNamespace().replaceAll( "\\.", "/" ), true );
			if ( perm.getType() != PermissionType.DEFAULT )
				sub.set( "value", child.getObject() );

			sub.set( "refs", child.getReferences().isEmpty() ? null : child.getReferences().join() );
		}

		Collection<Entry<PermissibleGroup, References>> groups = getGroupEntrys( null );
		for ( Entry<PermissibleGroup, References> entry : groups )
			root.set( "groups." + entry.getKey().getId(), entry.getValue().join() );
	}
}
