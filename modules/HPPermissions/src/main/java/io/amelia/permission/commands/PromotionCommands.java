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

import com.chiorichan.permission.PermissibleEntity;
import com.chiorichan.permission.PermissibleGroup;
import com.chiorichan.permission.PermissionDispatcher;
import com.chiorichan.permission.lang.RankingException;
import com.chiorichan.terminal.TerminalEntity;
import com.chiorichan.terminal.commands.advanced.CommandHandler;
import io.amelia.lang.EnumColor;

import java.util.Map;

public class PromotionCommands extends PermissionBaseCommand
{
	@CommandHandler( name = "pex", syntax = "demote <user> [ladder]", description = "Demotes <user> to previous group or [ladder]", isPrimary = true )
	public void demoteUser( TerminalEntity sender, Map<String, String> args )
	{
		String userName = autoCompleteAccount( args.get( "user" ) );
		PermissibleEntity user = PermissionDispatcher.i().getEntity( userName );

		if ( user == null )
		{
			sender.sendMessage( EnumColor.RED + "Specified user \"" + args.get( "user" ) + "\" not found!" );
			return;
		}

		String ladder = "default";

		if ( args.containsKey( "ladder" ) )
			ladder = args.get( "ladder" );

		PermissibleEntity demoter = sender.getPermissibleEntity();
		String demoterName = sender.getDisplayName();

		// TODO Get reference based on connection method, e.g., Telnet Query
		if ( demoter == null || !demoter.checkPermission( "permissions.user.demote." + ladder ).isTrue() )
		{
			sender.sendMessage( EnumColor.RED + "You don't have enough permissions to demote on this ladder" );
			return;
		}

		try
		{
			PermissibleGroup targetGroup = user.demote( demoter, args.get( "ladder" ) );

			informEntity( user.getId(), "You have been demoted on " + targetGroup.getRankLadder() + " ladder to " + targetGroup.getId() + " group" );
			sender.sendMessage( "User " + user.getId() + " demoted to " + targetGroup.getId() + " group" );
			PermissionDispatcher.getLogger().info( "User " + user.getId() + " has been demoted to " + targetGroup.getId() + " group on " + targetGroup.getRankLadder() + " ladder by " + demoterName );
		}
		catch ( RankingException e )
		{
			sender.sendMessage( EnumColor.RED + "Demotion error: " + e.getMessage() );
			PermissionDispatcher.getLogger().severe( "Ranking Error (" + demoterName + " demotes " + e.getTarget().getId() + "): " + e.getMessage() );
		}
	}

	@CommandHandler( name = "demote", syntax = "<user>", description = "Demotes <user> to previous group", isPrimary = true, permission = "permissions.user.rank.demote" )
	public void demoteUserAlias( TerminalEntity sender, Map<String, String> args )
	{
		demoteUser( sender, args );
	}

	@CommandHandler( name = "pex", syntax = "promote <user> [ladder]", description = "Promotes <user> to next group on [ladder]", isPrimary = true )
	public void promoteUser( TerminalEntity sender, Map<String, String> args )
	{
		String userName = autoCompleteAccount( args.get( "user" ) );
		PermissibleEntity user = PermissionDispatcher.i().getEntity( userName );

		if ( user == null )
		{
			sender.sendMessage( "Specified user \"" + args.get( "user" ) + "\" not found!" );
			return;
		}

		String ladder = "default";

		if ( args.containsKey( "ladder" ) )
			ladder = args.get( "ladder" );

		PermissibleEntity promoter = sender.getPermissibleEntity();
		String promoterName = sender.getDisplayName();

		// TODO Get reference based on connection method, e.g., Telnet Query
		if ( promoter == null || !promoter.checkPermission( "permissions.user.demote." + ladder ).isTrue() )
		{
			sender.sendMessage( EnumColor.RED + "You don't have enough permissions to demote on this ladder" );
			return;
		}

		try
		{
			PermissibleGroup targetGroup = user.promote( promoter, ladder );

			informEntity( user.getId(), "You have been promoted on " + targetGroup.getRankLadder() + " ladder to " + targetGroup.getId() + " group" );
			sender.sendMessage( "User " + user.getId() + " promoted to " + targetGroup.getId() + " group" );
			PermissionDispatcher.getLogger().info( "User " + user.getId() + " has been promoted to " + targetGroup.getId() + " group on " + targetGroup.getRankLadder() + " ladder by " + promoterName );
		}
		catch ( RankingException e )
		{
			sender.sendMessage( EnumColor.RED + "Promotion error: " + e.getMessage() );
			PermissionDispatcher.getLogger().severe( "Ranking Error (" + promoterName + " > " + e.getTarget().getId() + "): " + e.getMessage() );
		}
	}

	@CommandHandler( name = "promote", syntax = "<user>", description = "Promotes <user> to next group", isPrimary = true, permission = "permissions.user.rank.promote" )
	public void promoteUserAlias( TerminalEntity sender, Map<String, String> args )
	{
		promoteUser( sender, args );
	}

	@CommandHandler( name = "pex", syntax = "group <group> rank [rank] [ladder]", description = "Get or set <group> [rank] [ladder]", isPrimary = true, permission = "permissions.groups.rank.<group>" )
	public void rankGroup( TerminalEntity sender, Map<String, String> args )
	{
		String groupName = this.autoCompleteGroupName( args.get( "group" ) );

		PermissibleGroup group = PermissionDispatcher.i().getGroup( groupName );

		if ( group == null )
		{
			sender.sendMessage( EnumColor.RED + "Group \"" + groupName + "\" not found" );
			return;
		}

		if ( args.get( "rank" ) != null )
		{
			String newRank = args.get( "rank" ).trim();

			try
			{
				group.setRank( Integer.parseInt( newRank ) );
			}
			catch ( NumberFormatException e )
			{
				sender.sendMessage( "Wrong rank. Make sure it's number." );
			}

			if ( args.containsKey( "ladder" ) )
				group.setRankLadder( args.get( "ladder" ) );
		}

		int rank = group.getRank();

		if ( rank > 0 )
			sender.sendMessage( "Group " + group.getId() + " rank is " + rank + " (ladder = " + group.getRankLadder() + ")" );
		else
			sender.sendMessage( "Group " + group.getId() + " is unranked" );
	}
}
