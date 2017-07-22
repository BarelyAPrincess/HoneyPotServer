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
import com.chiorichan.permission.PermissibleEntity;
import com.chiorichan.permission.PermissibleGroup;
import com.chiorichan.permission.Permission;
import com.chiorichan.permission.PermissionDispatcher;
import com.chiorichan.permission.PermissionValue;
import com.chiorichan.permission.References;
import com.chiorichan.tasks.Timings;
import com.chiorichan.terminal.commands.advanced.CommandHandler;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import io.amelia.lang.EnumColor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class EntityCommands extends PermissionBaseCommand
{
	@CommandHandler( name = "pex", syntax = "entities cleanup <group> [threshold]", permission = "permissions.manage.entities.cleanup", description = "Clean entities of specified group, which last login was before threshold (in days). By default threshold is 30 days." )
	public void entitiesCleanup( AccountAttachment sender, Map<String, String> args )
	{
		long threshold = 2304000;

		PermissibleGroup group = PermissionDispatcher.i().getGroup( args.get( "group" ) );

		if ( args.containsKey( "threshold" ) )
			try
			{
				threshold = Integer.parseInt( args.get( "threshold" ) ) * 86400; // 86400 - seconds in one day
			}
			catch ( NumberFormatException e )
			{
				sender.sendMessage( EnumColor.RED + "Threshold should be number (in days)" );
				return;
			}

		int removed = 0;

		Long deadline = System.currentTimeMillis() / 1000L - threshold;
		for ( PermissibleEntity entity : group.getChildEntities( true, References.format() ) )
		{
			// XXX Check last login from account, maybe make a last use option
			int lastLogin = entity.getOption( "last-login-time", null, 0 );

			if ( lastLogin > 0 && lastLogin < deadline )
			{
				entity.remove();
				removed++;
			}
		}

		sender.sendMessage( "Cleaned " + removed + " entities" );
	}

	@CommandHandler( name = "pex", syntax = "entities list", permission = "permissions.manage.entities", description = "List all registered entities" )
	public void entitiesList( AccountAttachment sender, Map<String, String> args )
	{
		Collection<PermissibleEntity> entities = PermissionDispatcher.i().getEntities();

		sender.sendMessage( EnumColor.WHITE + "Currently registered entities: " );
		for ( PermissibleEntity entity : entities )
			sender.sendMessage( " " + entity.getId() + " " + EnumColor.DARK_GREEN + "[" + Joiner.on( ", " ).join( entity.getGroupNames( References.format() ) ) + "]" );
	}

	@CommandHandler( name = "pex", syntax = "entity <entity> group add <group> [ref] [lifetime]", permission = "permissions.manage.membership.<group>", description = "Add <entity> to <group>" )
	public void entityAddGroup( AccountAttachment sender, Map<String, String> args )
	{
		String entityName = autoCompleteAccount( args.get( "entity" ) );
		String groupName = autoCompleteGroupName( args.get( "group" ) );
		References refs = autoCompleteRef( args.get( "ref" ) );

		PermissibleEntity entity = PermissionDispatcher.i().getEntity( entityName );

		if ( entity == null )
		{
			sender.sendMessage( EnumColor.RED + "User does not exist" );
			return;
		}

		if ( args.containsKey( "lifetime" ) )
			try
			{
				int lifetime = Timings.parseInterval( args.get( "lifetime" ) );
				entity.addTimedGroup( PermissionDispatcher.i().getGroup( groupName ), lifetime, refs );
			}
			catch ( NumberFormatException e )
			{
				sender.sendMessage( EnumColor.RED + "Group lifetime should be number!" );
				return;
			}
		else
			entity.addGroup( PermissionDispatcher.i().getGroup( groupName ), refs );


		sender.sendMessage( EnumColor.WHITE + "User added to group \"" + groupName + "\"!" );
		informEntity( entityName, "You are assigned to \"" + groupName + "\" group" );
	}

	@CommandHandler( name = "pex", syntax = "entity <entity> add <permission> [ref]", permission = "permissions.manage.entities.permissions.<entity>", description = "Add <permission> to <entity> in [ref]" )
	public void entityAddPermission( AccountAttachment sender, Map<String, String> args )
	{
		String entityName = autoCompleteAccount( args.get( "entity" ) );
		References refs = autoCompleteRef( args.get( "ref" ) );

		PermissibleEntity entity = PermissionDispatcher.i().getEntity( entityName );

		if ( entity == null )
		{
			sender.sendMessage( EnumColor.RED + "User does not exist" );
			return;
		}

		entity.addPermission( args.get( "io/amelia/permission" ), true, refs );

		sender.sendMessage( EnumColor.WHITE + "Permission \"" + args.get( "io/amelia/permission" ) + "\" added!" );

		informEntity( entityName, "Your permissions have been changed!" );
	}

	@CommandHandler( name = "pex", syntax = "entity <entity> timed add <permission> [lifetime] [ref]", permission = "permissions.manage.entities.permissions.timed.<entity>", description = "Add timed <permissions> to <entity> for [lifetime] seconds in [ref]" )
	public void entityAddTimedPermission( AccountAttachment sender, Map<String, String> args )
	{
		String entityName = autoCompleteAccount( args.get( "entity" ) );
		References refs = autoCompleteRef( args.get( "ref" ) );

		int lifetime = 0;

		if ( args.containsKey( "lifetime" ) )
			lifetime = Timings.parseInterval( args.get( "lifetime" ) );

		PermissibleEntity entity = PermissionDispatcher.i().getEntity( entityName );

		if ( entity == null )
		{
			sender.sendMessage( EnumColor.RED + "User does not exist" );
			return;
		}

		String permission = args.get( "io/amelia/permission" );

		entity.addTimedPermission( permission, true, refs, lifetime );

		sender.sendMessage( EnumColor.WHITE + "Timed permission \"" + permission + "\" added!" );
		informEntity( entityName, "Your permissions have been changed!" );

		PermissionDispatcher.getLogger().info( "User " + entityName + " get timed permission \"" + args.get( "io/amelia/permission" ) + "\" " + ( lifetime > 0 ? "for " + lifetime + " seconds " : " " ) + "from " + sender.getDisplayName() );
	}

	@CommandHandler( name = "pex", syntax = "entity <entity> check <permission> [ref]", permission = "permissions.manage.<entity>", description = "Checks meta for <permission>" )
	public void entityCheckPermission( AccountAttachment sender, Map<String, String> args )
	{
		String entityName = autoCompleteAccount( args.get( "entity" ) );
		References refs = autoCompleteRef( args.get( "ref" ) );

		PermissibleEntity entity = PermissionDispatcher.i().getEntity( entityName );

		if ( entity == null )
		{
			sender.sendMessage( EnumColor.RED + "User does not exist" );
			return;
		}

		refs = getSafeSite( refs, entityName );

		String permission = entity.getMatchingExpression( args.get( "io/amelia/permission" ), refs );

		if ( permission == null )
			sender.sendMessage( "Account \"" + entityName + "\" does not have such permission" );
		else
			sender.sendMessage( "Account \"" + entityName + "\" has \"" + permission + "\" = " + entity.explainExpression( permission ) );
	}

	@CommandHandler( name = "pex", syntax = "entity <entity> get <option> [ref]", permission = "permissions.manage.<entity>", description = "Toggle debug only for <entity>" )
	public void entityGetOption( AccountAttachment sender, Map<String, String> args )
	{
		String entityName = autoCompleteAccount( args.get( "entity" ) );
		References refs = autoCompleteRef( args.get( "ref" ) );

		PermissibleEntity entity = PermissionDispatcher.i().getEntity( entityName );

		if ( entity == null )
		{
			sender.sendMessage( EnumColor.RED + "User does not exist" );
			return;
		}

		refs = getSafeSite( refs, entityName );

		String value = entity.getOption( args.get( "option" ), refs, null );

		sender.sendMessage( "AccountMeta " + entityName + " @ " + refs + " option \"" + args.get( "option" ) + "\" = \"" + value + "\"" );
	}

	@CommandHandler( name = "pex", syntax = "entities", permission = "permissions.manage.entities", description = "List all registered entities (alias)", isPrimary = true )
	public void entityListAlias( AccountAttachment sender, Map<String, String> args )
	{
		entitiesList( sender, args );
	}

	/**
	 * User permission management
	 */
	@CommandHandler( name = "pex", syntax = "entity <entity>", permission = "permissions.manage.entities.permissions.<entity>", description = "List entity permissions (list alias)" )
	public void entityListAliasPermissions( AccountAttachment sender, Map<String, String> args )
	{
		String entityName = autoCompleteAccount( args.get( "entity" ) );
		References refs = autoCompleteRef( args.get( "ref" ) );

		PermissibleEntity entity = PermissionDispatcher.i().getEntity( entityName );

		if ( entity == null )
		{
			sender.sendMessage( EnumColor.RED + "User does not exist" );
			return;
		}

		sender.sendMessage( "'" + entityName + "' is a member of:" );
		printEntityInheritance( sender, entity.getGroups( refs ) );

		sender.sendMessage( "  @" + refs.join() + ":" );
		printEntityInheritance( sender, entity.getGroups( refs ) );

		sender.sendMessage( entityName + "'s permissions:" );

		sender.sendMessage( mapPermissions( refs, entity, 0 ) );

		sender.sendMessage( entityName + "'s options:" );
		for ( Entry<String, String> option : entity.getOptions( refs ).entrySet() )
			sender.sendMessage( "  " + option.getKey() + " = \"" + option.getValue() + "\"" );
	}

	@CommandHandler( name = "pex", syntax = "entity", permission = "permissions.manage.entities", description = "List all registered entities (alias)" )
	public void entityListAnotherAlias( AccountAttachment sender, Map<String, String> args )
	{
		entitiesList( sender, args );
	}

	/**
	 * User's groups management
	 */
	@CommandHandler( name = "pex", syntax = "entity <entity> group list [ref]", permission = "permissions.manage.membership.<entity>", description = "List all <entity> groups" )
	public void entityListGroup( AccountAttachment sender, Map<String, String> args )
	{
		String entityName = autoCompleteAccount( args.get( "entity" ) );
		References refs = autoCompleteRef( args.get( "ref" ) );

		PermissibleEntity entity = PermissionDispatcher.i().getEntity( entityName );

		if ( entity == null )
		{
			sender.sendMessage( EnumColor.RED + "User does not exist" );
			return;
		}

		sender.sendMessage( "User " + args.get( "entity" ) + " @" + refs + " currently in:" );
		for ( PermissibleGroup group : entity.getGroups( refs ) )
			sender.sendMessage( "  " + group.getId() );
	}

	@CommandHandler( name = "pex", syntax = "entity <entity> list [ref]", permission = "permissions.manage.entities.permissions.<entity>", description = "List entity permissions" )
	public void entityListPermissions( AccountAttachment sender, Map<String, String> args )
	{
		String entityName = autoCompleteAccount( args.get( "entity" ) );
		// References refs = autoCompleteRef( args.get( "ref" ) );

		PermissibleEntity entity = PermissionDispatcher.i().getEntity( entityName );
		if ( entity == null )
		{
			sender.sendMessage( EnumColor.RED + "Entity not found!" );
			return;
		}

		sender.sendMessage( entityName + "'s permissions:" );

		for ( Entry<Permission, PermissionValue> perm : entity.getPermissionValues( References.format() ) )
			sender.sendMessage( " '" + EnumColor.GREEN + perm.getKey() + EnumColor.WHITE + "' = " + EnumColor.BLUE + perm.getValue() );

	}

	@CommandHandler( name = "pex", syntax = "entity <entity> prefix [newprefix] [ref]", permission = "permissions.manage.entities.prefix.<entity>", description = "Get or set <entity> prefix" )
	public void entityPrefix( AccountAttachment sender, Map<String, String> args )
	{
		String entityName = autoCompleteAccount( args.get( "entity" ) );
		References refs = autoCompleteRef( args.get( "ref" ) );

		PermissibleEntity entity = PermissionDispatcher.i().getEntity( entityName );

		if ( entity == null )
		{
			sender.sendMessage( EnumColor.RED + "User does not exist" );
			return;
		}

		if ( args.containsKey( "newprefix" ) )
			entity.setPrefix( args.get( "newprefix" ), refs );

		sender.sendMessage( entity.getId() + "'s prefix = \"" + entity.getPrefix() + "\"" );
	}

	@CommandHandler( name = "pex", syntax = "entity <entity> group remove <group> [ref]", permission = "permissions.manage.membership.<group>", description = "Remove <entity> from <group>" )
	public void entityRemoveGroup( AccountAttachment sender, Map<String, String> args )
	{
		String entityName = autoCompleteAccount( args.get( "entity" ) );
		String groupName = autoCompleteGroupName( args.get( "group" ) );
		References refs = autoCompleteRef( args.get( "ref" ) );

		PermissibleEntity entity = PermissionDispatcher.i().getEntity( entityName );

		if ( entity == null )
		{
			sender.sendMessage( EnumColor.RED + "User does not exist" );
			return;
		}

		entity.removeGroup( groupName, refs );

		sender.sendMessage( EnumColor.WHITE + "User removed from group " + groupName + "!" );

		informEntity( entityName, "You were removed from \"" + groupName + "\" group" );
	}

	@CommandHandler( name = "pex", syntax = "entity <entity> remove <permission> [ref]", permission = "permissions.manage.entities.permissions.<entity>", description = "Remove permission from <entity> in [ref]" )
	public void entityRemovePermission( AccountAttachment sender, Map<String, String> args )
	{
		String entityName = autoCompleteAccount( args.get( "entity" ) );
		References refs = autoCompleteRef( args.get( "ref" ) );

		PermissibleEntity entity = PermissionDispatcher.i().getEntity( entityName );

		if ( entity == null )
		{
			sender.sendMessage( EnumColor.RED + "User does not exist" );
			return;
		}

		String permission = autoCompletePermission( entity, args.get( "io/amelia/permission" ), refs );

		entity.removePermission( permission, refs );
		entity.removeTimedPermission( permission, refs );

		sender.sendMessage( EnumColor.WHITE + "Permission \"" + permission + "\" removed!" );
		informEntity( entityName, "Your permissions have been changed!" );
	}

	@CommandHandler( name = "pex", syntax = "entity <entity> timed remove <permission> [ref]", permission = "permissions.manage.entities.permissions.timed.<entity>", description = "Remove timed <permission> from <entity> in [ref]" )
	public void entityRemoveTimedPermission( AccountAttachment sender, Map<String, String> args )
	{
		String entityName = autoCompleteAccount( args.get( "entity" ) );
		References refs = autoCompleteRef( args.get( "ref" ) );
		String permission = args.get( "io/amelia/permission" );

		PermissibleEntity entity = PermissionDispatcher.i().getEntity( entityName );

		if ( entity == null )
		{
			sender.sendMessage( EnumColor.RED + "User does not exist" );
			return;
		}

		entity.removeTimedPermission( args.get( "io/amelia/permission" ), refs );

		sender.sendMessage( EnumColor.WHITE + "Timed permission \"" + permission + "\" removed!" );
		informEntity( entityName, "Your permissions have been changed!" );
	}

	@CommandHandler( name = "pex", syntax = "entity <entity> group set <group> [ref]", permission = "", description = "Set <group> for <entity>" )
	public void entitySetGroup( AccountAttachment sender, Map<String, String> args )
	{
		PermissionDispatcher manager = PermissionDispatcher.i();

		PermissibleEntity entity = manager.getEntity( autoCompleteAccount( args.get( "entity" ) ) );
		References refs = autoCompleteRef( args.get( "ref" ) );

		if ( entity == null )
		{
			sender.sendMessage( EnumColor.RED + "User does not exist" );
			return;
		}

		String groupName = args.get( "group" );

		List<PermissibleGroup> groups = Lists.newArrayList();

		if ( groupName.contains( "," ) )
		{
			String[] groupsNames = groupName.split( "," );

			for ( int i = 0; i < groupsNames.length; i++ )
			{
				if ( !sender.getPermissible().hasLogin() || !sender.getPermissibleEntity().checkPermission( "permissions.manage.membership." + groupsNames[i].toLowerCase() ).isTrue() )
				{
					sender.sendMessage( EnumColor.RED + "Don't have enough permission for group " + groupsNames[i] );
					return;
				}

				groups.add( manager.getGroup( autoCompleteGroupName( groupsNames[i] ) ) );
			}

		}
		else
		{
			groupName = autoCompleteGroupName( groupName );

			if ( groupName != null )
			{
				if ( !sender.getPermissible().hasLogin() || !sender.getPermissibleEntity().checkPermission( "permissions.manage.membership." + groupName.toLowerCase() ).isTrue() )
				{
					sender.sendMessage( EnumColor.RED + "Don't have enough permission for group " + groupName );
					return;
				}

			}
			else
			{
				sender.sendMessage( EnumColor.RED + "No groups set!" );
				return;
			}
		}

		if ( groups.size() > 0 )
		{
			entity.setGroups( groups, refs );
			sender.sendMessage( EnumColor.WHITE + "User groups set!" );
		}
		else
			sender.sendMessage( EnumColor.RED + "No groups set!" );

		informEntity( entity.getId(), "You are now only in \"" + groupName + "\" group" );
	}

	@CommandHandler( name = "pex", syntax = "entity <entity> set <option> <value> [ref]", permission = "permissions.manage.entities.permissions.<entity>", description = "Set <option> to <value> in [ref]" )
	public void entitySetOption( AccountAttachment sender, Map<String, String> args )
	{
		String entityName = autoCompleteAccount( args.get( "entity" ) );
		References refs = autoCompleteRef( args.get( "ref" ) );

		PermissibleEntity entity = PermissionDispatcher.i().getEntity( entityName );

		if ( entity == null )
		{
			sender.sendMessage( EnumColor.RED + "User does not exist" );
			return;
		}

		entity.setOption( args.get( "option" ), args.get( "value" ), refs );


		if ( args.containsKey( "value" ) && args.get( "value" ).isEmpty() )
			sender.sendMessage( EnumColor.WHITE + "Option \"" + args.get( "option" ) + "\" cleared!" );
		else
			sender.sendMessage( EnumColor.WHITE + "Option \"" + args.get( "option" ) + "\" set!" );

		informEntity( entityName, "Your permissions have been changed!" );
	}

	@CommandHandler( name = "pex", syntax = "entity <entity> suffix [newsuffix] [ref]", permission = "permissions.manage.entities.suffix.<entity>", description = "Get or set <entity> suffix" )
	public void entitySuffix( AccountAttachment sender, Map<String, String> args )
	{
		String entityName = autoCompleteAccount( args.get( "entity" ) );
		References refs = autoCompleteRef( args.get( "ref" ) );

		PermissibleEntity entity = PermissionDispatcher.i().getEntity( entityName );

		if ( entity == null )
		{
			sender.sendMessage( EnumColor.RED + "User does not exist" );
			return;
		}

		if ( args.containsKey( "newsuffix" ) )
			entity.setSuffix( args.get( "newsuffix" ), refs );

		sender.sendMessage( entity.getId() + "'s suffix = \"" + entity.getSuffix() + "\"" );
	}

	@CommandHandler( name = "pex", syntax = "entity <entity> swap <permission> <targetPermission> [ref]", permission = "permissions.manage.entities.permissions.<entity>", description = "Swap <permission> and <targetPermission> in permission list. Could be number or permission itself" )
	public void entitySwapPermission( AccountAttachment sender, Map<String, String> args )
	{
		String entityName = autoCompleteAccount( args.get( "entity" ) );
		References refs = autoCompleteRef( args.get( "ref" ) );

		PermissibleEntity entity = PermissionDispatcher.i().getEntity( entityName );

		if ( entity == null )
		{
			sender.sendMessage( EnumColor.RED + "User does not exist" );
			return;
		}

		Permission[] permissions = entity.getPermissions( refs ).toArray( new Permission[0] );

		try
		{
			int sourceIndex = getPosition( autoCompletePermission( entity, args.get( "io/amelia/permission" ), refs, "io/amelia/permission" ), permissions );
			int targetIndex = getPosition( autoCompletePermission( entity, args.get( "targetPermission" ), refs, "targetPermission" ), permissions );

			Permission targetPermission = permissions[targetIndex];

			permissions[targetIndex] = permissions[sourceIndex];
			permissions[sourceIndex] = targetPermission;

			// entity.setPermissions( permissions, refs );

			sender.sendMessage( "Permissions swapped!" );
		}
		catch ( Throwable e )
		{
			sender.sendMessage( EnumColor.RED + "Error: " + e.getMessage() );
		}
	}

	@CommandHandler( name = "pex", syntax = "entity <entity> toggle debug", permission = "permissions.manage.<entity>", description = "Toggle debug only for <entity>" )
	public void entityToggleDebug( AccountAttachment sender, Map<String, String> args )
	{
		String entityName = autoCompleteAccount( args.get( "entity" ) );

		PermissibleEntity entity = PermissionDispatcher.i().getEntity( entityName );

		if ( entity == null )
		{
			sender.sendMessage( EnumColor.RED + "User does not exist" );
			return;
		}

		entity.setDebug( !entity.isDebug() );

		sender.sendMessage( "Debug mode for entity " + entityName + " " + ( entity.isDebug() ? "enabled" : "disabled" ) + "!" );
	}
}
