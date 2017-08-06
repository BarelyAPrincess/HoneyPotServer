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

import io.amelia.foundation.binding.BindingReference;
import io.amelia.lang.EnumColor;
import io.amelia.support.Strs;

/**
 * Permission class for each permission node
 */
public final class Permission extends BindingReference<Permission, PermissionMeta> implements Comparable<Permission>
{
	public Permission()
	{
		super( PermissionMeta.class );
	}

	public void commit()
	{
		PermissionGuard.getBackend().nodeCommit( this );
	}

	@Override
	public int compareTo( Permission perm )
	{
		if ( getNamespace().equals( perm.getNamespace() ) )
			return 0;

		PermissionNamespace ns1 = getNamespaceObj();
		PermissionNamespace ns2 = perm.getNamespaceObj();

		int ln = Math.min( ns1.getNodeCount(), ns2.getNodeCount() );

		for ( int i = 0; i < ln; i++ )
			if ( !ns1.getNode( i ).equals( ns2.getNode( i ) ) )
				return ns1.getNode( i ).compareTo( ns2.getNode( i ) );

		return ns1.getNodeCount() > ns2.getNodeCount() ? -1 : 1;
	}

	public String dumpPermissionStack()
	{
		return dumpPermissionStack0( 0 );
	}

	private String dumpPermissionStack0( int depth )
	{
		StringBuilder output = new StringBuilder();

		String spacing = depth > 0 ? Strs.repeat( "      ", depth - 1 ) + "|---> " : "";

		output.append( String.format( "%s%s%s=%s", EnumColor.YELLOW, spacing, getLocalName(), getPermissionMeta() ) );
		getChildren().forEach( p -> output.append( p.dumpPermissionStack0( depth + 1 ) ) );

		return output.toString();
	}

	@Override
	public PermissionNamespace getNamespaceObj()
	{
		return PermissionNamespace.transform( super.getNamespaceObj() );
	}

	public PermissionMeta getPermissionMeta()
	{
		return computeValue( v -> v.orElseGet( () -> new PermissionMeta( getNamespaceObj() ) ) );
	}

	@Override
	public String toString()
	{
		return String.format( "Permission{name=%s,parent=%s,meta=%s}", getLocalName(), getParent(), getPermissionMeta() );
	}
}
