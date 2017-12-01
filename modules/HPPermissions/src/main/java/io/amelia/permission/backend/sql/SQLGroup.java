/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.permission.backend.sql;

import com.chiorichan.permission.ChildPermission;
import com.chiorichan.permission.PermissibleGroup;
import com.chiorichan.permission.Permission;
import com.chiorichan.permission.PermissionDispatcher;
import com.chiorichan.permission.PermissionNamespace;
import com.chiorichan.permission.PermissionValue;
import com.chiorichan.permission.References;
import io.amelia.lang.DatabaseException;
import io.amelia.lang.EnumColor;
import io.amelia.storage.Database;
import io.amelia.storage.elegant.ElegantQuerySelect;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;

public class SQLGroup extends PermissibleGroup
{
	public SQLGroup( String id )
	{
		super( id );
	}

	@Override
	public void reloadGroups()
	{
		Database db = SQLBackend.getBackend().getSQL();

		clearGroups();
		try
		{
			ElegantQuerySelect rs = db.table( "permissions_groups" ).select().where( "parent" ).matches( getId() ).and().where( "type" ).matches( "1" ).executeWithException();
			// ResultSet rs = db.query( "SELECT * FROM `permissions_groups` WHERE `parent` = '" + getId() + "' AND `type` = '1';" );

			if ( rs.count() > 0 )
				do
				{
					PermissibleGroup grp = PermissionDispatcher.i().getGroup( rs.getString( "child" ) );
					addGroup( grp, References.format( rs.getString( "refs" ) ) );
				}
				while ( rs.next() );

			if ( rs != null )
				rs.close();
		}
		catch ( DatabaseException e )
		{
			throw new RuntimeException( e );
		}
	}

	@Override
	public void reloadPermissions()
	{
		Database db = SQLBackend.getBackend().getSQL();

		clearPermissions();
		clearTimedPermissions();
		try
		{
			ElegantQuerySelect rs = db.table( "permissions_entity" ).select().where( "owner" ).matches( getId() ).and().where( "type" ).matches( "1" ).executeWithException();
			// ResultSet rs = db.query( "SELECT * FROM `permissions_entity` WHERE `owner` = ? AND `type` = '1';", getId() );

			if ( rs.count() > 0 )
				do
				{
					PermissionNamespace ns = PermissionNamespace.parseString( rs.getString( "io/amelia/permission" ) );

					if ( !ns.containsOnlyValidChars() )
					{
						PermissionDispatcher.getLogger().warning( "We failed to add the permission %s to entity %s because it contained invalid characters, namespaces can only contain 0-9, a-z and _." );
						continue;
					}

					Collection<Permission> perms = ns.containsRegex() ? PermissionDispatcher.i().getNodes( ns ) : Arrays.asList( ns.createPermission() );

					for ( Permission perm : perms )
					{
						PermissionValue value = null;
						if ( rs.getString( "value" ) != null )
							value = perm.getModel().createValue( rs.getString( "value" ) );

						addPermission( new ChildPermission( this, perm, value, getWeight() ), References.format( rs.getString( "refs" ) ) );
					}
				}
				while ( rs.next() );

			if ( rs != null )
				rs.close();
		}
		catch ( DatabaseException e )
		{
			throw new RuntimeException( e );
		}
	}

	@Override
	public void remove()
	{
		Database db = SQLBackend.getBackend().getSQL();
		try
		{
			// db.queryUpdate( String.format( "DELETE FROM `permissions_entity` WHERE `owner` = '%s' AND `type` = '1';", getId() ) );
			// db.queryUpdate( String.format( "DELETE FROM `permissions_groups` WHERE `parent` = '%s' AND `type` = '1';", getId() ) );

			db.table( "permissions_entity" ).delete().where( "owner" ).matches( getId() ).and().where( "type" ).matches( "1" ).executeWithException();
			db.table( "permissions_groups" ).delete().where( "parent" ).matches( getId() ).and().where( "type" ).matches( "1" ).executeWithException();
		}
		catch ( DatabaseException e )
		{
			throw new RuntimeException( e );
		}
	}

	@Override
	public void save()
	{
		if ( isVirtual() )
			return;

		if ( isDebug() )
			PermissionDispatcher.getLogger().info( EnumColor.YELLOW + "Group " + getId() + " saved to backend" );

		try
		{
			Database db = SQLBackend.getBackend().getSQL();
			remove();

			Collection<ChildPermission> children = getChildPermissions( null );
			for ( ChildPermission child : children )
			{
				Permission perm = child.getPermission();
				// db.queryUpdate( String.format( "INSERT INTO `permissions_entity` (`owner`,`type`,`refs`,`permission`,`value`) VALUES ('%s','1','%s','%s','%s');", getId(), child.getReferences().join(), perm.getNamespace(),
				// child.getObject() ) );
				db.table( "permissions_entity" ).insert().value( "owner", getId() ).value( "type", 1 ).value( "refs", child.getReferences().join() ).value( "io/amelia/permission", perm.getNamespace() ).value( "value", child.getObject() ).executeWithException();
			}

			Collection<Entry<PermissibleGroup, References>> groups = getGroupEntrys( null );
			for ( Entry<PermissibleGroup, References> entry : groups )
				db.table( "permissions_groups" ).insert().value( "child", entry.getKey().getId() ).value( "parent", getId() ).value( "type", 1 ).value( "refs", entry.getValue().join() ).executeWithException();
			// db.queryUpdate( String.format( "INSERT INTO `permissions_groups` (`child`, `parent`, `type`, `refs`) VALUES ('%s', '%s', '1', '%s');", entry.getKey().getId(), getId(), entry.getValue().join() ) );
		}
		catch ( DatabaseException e )
		{
			throw new RuntimeException( e );
		}
	}
}
