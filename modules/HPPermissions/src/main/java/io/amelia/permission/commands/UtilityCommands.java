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

import io.amelia.config.ConfigRegistry;
import io.amelia.lang.EnumColor;
import io.amelia.permission.PermissibleEntity;
import io.amelia.permission.PermissibleGroup;
import io.amelia.permission.PermissionBackend;
import io.amelia.permission.PermissionDispatcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class UtilityCommands extends PermissionBaseCommand
{
	private static int tryGetInt( TerminalEntity sender, Map<String, String> args, String key, int def )
	{
		if ( !args.containsKey( key ) )
			return def;

		try
		{
			return Integer.parseInt( args.get( key ) );
		}
		catch ( NumberFormatException e )
		{
			sender.sendMessage( EnumColor.RED + "Invalid " + key + " entered; must be an integer but was '" + args.get( key ) + "'" );
			return Integer.MIN_VALUE;
		}
	}

	@CommandHandler( name = "pex", syntax = "commit [type] [id]", permission = "permissions.manage.commit", description = "Commit all permission changes to the backend" )
	public void commit( TerminalEntity sender, Map<String, String> args )
	{
		if ( args.containsKey( "type" ) && args.containsKey( "id" ) )
			switch ( args.get( "type" ) )
			{
				case "entity":
					PermissibleEntity entity = PermissionDispatcher.i().getEntity( args.get( "id" ) );
					if ( entity == null )
						sender.sendMessage( EnumColor.RED + "We could not find an entity with id `" + args.get( "id" ) + "`!" );
					else
					{
						entity.save();
						sender.sendMessage( EnumColor.AQUA + "Wonderful news, we successfully committed changes made to entity `" + entity.getId() + "`!" );
					}
					break;
				case "group":
					PermissibleGroup group = PermissionDispatcher.getGroup( args.get( "id" ) );
					if ( group == null )
						sender.sendMessage( EnumColor.RED + "We could not find a group with id `" + args.get( "id" ) + "`!" );
					else
					{
						group.save();
						sender.sendMessage( EnumColor.AQUA + "Wonderful news, we successfully committed changes made to group `" + group.getId() + "`!" );
					}
					break;
				case "io/amelia/permission":
					Permission perm = PermissionDispatcher.i().getNode( args.get( "id" ) );
					if ( perm == null )
						sender.sendMessage( EnumColor.RED + "We could not find a permission with namespace `" + args.get( "id" ) + "`!" );
					else
					{
						perm.commit();
						sender.sendMessage( EnumColor.AQUA + "Wonderful news, we successfully committed changes made to permission `" + perm.getNamespace() + "`!" );
					}
					break;
			}
		else
		{
			PermissionDispatcher.i().saveData();
			sender.sendMessage( EnumColor.AQUA + "Wonderful news, we successfully committed any changes to the backend!" );
		}

		// Force backend to finally flush changes
		PermissionDispatcher.getBackend().commit();
	}

	@SuppressWarnings( "unchecked" )
	@CommandHandler( name = "pex", syntax = "config <node> [value]", permission = "permissions.manage.config", description = "Print or set <node> [value]" )
	public void config( TerminalEntity sender, Map<String, String> args )
	{
		String nodeName = args.get( "node" );
		if ( nodeName == null || nodeName.isEmpty() )
			return;

		if ( args.get( "value" ) != null )
		{
			ConfigRegistry.set( nodeName, parseValue( args.get( "value" ) ) );
			ConfigRegistry.save();
		}

		Object node = ConfigRegistry.get( nodeName );
		if ( node instanceof Map )
		{
			sender.sendMessage( "Node \"" + nodeName + "\": " );
			for ( Map.Entry<String, Object> entry : ( ( Map<String, Object> ) node ).entrySet() )
				sender.sendMessage( "  " + entry.getKey() + " = " + entry.getValue() );
		}
		else if ( node instanceof List )
		{
			sender.sendMessage( "Node \"" + nodeName + "\": " );
			for ( String item : ( List<String> ) node )
				sender.sendMessage( " - " + item );
		}
		else
			sender.sendMessage( "Node \"" + nodeName + "\" = \"" + node + "\"" );
	}

	@CommandHandler( name = "pex", syntax = "dump <backend> <filename>", permission = "permissions.dump", description = "Dump users/groups to selected <backend> format" )
	public void dumpData( TerminalEntity sender, Map<String, String> args )
	{
		try
		{
			PermissionBackend backend = PermissionBackend.getBackendWithException( args.get( "backend" ) );

			File dstFile = new File( args.get( "filename" ) );

			FileOutputStream outStream = new FileOutputStream( dstFile );

			backend.dumpData( new OutputStreamWriter( outStream, "UTF-8" ) );

			outStream.close();

			sender.sendMessage( EnumColor.WHITE + "[Permissions] Data dumped in \"" + dstFile.getName() + "\" " );
		}
		catch ( IOException e )
		{
			sender.sendMessage( EnumColor.RED + "IO Error: " + e.getMessage() );
		}
		catch ( ClassNotFoundException e )
		{
			sender.sendMessage( EnumColor.RED + "Specified backend not found!" );
		}
		catch ( Throwable t )
		{
			sender.sendMessage( EnumColor.RED + "Error: " + t.getMessage() );
			PermissionDispatcher.getLogger().severe( "Error: " + t.getMessage(), t );
			// t.printStackTrace();
		}
	}

	@CommandHandler( name = "pex", syntax = "backend", permission = "permissions.manage.backend", description = "Print currently used backend" )
	public void getBackend( TerminalEntity sender, Map<String, String> args )
	{
		sender.sendMessage( "Current backend: " + PermissionDispatcher.i().getBackend() );
	}

	@CommandHandler( name = "pex", syntax = "hierarchy [ref]", permission = "permissions.manage.users", description = "Print complete user/group hierarchy" )
	public void printHierarchy( TerminalEntity sender, Map<String, String> args )
	{
		sender.sendMessage( "Entity/Group inheritance hierarchy:" );
		sendMessage( sender, this.printHierarchy( null, autoCompleteRef( args.get( "world" ) ), 0 ) );
	}

	@CommandHandler( name = "pex", syntax = "reload [id]", permission = "permissions.manage.reload", description = "Reload permissions and groups from backend" )
	public void reload( TerminalEntity sender, Map<String, String> args )
	{
		if ( args.containsKey( "id" ) )
		{
			PermissibleEntity entity = PermissionDispatcher.i().getEntity( args.get( "id" ), false );

			if ( entity == null )
			{
				PermissibleGroup group = PermissionDispatcher.i().getGroup( args.get( "id" ), false );

				if ( group == null )
					sender.sendMessage( EnumColor.RED + "We could not find anything with id `" + args.get( "id" ) + "`!" );
				else
				{
					group.reload();
					sender.sendMessage( EnumColor.AQUA + "Wonderful news, we successfully reloaded group `" + group.getId() + "` from backend!" );
				}
			}
			else
			{
				entity.reload();
				sender.sendMessage( EnumColor.AQUA + "Wonderful news, we successfully reloaded entity `" + entity.getId() + "` from backend!" );
			}
		}
		else
			try
			{
				PermissionDispatcher.i().reload();
				sender.sendMessage( EnumColor.AQUA + "Wonderful news, we successfully reloaded all entities and groups from the backend!" );
			}
			catch ( PermissionBackendException e )
			{
				sender.sendMessage( EnumColor.RED + "Failed to reload! Check configuration!\n" + EnumColor.RED + "Error (see console for full): " + e.getMessage() );
				PermissionDispatcher.getLogger().log( Level.WARNING, "Failed to reload permissions when " + sender.getDisplayName() + " ran `pex reload`", e );
			}
	}

	@CommandHandler( name = "pex", syntax = "report", permission = "permissions.manage.reportbug", description = "Create an issue template to report an issue" )
	public void report( TerminalEntity sender, Map<String, String> args )
	{
		/*
		 * ErrorReport report = ErrorReport.withException( "User-requested report", new Exception().fillInStackTrace() );
		 * sender.sendMessage( "Fill in the information at " + report.getShortURL() + " to report an issue" );
		 * sender.sendMessage( ConsoleColor.RED + "NOTE: A GitHub account is necessary to report issues. Create one at https://github.com/" );
		 */
	}

	@CommandHandler( name = "pex", syntax = "backend <backend>", permission = "permissions.manage.backend", description = "Change permission backend on the fly (Use with caution!)" )
	public void setBackend( TerminalEntity sender, Map<String, String> args )
	{
		if ( args.get( "backend" ) == null )
			return;

		try
		{
			PermissionDispatcher.i().setBackend( args.get( "backend" ) );
			sender.sendMessage( EnumColor.WHITE + "Permission backend changed!" );
		}
		catch ( RuntimeException e )
		{
			if ( e.getCause() instanceof ClassNotFoundException )
				sender.sendMessage( EnumColor.RED + "Specified backend not found." );
			else
			{
				sender.sendMessage( EnumColor.RED + "Error during backend initialization." );
				e.printStackTrace();
			}
		}
		catch ( PermissionBackendException e )
		{
			sender.sendMessage( EnumColor.RED + "Backend initialization failed! Fix your configuration!\n" + EnumColor.RED + "Error (see console for more): " + e.getMessage() );
			PermissionDispatcher.getLogger().log( Level.WARNING, "Backend initialization failed when " + sender.getDisplayName() + " was initializing " + args.get( "backend" ), e );
		}
	}

	@CommandHandler( name = "pex", syntax = "help [page] [count]", permission = "permissions.manage", description = "PermissionManager commands help" )
	public void showHelp( TerminalEntity sender, Map<String, String> args )
	{
		List<CommandBinding> commands = command.getCommands();

		int count = tryGetInt( sender, args, "count", 8 );
		int page = tryGetInt( sender, args, "page", 1 );

		if ( page == Integer.MIN_VALUE || count == Integer.MIN_VALUE )
			return; // method already prints error message

		if ( page < 1 )
		{
			sender.sendMessage( EnumColor.RED + "Page couldn't be lower than 1" );
			return;
		}

		int totalPages = ( int ) Math.ceil( commands.size() / count );

		sender.sendMessage( EnumColor.BLUE + "PermissionManager" + EnumColor.WHITE + " commands (page " + EnumColor.GOLD + page + "/" + totalPages + EnumColor.WHITE + "): " );

		int base = count * ( page - 1 );

		for ( int i = base; i < base + count; i++ )
		{
			if ( i >= commands.size() )
				break;

			CommandHandler command = commands.get( i ).getMethodAnnotation();
			String commandName = String.format( "/%s %s", command.name(), command.syntax() ).replace( "<", EnumColor.BOLD.toString() + EnumColor.RED + "<" ).replace( ">", ">" + EnumColor.RESET + EnumColor.GOLD.toString() ).replace( "[", EnumColor.BOLD.toString() + EnumColor.BLUE + "[" ).replace( "]", "]" + EnumColor.RESET + EnumColor.GOLD.toString() );


			sender.sendMessage( EnumColor.GOLD + commandName );
			sender.sendMessage( EnumColor.AQUA + "    " + command.description() );
		}
	}

	@CommandHandler( name = "pex", syntax = "toggle debug", permission = "permissions.debug", description = "Enable/disable debug mode" )
	public void toggleFeature( TerminalEntity sender, Map<String, String> args )
	{
		PermissionDispatcher.setDebug( !PermissionDispatcher.isDebug() );

		String debugStatusMessage = "[Permissions] Debug mode " + ( PermissionDispatcher.isDebug() ? "enabled" : "disabled" );

		sender.sendMessage( debugStatusMessage );
		PermissionDispatcher.getLogger().warning( debugStatusMessage );
	}
}
