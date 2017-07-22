/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.permission.commands;

import com.chiorichan.account.AccountAttachment;
import com.chiorichan.permission.Permission;
import com.chiorichan.permission.PermissionDispatcher;
import com.chiorichan.permission.PermissionNamespace;
import com.chiorichan.permission.PermissionType;
import com.chiorichan.terminal.commands.advanced.CommandHandler;
import io.amelia.lang.EnumColor;

import java.util.Collection;
import java.util.Map;

/**
 * Manages permissions
 */
public class PermissionCommands extends PermissionBaseCommand
{
	@CommandHandler( name = "pex", syntax = "perm create <node> [type]", permission = "permissions.manage.permissions", description = "Create permission" )
	public void permsCreate( AccountAttachment sender, Map<String, String> args )
	{
		if ( !args.containsKey( "node" ) || args.get( "node" ).isEmpty() )
		{
			sender.sendMessage( EnumColor.RED + "You must specify a permission node!" );
			return;
		}

		PermissionType type = args.containsKey( "type" ) && !args.get( "type" ).isEmpty() ? PermissionType.valueOf( args.get( "type" ) ) : PermissionType.DEFAULT;

		if ( type == null )
		{
			sender.sendMessage( EnumColor.RED + "We could not find a permission type that matches '" + args.get( "type" ) + "'!" );
			return;
		}

		PermissionNamespace ns = PermissionNamespace.parseString( args.get( "node" ) );

		ns.createPermission( type );

		sender.sendMessage( EnumColor.AQUA + "Good news everybody, we successfully created permission node '" + ns.getString() + "' with type '" + type.name() + "'!" );
	}

	@CommandHandler( name = "pex", syntax = "perm list [parent]", permission = "permissions.manage.permissions", description = "List all permissions" )
	public void permsList( AccountAttachment sender, Map<String, String> args )
	{
		if ( args.containsKey( "parent" ) )
		{
			Permission root = PermissionDispatcher.i().getNode( args.get( "parent" ) );
			if ( root == null )
				sender.sendMessage( EnumColor.RED + "There was no such permission '" + args.get( "parent" ) + "'!" );
			else
			{
				sender.sendMessage( EnumColor.WHITE + "Permissions stack dump for '" + args.get( "parent" ) + "':" );
				root.debugPermissionStack( 0 );
			}
		}
		else
		{
			Collection<Permission> perms = PermissionDispatcher.i().getRootNodes();

			sender.sendMessage( EnumColor.WHITE + "Permissions stack dump:" );
			for ( Permission root : perms )
				root.debugPermissionStack( 0 );
		}
	}
}
