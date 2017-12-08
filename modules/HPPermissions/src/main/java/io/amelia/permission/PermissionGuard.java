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

import io.amelia.foundation.ConfigRegistry;
import io.amelia.events.EventDispatcher;
import io.amelia.events.EventHandler;
import io.amelia.events.EventPriority;
import io.amelia.foundation.RegistrarBase;
import io.amelia.foundation.binding.AppBindings;
import io.amelia.foundation.facades.interfaces.PermissionService;
import io.amelia.foundation.service.IPermission;
import io.amelia.foundation.service.IWhitelist;
import io.amelia.foundation.facades.FacadePriority;
import io.amelia.lang.EnumColor;
import io.amelia.logcompat.LogBuilder;
import io.amelia.logcompat.Logger;
import io.amelia.permission.backend.file.FileBackend;
import io.amelia.permission.backend.memory.MemoryBackend;
import io.amelia.permission.backend.sql.SQLBackend;
import io.amelia.permission.event.PermissibleEntityEvent;
import io.amelia.permission.event.PermissibleEvent;
import io.amelia.permission.event.PermissibleSystemEvent;
import io.amelia.permission.lang.PermissionBackendException;
import io.amelia.support.Objs;
import io.amelia.tasks.CallableTask;
import io.amelia.tasks.TaskDispatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PermissionGuard implements PermissionService
{
	public static final Logger L = LogBuilder.get( PermissionGuard.class );
	private static final String PERMISSION_PREFIX = "io.amelia.permissions";
	// private static final Set<Permission> permissions = new HashSet<>();
	static boolean allowOps = true;
	static boolean debugMode = false;
	private static PermissionBackend backend = null;
	private static Map<String, PermissibleGroup> defaultGroups = new HashMap<>();
	private static Map<String, PermissibleEntity> entities = new HashMap<>();
	private static Map<String, PermissibleGroup> groups = new HashMap<>();
	private static boolean hasWhitelist = false;
	private static RegExpMatcher matcher = null;
	private static Map<String, Set<String>> refInheritance = new ConcurrentHashMap<>();

	static
	{
		hasWhitelist = ConfigRegistry.getBoolean( "settings.whitelist" ).orElse( hasWhitelist );
		debugMode = ConfigRegistry.getBoolean( "permissions.debug" ).orElse( debugMode );
		allowOps = ConfigRegistry.getBoolean( "permissions.allowOps" ).orElse( allowOps );

		AppBindings.registerFacade( Whitelist.class, FacadePriority.LOWEST, PermissionGuard::new );
		AppBindings.registerFacade( Permissions.class, FacadePriority.LOWEST, PermissionGuard::new );

		// ServiceDispatcher.registerService( Permission.class, modular, modular, ServicePriority.Lowest );
		// ServiceDispatcher.registerService( PermissibleGroup.class, modular, modular, ServicePriority.Lowest );
		// ServiceDispatcher.registerService( PermissibleEntity.class, modular, modular, ServicePriority.Lowest );

		initBackend();
	}

	protected static void callEvent( PermissibleSystemEvent.Action action )
	{
		callEvent( new PermissibleSystemEvent( action ) );
	}

	protected static void callEvent( PermissibleEvent event )
	{
		EventDispatcher.callEvent( event );
	}

	/**
	 * Check if entity with name has permission in ref
	 *
	 * @param entityId   entity name
	 * @param permission permission as string to check against
	 * @param refs       References
	 * @return true on success false otherwise
	 */
	public static PermissionResult checkPermission( String entityId, String permission, String... refs )
	{
		PermissibleEntity entity = getEntity( entityId );

		if ( entity == null )
			throw new RuntimeException( "Entity returned null! This is a bug and needs to be reported to the developers." );

		return entity.checkPermission( permission, References.format( refs ) );
	}

	/**
	 * Check if specified entity has specified permission
	 *
	 * @param entity entity object
	 * @param perm   permission string to check against
	 * @return true on success false otherwise
	 */
	public static PermissionResult checkPermission( Permissible entity, String perm )
	{
		return checkPermission( entity.getId(), perm, "" );
	}

	public static Permission createNode( String namespace )
	{
		return createNode( namespace, PermissionType.DEFAULT );
	}

	/**
	 * Finds a registered permission node in the stack by crawling.
	 *
	 * @param namespace The full name space we need to crawl for.
	 * @param type      What PermissionType should the final node be
	 * @return The child node based on the namespace. Will return NULL if non-existent and createChildren is false.
	 */
	public static Permission createNode( String namespace, PermissionType type )
	{
		if ( isDebug() )
			L.info( EnumColor.YELLOW + "Created permission " + namespace + " as type " + type.name() );

		String[] nodes = namespace.split( "\\." );

		if ( nodes.length < 1 )
			return null;

		Permission curr = getRootNode( nodes[0] );

		if ( curr == null )
			curr = new Permission( nodes[0] );

		if ( nodes.length == 1 )
			return curr;

		boolean createdLast = false;

		for ( String node : Arrays.copyOfRange( nodes, 1, nodes.length ) )
		{
			Permission child = curr.getChild( node.toLowerCase() );
			if ( child == null )
			{
				child = new Permission( node, curr );
				curr.addChild( child );
				curr.commit();
				curr = child;
				createdLast = true;
			}
			else
			{
				curr = child;
				createdLast = false;
			}
		}

		// If the last node was created then we set it to the desired PermissionType
		if ( createdLast )
			curr.setType( type );

		return curr;
	}

	public static void end()
	{
		try
		{
			reset();
		}
		catch ( PermissionBackendException ignore )
		{
			// Ignore because we're shutting down so who cares
		}
	}

	/**
	 * Return current backend
	 *
	 * @return current backend object
	 */
	public static PermissionBackend getBackend()
	{
		return backend;
	}

	/**
	 * Set backend to specified backend. This would also cause backend resetting.
	 *
	 * @param backendName name of backend to set to
	 */
	public static void setBackend( String backendName ) throws PermissionBackendException
	{
		synchronized ( this )
		{
			reset();
			backend = PermissionBackend.getBackend( backendName );
			backend.initialize();

			loadData();
		}

		callEvent( PermissibleSystemEvent.Action.BACKEND_CHANGED );
	}

	public static PermissibleGroup getDefaultGroup()
	{
		return getDefaultGroup( null );
	}

	public static void setDefaultGroup( PermissibleGroup group )
	{
		setDefaultGroup( group, null );
	}

	/**
	 * Return default group object
	 *
	 * @return default group object. null if not specified
	 */
	public static PermissibleGroup getDefaultGroup( References refs )
	{
		String refIndex = ""; // refs != null ? refs : "";

		if ( !defaultGroups.containsKey( refIndex ) )
			defaultGroups.put( refIndex, getDefaultGroup( refs, getDefaultGroup( null, null ) ) );

		return defaultGroups.get( refIndex );
	}

	private static PermissibleGroup getDefaultGroup( References refs, PermissibleGroup fallback )
	{
		PermissibleGroup defaultGroup = backend.getDefaultGroup( refs );

		if ( defaultGroup == null && refs == null )
		{
			L.warning( "No default group defined. Use \"perm set default group <group> [ref]\" to define default group." );
			return fallback;
		}

		if ( defaultGroup != null )
			return defaultGroup;

		return fallback;
	}

	/**
	 * Return all registered entity objects
	 *
	 * @return PermissibleEntity array
	 */
	public static Set<PermissibleEntity> getEntities()
	{
		return new HashSet<>( entities.values() );
	}

	public static Set<PermissibleEntity> getEntities( String query )
	{
		return entities.values().stream().filter( e -> e.getId().toLowerCase().startsWith( query.toLowerCase() ) ).collect( Collectors.toSet() );
	}

	/**
	 * Finds entities assigned provided permission. WARNING: Will not return a complete list if permissions.preloadEntities config is false.
	 *
	 * @param perm The permission to check for.
	 * @return a list of permissibles that have that permission assigned to them.
	 */
	public static List<PermissibleEntity> getEntitiesWithPermission( Permission perm )
	{
		return entities.values().stream().filter( p -> p.checkPermission( perm ).isAssigned() ).collect( Collectors.toList() );
	}

	/**
	 * Finds entities assigned provided permission.
	 *
	 * @param perm The permission to check for.
	 * @return a list of permissibles that have that permission assigned to them.
	 * @see PermissionGuard#getEntitiesWithPermission(Permission)
	 */
	public static List<PermissibleEntity> getEntitiesWithPermission( String perm )
	{
		return getEntitiesWithPermission( getNode( perm ) );
	}

	public static PermissibleEntity getEntity( String id )
	{
		return getEntity( id, true );
	}

	public static PermissibleEntity getEntity( String id, boolean create )
	{
		Objs.notEmpty( id );

		if ( entities.containsKey( id ) )
			return entities.get( id );
		else if ( create )
		{
			PermissibleEntity entity = backend.getEntity( id );
			entities.put( id, entity );
			return entity;
		}
		else
			return null;
	}

	/**
	 * Return object for specified group
	 *
	 * @param id the group id
	 * @return PermissibleGroup object
	 */
	public static PermissibleGroup getGroup( String id )
	{
		return getGroup( id, true );
	}

	public static PermissibleGroup getGroup( String id, boolean create )
	{
		if ( id == null || id.isEmpty() )
			throw new IllegalArgumentException( "Null id passed!" );

		id = id.toLowerCase();

		if ( groups.containsKey( id ) )
			return groups.get( id );
		else if ( create )
		{
			PermissibleGroup group = backend.getGroup( id );
			groups.put( id, group );
			return group;
		}
		else
			return null;
	}

	/**
	 * Return all groups
	 *
	 * @return PermissibleGroup array
	 */
	public static List<PermissibleGroup> getGroups()
	{
		return new ArrayList<>( groups.values() );
	}

	public static List<PermissibleGroup> getGroups( String query )
	{
		return groups.values().stream().filter( g -> g.getId().toLowerCase().startsWith( query.toLowerCase() ) ).collect( Collectors.toList() );
	}

	public static RegExpMatcher getMatcher()
	{
		if ( matcher == null )
			matcher = new RegExpMatcher();
		return matcher;
	}

	/**
	 * Attempts to find a Permission Node. Will not create the node if non-existent.
	 *
	 * @param namespace The namespace to find, e.g., com.chiorichan.user
	 * @return The found permission, null if non-existent
	 */
	public static Permission getNode( String namespace )
	{
		String[] nodes = namespace.split( "\\." );

		if ( nodes.length < 1 )
			return null;

		Permission curr = getRootNode( nodes[0] );

		if ( curr == null )
			return null;

		if ( nodes.length == 1 )
			return curr;

		for ( String node : Arrays.copyOfRange( nodes, 1, nodes.length ) )
		{
			Permission child = curr.getChild( node.toLowerCase() );
			if ( child == null )
				return null;
			else
				curr = child;
		}

		return curr;
	}

	protected static Permission getNodeByLocalName( String name )
	{
		for ( Permission perm : permissions )
			if ( perm.getLocalName().equalsIgnoreCase( name ) )
				return perm;
		return null;
	}

	/**
	 * Finds registered permission nodes.
	 *
	 * @param ns The full name space we need to crawl for.
	 * @return A list of permissions that matched the namespace. Will return more then one if namespace contained asterisk.
	 */
	public static List<Permission> getNodes( PermissionNamespace ns )
	{
		if ( ns == null || ns.getNodeCount() < 1 )
			return new ArrayList<>();



		return permissions.stream().filter( p -> ns.matches( p ) ).collect( Collectors.toList() );
	}

	public static List<Permission> getNodes( String ns )
	{
		return getNodes( PermissionNamespace.parseString( ns ) );
	}

	public static Permission getPermission( String path )
	{
		return AppBindings.getReference( Permission::new, PERMISSION_PREFIX, path );
	}

	public static Set<String> getRefInheritance( String ref )
	{
		return refInheritance.containsKey( ref ) ? refInheritance.get( ref ) : new HashSet<>();
	}

	public static Set<String> getReferences()
	{
		return refInheritance.keySet();
	}

	protected static Permission getRootNode( String name )
	{
		for ( Permission perm : permissions )
			if ( perm.parent == null && perm.getLocalName().equalsIgnoreCase( name ) )
				return perm;
		return null;
	}

	public static List<Permission> getRootNodes()
	{
		return getRootNodes( true );
	}

	public static List<Permission> getRootNodes( boolean ignoreSysNode )
	{
		return permissions.stream().filter( p -> p.parent == null && !p.getNamespace().startsWith( "sys" ) && ignoreSysNode ).collect( Collectors.toList() );
	}

	public static boolean hasWhitelist()
	{
		return hasWhitelist;
	}

	private static void initBackend() throws PermissionBackendException
	{
		PermissionBackend.registerBackendAlias( "sql", SQLBackend.class );
		PermissionBackend.registerBackendAlias( "file", FileBackend.class );
		PermissionBackend.registerBackendAlias( "memory", MemoryBackend.class );

		String backendName = ConfigRegistry.getString( "permissions.backend" ).orElse( null );

		if ( Objs.isEmpty( backendName ) )
		{
			backendName = PermissionBackend.defaultBackend; // Default backend
			ConfigRegistry.setObject( "permissions.backend", backendName );
		}

		setBackend( backendName );
	}

	/**
	 * Return current state of debug mode
	 *
	 * @return true debug is enabled, false if disabled
	 */
	public static boolean isDebug()
	{
		return debugMode;
	}

	/**
	 * Set debug mode
	 *
	 * @param debug true enables debug mode, false disables
	 */
	public static void setDebug( boolean debug )
	{
		debugMode = debug;
		callEvent( PermissibleSystemEvent.Action.DEBUGMODE_TOGGLE );
	}

	/**
	 * Loads all groups and entities from the backend data source.
	 *
	 * @throws PermissionBackendException
	 */
	public static void loadData() throws PermissionBackendException
	{
		if ( isDebug() )
			L.warning( EnumColor.YELLOW + "Permission debug is enabled!" );

		groups.clear();
		entities.clear();

		if ( isDebug() )
			L.info( EnumColor.YELLOW + "Loading permissions from backend!" );

		backend.loadPermissions();
		PermissionDefault.initNodes();

		if ( isDebug() )
			L.info( EnumColor.YELLOW + "Loading groups from backend!" );
		backend.loadGroups();

		if ( isDebug() )
			L.info( EnumColor.YELLOW + "Loading entities from backend!" );
		backend.loadEntities();

		/*if ( debug() )
		{
			L.info( EnumColor.YELLOW + "Dumping loaded permissions:" );
			for ( Permission root : getRootNodes( false ) )
				root.debugPermissionStack( 0 );
		}*/

	}

	// TODO Make more checks
	@EventHandler( priority = EventPriority.HIGHEST )
	public static void onAccountLoginEvent( AccountPreLoginEvent event )
	{
		PermissibleEntity entity = getEntity( event.getAccount().getId() );

		if ( hasWhitelist() && entity.isWhitelisted() )
		{
			event.fail( AccountDescriptiveReason.ACCOUNT_NOT_WHITELISTED );
			return;
		}

		if ( entity.isBanned() )
		{
			event.fail( AccountDescriptiveReason.ACCOUNT_BANNED );
			return;
		}
	}

	/**
	 * Attempts to parse if a permission string is actually a reference to the EVERYBODY (-1, everybody, everyone), OP (0, op, root) or ADMIN (admin) permission nodes;
	 *
	 * @param perm The permission string to parse
	 * @return A string for the permission node, will return the original string if no match was found.
	 */
	public static String parseNode( String perm )
	{
		// Everyone
		if ( perm == null || perm.isEmpty() || perm.equals( "-1" ) || perm.equals( "everybody" ) || perm.equals( "everyone" ) )
			perm = PermissionDefault.EVERYBODY.getNamespace();

		// OP Only
		if ( perm.equals( "0" ) || perm.equalsIgnoreCase( "op" ) || perm.equalsIgnoreCase( "root" ) )
			perm = PermissionDefault.OP.getNamespace();

		if ( perm.equalsIgnoreCase( "admin" ) )
			perm = PermissionDefault.ADMIN.getNamespace();

		return perm;
	}

	/**
	 * Attempts to move a permission from one namespace to another. e.g., com.chiorichan.oldspace1.same.oldname -> com.chiorichan.newspace2.same.newname.
	 *
	 * @param newNamespace    The new namespace you wish to use.
	 * @param appendLocalName Pass true if you wish the method to append the LocalName to the new namespace. If the local name of the new namespace is different then this permission will be renamed.
	 * @return true if move/rename was successful.
	 */
	public static boolean refactorNamespace( String newNamespace, boolean appendLocalName )
	{
		// PermissionNamespace ns = getNamespaceObj();
		// TODO THIS!
		return false;
	}

	/**
	 * Register new timer task
	 *
	 * @param task  TimerTask object
	 * @param delay delay in seconds
	 */
	protected static void registerTask( CallableTask task, int delay )
	{
		TaskDispatcher.scheduleAsyncDelayedTask( RegistrarBase.INTERNAL, delay * 50, task );
	}

	public static void reload() throws PermissionBackendException
	{
		reset();
		backend.reloadBackend();

		backend.loadEntities();
		backend.loadGroups();

		hasWhitelist = ConfigRegistry.getBoolean( "settings.whitelist" ).orElse( false );
	}

	/**
	 * Reset all in-memory groups and entities, clean up runtime stuff, reloads backend
	 */
	public static void reset() throws PermissionBackendException
	{
		defaultGroups.clear();
		entities.clear();
		groups.clear();

		callEvent( PermissibleSystemEvent.Action.RELOADED );
	}

	/**
	 * Reset in-memory object of specified entity
	 *
	 * @param entity the entity
	 */
	public static void resetEntity( Permissible entity )
	{
		entities.remove( entity.getId() );
	}

	/**
	 * Reset in-memory object for groupName
	 *
	 * @param groupName group's name
	 */
	public static void resetGroup( String groupName )
	{
		groups.remove( groupName );
	}

	/**
	 * Forcefully saves groups and entities to the backend data source.
	 */
	public static void saveData()
	{
		AppBindings.getReference( Permission::new, PERMISSION_PREFIX, "" ).getChildren().forEach( Permission::commit );

		// for ( Permission p : permissions )
		// p.commit();

		for ( PermissibleGroup entity : groups.values() )
			entity.save();

		for ( PermissibleEntity entity : entities.values() )
			entity.save();
	}

	/**
	 * Set default group to specified group
	 *
	 * @param group PermissibleGroup group object
	 */
	public static void setDefaultGroup( PermissibleGroup group, References refs )
	{
		if ( group == null || group.equals( defaultGroups ) )
			return;

		backend.setDefaultGroup( group.getId(), refs );

		defaultGroups.clear();

		callEvent( PermissibleSystemEvent.Action.DEFAULTGROUP_CHANGED );
		callEvent( new PermissibleEntityEvent( group, PermissibleEntityEvent.Action.DEFAULTGROUP_CHANGED ) );
	}

	public static void setRefInheritance( String ref, Collection<String> heir )
	{
		Set<String> cur = getRefInheritance( ref );
		cur.addAll( heir );
		refInheritance.put( ref, cur );
	}

	public static void setWhitelist( boolean value )
	{
		hasWhitelist = value;
		ConfigRegistry.setObject( "settings.whitelist", value );
	}

	private PermissionGuard()
	{
		// Static Class
	}

	public static class PermissionService implements IPermission
	{
		public PermissionService()
		{

		}
	}

	/**
	 * Provides access control for logins
	 */
	public static class WhitelistService implements IWhitelist
	{
		public WhitelistService()
		{

		}
	}
}
