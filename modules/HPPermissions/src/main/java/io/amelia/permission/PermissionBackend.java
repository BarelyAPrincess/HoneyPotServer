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

import io.amelia.permission.lang.PermissionBackendException;

import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides the basis of Permission Backend classes
 */
public abstract class PermissionBackend
{
	public static final int ENTITY = 0;
	public static final int GROUP = 1;
	protected static final String defaultBackend = "file";
	/**
	 * Array of backend aliases
	 */
	protected static Map<String, Class<? extends PermissionBackend>> registeredAliases = new HashMap<String, Class<? extends PermissionBackend>>();

	// TODO Make it so node can be changed from one backend to another with ease and without restarting.

	private static PermissionBackend getBackend( Class<? extends PermissionBackend> backendClass )
	{
		try
		{
			PermissionGuard.L.info( "Initializing " + backendClass.getName() + " backend" );
			Constructor<? extends PermissionBackend> constructor = backendClass.getConstructor();
			return constructor.newInstance();
		}
		catch ( Exception e )
		{
			throw new RuntimeException( e );
		}
	}

	/**
	 * Return a specific Backend instance, be that the current backend or not.
	 *
	 * @param backendName Class name or alias of backend
	 * @return instance of PermissionBackend object
	 */
	public static PermissionBackend getBackend( String backendName )
	{
		try
		{
			return getBackendWithException( backendName );
		}
		catch ( ClassNotFoundException e )
		{
			return null;
		}
	}

	public static PermissionBackend getBackend( String backendName, String fallBackBackend )
	{
		try
		{
			return getBackendWithException( backendName, fallBackBackend );
		}
		catch ( ClassNotFoundException e )
		{
			return null;
		}
	}

	/**
	 * Return alias for specified backend class
	 * If there is no such class registered the name of this class would
	 * be returned using backendClass.getName();
	 *
	 * @param backendClass
	 * @return alias or class name when not found using backendClass.getName()
	 */
	public static String getBackendAlias( Class<? extends PermissionBackend> backendClass )
	{
		if ( registeredAliases.containsValue( backendClass ) )
			for ( String alias : registeredAliases.keySet() )
				if ( registeredAliases.get( alias ).equals( backendClass ) )
					return alias;

		return backendClass.getName();
	}

	/**
	 * Returns Class object for specified alias, if there is no alias registered
	 * then try to find it using Class.forName(alias)
	 *
	 * @param alias
	 * @return PermissionBackend Class for alias provided
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings( "unchecked" )
	public static Class<? extends PermissionBackend> getBackendClass( String alias ) throws ClassNotFoundException
	{
		if ( !registeredAliases.containsKey( alias ) )
			return ( Class<? extends PermissionBackend> ) Class.forName( alias );

		return registeredAliases.get( alias );
	}

	/**
	 * Return class name for alias
	 *
	 * @param alias
	 * @return Class name if found or alias if there is no such class name present
	 */
	public static String getBackendClassName( String alias )
	{

		if ( registeredAliases.containsKey( alias ) )
			return registeredAliases.get( alias ).getName();

		return alias;
	}

	public static PermissionBackend getBackendWithException( String backendName ) throws ClassNotFoundException
	{
		Class<? extends PermissionBackend> backendClass = getBackendClass( backendName );
		if ( PermissionGuard.getBackend() != null && PermissionGuard.getBackend().getClass() == backendClass )
			return PermissionGuard.getBackend();
		else
			return getBackend( backendClass );
	}

	/**
	 * Returns a new Backend class instance for specified backendName
	 *
	 * @param backendName     Class name or alias of backend
	 * @param fallBackBackend name of backend that should be used if specified backend was not found or failed to initialize
	 * @return new instance of PermissionBackend object
	 */
	public static PermissionBackend getBackendWithException( String backendName, String fallBackBackend ) throws ClassNotFoundException
	{
		if ( backendName == null || backendName.isEmpty() )
			backendName = defaultBackend;

		try
		{
			return getBackendWithException( backendName );
		}
		catch ( ClassNotFoundException e )
		{
			PermissionGuard.L.warning( "Specified backend \"" + backendName + "\" was not found." );

			if ( fallBackBackend == null )
				throw e;

			if ( !getBackendClassName( backendName ).equals( getBackendClassName( fallBackBackend ) ) )
				return getBackend( fallBackBackend );
			else
				throw e;
		}
	}

	/**
	 * Register new alias for specified backend class
	 *
	 * @param alias
	 * @param backendClass
	 */
	public static void registerBackendAlias( String alias, Class<? extends PermissionBackend> backendClass )
	{
		if ( !PermissionBackend.class.isAssignableFrom( backendClass ) )
			throw new RuntimeException( "Provided class should be subclass of PermissionBackend" );

		registeredAliases.put( alias, backendClass );

		PermissionGuard.L.fine( alias + " backend registered!" );
	}

	public abstract void commit();

	public void dumpData( OutputStreamWriter outputStreamWriter )
	{
		// TODO Auto-generated method stub
	}

	/**
	 * Returns default group, a group that is assigned to a entity without a group set
	 *
	 * @return Default group instance
	 */
	public abstract PermissibleGroup getDefaultGroup( References refs );

	/**
	 * Returns new PermissibleEntity object for specified id
	 *
	 * @param id
	 * @return PermissibleEntity for specified id, or null on error.
	 */
	public abstract PermissibleEntity getEntity( String id );

	/**
	 * This method loads all entity names from the backend.
	 */
	public abstract Collection<String> getEntityNames();

	public abstract Collection<String> getEntityNames( int type );

	/**
	 * Returns new PermissibleGroup object for specified id
	 *
	 * @param id
	 * @return PermissibleGroup object, or null on error
	 */
	public abstract PermissibleGroup getGroup( String id );

	/**
	 * This method loads all group names from the backend.
	 */
	public abstract Collection<String> getGroupNames();

	/**
	 * Backend initialization should be done here
	 */
	public abstract void initialize() throws PermissionBackendException;

	/**
	 * This method loads all entities from the backend.
	 *
	 * @throws PermissionBackendException
	 */
	public abstract void loadEntities() throws PermissionBackendException;

	/**
	 * This method loads all groups from the backend.
	 *
	 * @throws PermissionBackendException
	 */
	public abstract void loadGroups() throws PermissionBackendException;

	/**
	 * This method loads all permissions from the backend.
	 *
	 * @throws PermissionBackendException
	 */
	public abstract void loadPermissions() throws PermissionBackendException;

	/**
	 * Commits any changes made to the permission node to the backend for saving
	 */
	public abstract void nodeCommit( Permission perm );

	/**
	 * Destroys the permission node and it's children, removing it from both the backend and memory.<br>
	 * <br>
	 * Warning: could be considered unsafe to destroy a permission node without first removing all child values
	 */
	public abstract void nodeDestroy( Permission perm );

	/**
	 * Disregards any changes made to the permission node and reloads from the backend
	 */
	public abstract void nodeReload( Permission perm );

	public abstract void reloadBackend() throws PermissionBackendException;

	/**
	 * Sets the default group
	 */
	public abstract void setDefaultGroup( String child, References refs );
}
