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
import com.chiorichan.account.AccountManager;
import com.chiorichan.account.AccountMeta;
import com.chiorichan.account.LocationService;
import com.chiorichan.permission.PermissibleEntity;
import com.chiorichan.permission.PermissibleGroup;
import com.chiorichan.permission.Permission;
import com.chiorichan.permission.PermissionDispatcher;
import com.chiorichan.permission.References;
import com.chiorichan.terminal.TerminalEntity;
import com.chiorichan.terminal.commands.AdvancedCommand;
import com.chiorichan.terminal.commands.advanced.AutoCompleteChoicesException;
import com.chiorichan.terminal.commands.advanced.CommandListener;
import com.google.common.collect.Sets;
import io.amelia.foundation.ConfigRegistry;
import io.amelia.foundation.Kernel;
import io.amelia.lang.EnumColor;
import io.amelia.foundation.messaging.MessageBuilder;
import io.amelia.foundation.messaging.MessageDispatch;
import io.amelia.foundation.messaging.MessageException;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public abstract class PermissionBaseCommand implements CommandListener
{
	protected AdvancedCommand command;

	protected String autoCompleteAccount( String account )
	{
		return autoCompleteAccount( account, "user" );
	}

	protected String autoCompleteAccount( String query, String argName )
	{
		if ( query == null )
			return null;

		if ( query.startsWith( "#" ) )
			return query.substring( 1 );

		Set<String> accounts = Sets.newHashSet();

		for ( AccountMeta account : AccountManager.i().getAccounts() )
		{
			if ( account.getId().toLowerCase().startsWith( query.toLowerCase() ) )
				accounts.add( account.getId() );

			if ( account.getDisplayName().toLowerCase().startsWith( query.toLowerCase() ) )
				accounts.add( account.getDisplayName() );
		}

		if ( accounts.size() > 1 )
			throw new AutoCompleteChoicesException( accounts.toArray( new String[0] ), argName );
		else if ( accounts.size() == 1 )
			return accounts.toArray( new String[0] )[0];

		return query;
	}

	protected String autoCompleteGroupName( String groupName )
	{
		return this.autoCompleteGroupName( groupName, "group" );
	}

	protected String autoCompleteGroupName( String groupName, String argName )
	{
		if ( groupName.startsWith( "#" ) )
			return groupName.substring( 1 );

		List<String> groups = new LinkedList<String>();

		for ( String group : PermissionDispatcher.i().getBackend().getGroupNames() )
		{
			if ( group.equalsIgnoreCase( groupName ) )
				return group;

			if ( group.toLowerCase().startsWith( groupName.toLowerCase() ) && !groups.contains( group ) )
				groups.add( group );
		}

		if ( groups.size() > 1 )
			throw new AutoCompleteChoicesException( groups.toArray( new String[0] ), argName );
		else if ( groups.size() == 1 )
			return groups.get( 0 );

		// Nothing found
		return groupName;
	}

	protected String autoCompletePermission( PermissibleEntity entity, String permission, References refs )
	{
		return this.autoCompletePermission( entity, permission, refs, "io/io.amelia/permission" );
	}

	protected String autoCompletePermission( PermissibleEntity entity, String permission, References refs, String argName )
	{
		if ( permission == null )
			return permission;

		Set<String> permissions = Sets.newHashSet();
		for ( Permission perm : entity.getPermissions( refs ) )
		{
			if ( perm.getNamespace().equalsIgnoreCase( permission ) )
				return perm.getLocalName();

			if ( perm.getNamespace().startsWith( permission.toLowerCase() ) )
				permissions.add( perm.getNamespace() );
		}

		if ( permissions.size() > 0 )
		{
			String[] permissionArray = permissions.toArray( new String[0] );

			if ( permissionArray.length == 1 )
				return permissionArray[0];

			throw new AutoCompleteChoicesException( permissionArray, argName );
		}

		return permission;
	}

	protected References autoCompleteRef( String ref )
	{
		return autoCompleteRef( ref, "default" );
	}

	protected References autoCompleteRef( String ref, String argName )
	{
		if ( ref == null || ref.isEmpty() || "*".equals( ref ) )
			return References.format();

		Set<String> refs = Sets.newHashSet();

		for ( String r : PermissionDispatcher.i().getReferences() )
			if ( r.toLowerCase().startsWith( ref.toLowerCase() ) )
				refs.add( r );

		LocationService service = Kernel.getServiceProvider( LocationService.class );
		if ( service != null )
			service.getLocations().filter( l -> l != null && l.getId().toLowerCase().startsWith( ref.toLowerCase() ) ).forEach( l -> refs.add( l.getId() ) );

		if ( refs.size() > 1 )
			throw new AutoCompleteChoicesException( refs.toArray( new String[0] ), argName );
		else if ( refs.size() == 1 )
			return References.format( refs.toArray( new String[0] )[0] );

		return References.format( ref );
	}

	protected List<String> getPermissionsTree( PermissibleEntity entity, References refs, int level )
	{
		// Thing might need some help!

		List<String> permissions = new LinkedList<String>();
		Set<String> refPermissions = Sets.newHashSet();
		Set<String> commonPermissions = Sets.newHashSet();

		for ( Entry<Permission, References> perm : entity.getPermissionEntrys( refs ) )
			if ( perm.getValue().isEmpty() )
				commonPermissions.add( perm.getKey().getNamespace() );
			else
				refPermissions.add( perm.getKey().getNamespace() );

		permissions.addAll( sprintPermissions( refs, refPermissions.toArray( new String[0] ) ) );

		// for ( String parentSite : Permissions.getPermissionManager().getSiteInheritance( ref ) )
		// if ( parentSite != null && !parentSite.isEmpty() )
		// permissions.addAll( getPermissionsTree( entity, parentSite, level + 1 ) );

		if ( level == 0 && commonPermissions.size() > 0 )
			permissions.addAll( sprintPermissions( References.format(), commonPermissions.toArray( new String[0] ) ) );

		return permissions;
	}

	protected int getPosition( String permission, Permission[] permissions )
	{
		try
		{
			// permission is permission index
			int position = Integer.parseInt( permission ) - 1;

			if ( position < 0 || position >= permissions.length )
				throw new RuntimeException( "Wrong permission index specified!" );

			return position;
		}
		catch ( NumberFormatException e )
		{
			for ( int i = 0; i < permissions.length; i++ )
				if ( permission.equalsIgnoreCase( permissions[i].getNamespace() ) )
					return i;
		}

		throw new RuntimeException( "Specified permission not found" );
	}

	protected References getSafeSite( References ref, String acctId )
	{
		if ( ref == null )
		{
			AccountMeta meta = AccountManager.i().getAccount( "%", acctId );

			if ( meta == null )
				ref = References.format( ( ( LocationService ) Kernel.getServiceProvider( LocationService.class ) ).getDefaultLocation().getId() );
			else
				ref = References.format( meta.getLocation().getId() );
		}

		return ref;
	}

	protected void informEntity( String entityId, String message )
	{
		try
		{
			informEntityWithException( entityId, message );
		}
		catch ( MessageException e )
		{
			e.printStackTrace();
		}
	}

	protected void informEntityWithException( String entityId, String message ) throws MessageException
	{
		if ( !ConfigRegistry.i().getBoolean( "permissions.informEntities.changes", false ) )
			return; // User informing is disabled

		AccountMeta meta = AccountManager.i().getAccount( "%", entityId );
		if ( meta == null )
			return;

		MessageDispatch.sendMessage( MessageBuilder.msg( EnumColor.BLUE + "[Permissions] " + EnumColor.WHITE + message ).from( meta ) );
	}

	protected void informGroup( PermissibleGroup group, String message )
	{
		try
		{
			informGroupWithException( group, message );
		}
		catch ( MessageException e )
		{
			e.printStackTrace();
		}
	}

	protected void informGroupWithException( PermissibleGroup group, String message ) throws MessageException
	{
		for ( PermissibleEntity entity : group.getChildEntities( true, References.format() ) )
			informEntity( entity.getId(), message );
	}

	protected String mapPermissions( References refs, PermissibleEntity entity, int level )
	{
		StringBuilder builder = new StringBuilder();

		int index = 1;
		for ( String permission : getPermissionsTree( entity, refs, 0 ) )
		{
			if ( level > 0 )
				builder.append( "   " );
			else
				builder.append( index++ ).append( ") " );

			builder.append( permission );
			if ( level > 0 )
				builder.append( " (from " ).append( entity.getId() ).append( ")" );
			else
				builder.append( " (own)" );
			builder.append( "\n" );
		}

		// entity.getGroups( refs );

		level++; // Just increment level once
		return builder.toString();
	}

	@Override
	public void onRegistered( AdvancedCommand command )
	{
		this.command = command;
	}

	protected Object parseValue( String value )
	{
		if ( value == null )
			return null;

		if ( value.equalsIgnoreCase( "true" ) || value.equalsIgnoreCase( "false" ) )
			return Boolean.parseBoolean( value );

		try
		{
			return Integer.parseInt( value );
		}
		catch ( NumberFormatException e )
		{
			// Ignore
		}

		try
		{
			return Double.parseDouble( value );
		}
		catch ( NumberFormatException e )
		{
			// Ignore
		}

		return value;
	}

	protected void printEntityInheritance( AccountAttachment sender, Collection<PermissibleGroup> collection )
	{
		for ( PermissibleGroup group : collection )
		{
			String rank = "not ranked";
			if ( group.isRanked() )
				rank = "rank " + group.getRank() + " @ "; // XXX + group.getRankLadder();

			sender.sendMessage( "   " + group.getId() + " (" + rank + ")" );
		}
	}

	protected String printHierarchy( PermissibleGroup parent, References refs, int level )
	{
		StringBuilder buffer = new StringBuilder();

		Collection<PermissibleGroup> groups = parent == null ? PermissionDispatcher.i().getGroups() : parent.getChildGroups( refs );

		for ( PermissibleGroup group : groups )
		{
			if ( parent == null && group.getGroups( refs ).size() > 0 )
				continue;

			buffer.append( StringUtils.repeat( "  ", level ) ).append( " - " ).append( group.getId() ).append( "\n" );

			// Groups
			buffer.append( printHierarchy( group, refs, level + 1 ) );

			for ( PermissibleEntity user : group.getChildEntities( refs ) )
				buffer.append( StringUtils.repeat( "  ", level + 1 ) ).append( " + " ).append( user.getId() ).append( "\n" );
		}

		return buffer.toString();
	}

	protected void sendMessage( TerminalEntity sender, String message )
	{
		for ( String messagePart : message.split( "\n" ) )
			sender.sendMessage( messagePart );
	}

	protected List<String> sprintPermissions( References refs, String[] permissions )
	{
		List<String> permissionList = new LinkedList<String>();

		if ( permissions == null )
			return permissionList;

		for ( String permission : permissions )
			permissionList.add( permission + ( refs != null ? " @" + refs.toString() : "" ) );

		return permissionList;
	}
}
