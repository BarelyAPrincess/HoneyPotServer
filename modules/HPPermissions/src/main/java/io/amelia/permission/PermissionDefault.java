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


public enum PermissionDefault
{
	USER( "sys.user" ),
	ADMIN( "sys.admin" ),
	BANNED( "sys.banned" ),
	DEFAULT( "default" ),
	EVERYBODY( "" ),
	OP( "sys.op" ),
	WHITELISTED( "sys.whitelisted" ),
	QUERY( "sys.query" );

	/**
	 * By calling each Permission node we forces it's creation if non-existent
	 */
	public static void initNodes()
	{
		USER.getNode();
		ADMIN.getNode();
		BANNED.getNode();
		DEFAULT.getNode();
		EVERYBODY.getNode();
		OP.getNode();
		WHITELISTED.getNode();
		QUERY.getNode();
	}

	public static boolean isDefault( Permission perm )
	{
		for ( PermissionDefault pd : PermissionDefault.values() )
			if ( pd.getNamespace().equalsIgnoreCase( perm.getNamespace() ) )
				return true;

		return false;
	}

	private String namespace = "";

	PermissionDefault( String namespace )
	{
		this.namespace = namespace;
	}

	public String getLocalName()
	{
		return namespace.contains( "." ) ? namespace.substring( namespace.indexOf( "." ) + 1 ) : namespace;
	}

	public String getNamespace()
	{
		return namespace;
	}

	public Permission getNode()
	{
		Permission result = PermissionDispatcher.getNode( namespace );

		if ( result == null )
		{
			if ( this == EVERYBODY )
			{
				result = PermissionDispatcher.createNode( getNamespace(), PermissionType.BOOL );
				result.getModel().setValue( true );
				result.getModel().setValueDefault( true );
			}
			else
				result = PermissionDispatcher.createNode( getNamespace() );

			switch ( this )
			{
				case DEFAULT:
					result.getModel().setDescription( "Used as the default permission node if one does not exist. (DO NOT EDIT!)" );
					break;
				case EVERYBODY:
					result.getModel().setDescription( "This node is used for the 'everyone' permission. (DO NOT EDIT!)" );
					break;
				case OP:
					result.getModel().setDescription( "Indicates OP entities. (DO NOT EDIT!)" );
					break;
				case USER:
					result.getModel().setDescription( "Indicates a general USER entity. (DO NOT EDIT!)" );
					break;
				case ADMIN:
					result.getModel().setDescription( "Indicates ADMIN entities. (DO NOT EDIT!)" );
					break;
				case BANNED:
					result.getModel().setDescription( "Indicates BANNED entities. (DO NOT EDIT!)" );
					break;
				case WHITELISTED:
					result.getModel().setDescription( "Indicates WHITELISTED entities. (DO NOT EDIT!)" );
					break;
				case QUERY:
					result.getModel().setDescription( "Indicates entities allowed to login thru QUERY server. (DO NOT EDIT!)" );
					break;
			}

			result.commit();
		}

		return result;
	}

	@Override
	public String toString()
	{
		return name() + " {namespace=" + namespace + "}";
	}
}
