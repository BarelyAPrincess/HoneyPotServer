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

import io.amelia.foundation.service.ServiceProvider;
import io.amelia.lang.EnumColor;
import io.amelia.permission.lang.PermissionException;
import io.amelia.support.Namespace;
import io.amelia.support.Strs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Permission class for each permission node
 */
public final class Permission implements Comparable<Permission>
{
	protected final List<Permission> children = new CopyOnWriteArrayList<>();
	protected final String localName;
	protected final Permission parent;
	protected PermissionModelValue model;

	public Permission( Namespace ns )
	{
		this( ns.getLocalName(), PermissionType.DEFAULT, ns.getNodeCount() <= 1 ? null : PermissionDispatcher.createNode( ns.getParent() ) );
	}

	public Permission( Namespace ns, PermissionType type )
	{
		this( ns.getLocalName(), type, ns.getNodeCount() <= 1 ? null : PermissionDispatcher.createNode( ns.getParent() ) );
	}

	public Permission( String localName )
	{
		this( localName, PermissionType.DEFAULT );
	}

	public Permission( String localName, Permission parent )
	{
		this( localName, PermissionType.DEFAULT, parent );
	}

	public Permission( String localName, PermissionType type )
	{
		this( localName, type, null );
	}

	public Permission( String localName, PermissionType type, Permission parent )
	{
		if ( !localName.matches( "[a-z0-9_]*" ) )
			throw new PermissionException( String.format( "The permission local name '%s' can only contain characters a-z, 0-9, and _.", localName ) );

		this.localName = localName;
		this.parent = parent;

		model = new PermissionModelValue( localName, type, this );
		PermissionDispatcher.addPermission( this );
	}

	public void addChild( Permission node )
	{
		children.add( node );
	}

	public void commit()
	{
		PermissionDispatcher.getBackend().nodeCommit( this );
	}

	@Override
	public int compareTo( Permission perm )
	{
		if ( getNamespace().equals( perm.getNamespace() ) )
			return 0;

		Namespace ns1 = getPermissionNamespace();
		Namespace ns2 = perm.getPermissionNamespace();

		int ln = Math.min( ns1.getNodeCount(), ns2.getNodeCount() );

		for ( int i = 0; i < ln; i++ )
			if ( !ns1.getNode( i ).equals( ns2.getNode( i ) ) )
				return ns1.getNode( i ).compareTo( ns2.getNode( i ) );

		return ns1.getNodeCount() > ns2.getNodeCount() ? -1 : 1;
	}

	public void debugPermissionStack( AccountAttachment sender, int depth )
	{
		String spacing = depth > 0 ? Strs.repeat( "      ", depth - 1 ) + "|---> " : "";

		sender.sendMessage( String.format( "%s%s%s=%s", EnumColor.YELLOW, spacing, getLocalName(), model ) );

		depth++;
		for ( Permission p : children )
			p.debugPermissionStack( sender, depth );
	}

	public void debugPermissionStack( int depth )
	{
		debugPermissionStack( ApplicationTerminal.terminal(), depth );
	}

	public Permission getChild( String name )
	{
		for ( Permission node : children )
			if ( node.getLocalName().equals( name ) )
				return node;
		return null;
	}

	/**
	 * Returns the Permission Children of this Permission
	 *
	 * @return Permission Children
	 */
	public List<Permission> getChildren()
	{
		return Collections.unmodifiableList( children );
	}

	/**
	 * Returns all children of this
	 *
	 * @return List of Permission Children
	 */
	public List<Permission> getChildrenRecursive()
	{
		return getChildrenRecursive( false );
	}

	/**
	 * Returns all children of this
	 *
	 * @param includeParents Shall we include parent Permission of all children
	 * @return List of Permission Children
	 */
	public List<Permission> getChildrenRecursive( boolean includeParents )
	{
		List<Permission> result = new ArrayList<>();

		getChildrenRecursive( result, includeParents );

		return result;
	}

	private void getChildrenRecursive( List<Permission> result, boolean includeParents )
	{
		if ( includeParents || !hasChildren() )
			result.add( this );

		for ( Permission p : getChildren() )
			p.getChildrenRecursive( result, includeParents );
	}

	/**
	 * Returns the unique fully qualified name of this Permission
	 *
	 * @return Fully qualified name
	 */
	public String getLocalName()
	{
		return localName.toLowerCase();
	}

	/**
	 * Return the {@link PermissionModelValue} class instance
	 *
	 * @return {@link PermissionModelValue} class instance
	 */
	public PermissionModelValue getModel()
	{
		return model;
	}

	/**
	 * Returns the dynamic Permission Namespace
	 *
	 * @return The Permission Namespace as a string
	 */
	public String getNamespace()
	{
		String namespace = "";
		Permission curr = this;

		do
		{
			namespace = curr.getLocalName() + "." + namespace;
			curr = curr.getParent();
		}
		while ( curr != null );

		namespace = namespace.substring( 0, namespace.length() - 1 );
		return namespace;
	}

	public Permission getParent()
	{
		return parent;
	}

	/**
	 * Returns the {@link Namespace} class instance
	 *
	 * @return {@link Namespace} class instance
	 */
	public Namespace getPermissionNamespace()
	{
		return Namespace.parseString( getNamespace() );
	}

	public PermissionType getType()
	{
		return model.getType();
	}

	void setType( PermissionType type )
	{
		model = new PermissionModelValue( localName, type, this );
	}

	public boolean hasChildren()
	{
		return children.size() > 0;
	}

	@Override
	public String toString()
	{
		return String.format( "Permission{name=%s,parent=%s,modelValue=%s}", getLocalName(), getParent(), model );
	}
}
