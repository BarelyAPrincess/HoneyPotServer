/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.permission;

import io.amelia.support.NamespaceBase;
import io.amelia.support.Strs;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Extends the base {@link NamespaceBase} and adds permission specific methods
 */
public class PermissionNamespace extends NamespaceBase<PermissionNamespace>
{
	public static PermissionNamespace parseString( String namespace )
	{
		return parseString( namespace, null );
	}

	public static PermissionNamespace parseString( String namespace, String separator )
	{
		if ( namespace == null )
			namespace = "";
		if ( separator == null || separator.length() == 0 )
			separator = ".";
		return new PermissionNamespace( Strs.split( namespace, separator ).collect( Collectors.toList() ) );
	}

	public static PermissionNamespace parseStringRegex( String namespace, String regex )
	{
		if ( namespace == null )
			namespace = "";
		if ( regex == null || regex.length() == 0 )
			regex = "\\.";
		return new PermissionNamespace( Strs.split( namespace, regex ).collect( Collectors.toList() ) );
	}

	public PermissionNamespace( String[] nodes )
	{
		super( nodes );
	}

	public PermissionNamespace( List<String> nodes )
	{
		super( nodes );
	}

	public PermissionNamespace()
	{
		super();
	}

	@Override
	protected PermissionNamespace create( String[] nodes )
	{
		return new PermissionNamespace( nodes );
	}

	public Permission createPermission()
	{
		return PermissionGuard.createNode( getString() );
	}

	public Permission createPermission( PermissionType type )
	{
		return PermissionGuard.createNode( getString(), type );
	}

	public Permission getPermission()
	{
		return PermissionGuard.getNode( getString() );
	}

	public boolean matches( Permission perm )
	{
		return matches( perm.getNamespace() );
	}
}
