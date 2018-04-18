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

import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.amelia.events.Events;
import io.amelia.permission.event.PermissibleEntityEvent;
import io.amelia.permission.lang.PermissionException;
import io.amelia.support.DateAndTime;
import io.amelia.support.EnumColor;
import io.amelia.support.NamespaceBase;
import io.amelia.support.Objs;
import io.amelia.support.Pair;

public abstract class PermissibleEntity
{
	private final Map<String, PermissionResult> cachedResults = new ConcurrentHashMap<>();
	private final Map<PermissibleGroup, References> groups = new ConcurrentHashMap<>();
	private final Map<ChildPermission, References> permissions = new ConcurrentHashMap<>();
	private final Map<PermissibleGroup, TimedReferences> timedGroups = new ConcurrentHashMap<>();
	private final Map<ChildPermission, TimedReferences> timedPermissions = new ConcurrentHashMap<>();
	protected boolean debugMode = false;
	private String id;
	private boolean virtual = false;

	public PermissibleEntity( String id )
	{
		if ( PermissionGuard.isDebug() )
			PermissionGuard.L.info( String.format( "%sThe %s `%s` has been created.", EnumColor.YELLOW, isGroup() ? "group" : "entity", id ) );

		this.id = id;
		reload();
	}

	public void addGroup( PermissibleGroup group, References refs )
	{
		addGroup0( group, refs );
		recalculatePermissions();
	}

	protected void addGroup0( PermissibleGroup group, References refs )
	{
		References ref = groups.get( group );
		if ( ref == null )
			ref = refs;
		else
			ref.add( refs );
		groups.put( group, ref );
		removeTimedGroup( group, ref );

		if ( isDebug() )
			PermissionGuard.L.info( String.format( "%sThe group `%s` with reference `%s` was attached to entity `%s`.", EnumColor.YELLOW, group.getId(), refs.join(), getId() ) );
	}

	protected final void addPermission( ChildPermission perm, References refs )
	{
		Objs.notNull( perm );

		if ( checkPermission( perm.getPermission() ).isAssigned() )
		{
			if ( isDebug() )
				PermissionGuard.L.info( String.format( "%sThe permission `%s` with reference `%s` is already attached to entity `%s`.", EnumColor.YELLOW, perm.getPermission().getNamespace(), refs == null ? "null" : refs.join(), getId() ) );
			return;
		}

		if ( refs == null )
			refs = References.format();
		References oldRefs = getPermissionReferences( perm.getPermission() );

		if ( oldRefs != null )
			refs.add( oldRefs );
		permissions.put( perm, refs );

		if ( isDebug() )
			PermissionGuard.L.info( String.format( "%sThe permission `%s` with reference `%s` was attached to entity `%s`.", EnumColor.YELLOW, perm.getPermission().getNamespace(), refs.join(), getId() ) );

		recalculatePermissions();
	}

	public void addPermission( Permission perm, Object val, References refs )
	{
		addPermission( new ChildPermission( this, perm, perm.getModel().createValue( val ), isGroup() ? ( ( PermissibleGroup ) this ).getWeight() : -1 ), refs );
	}

	public void addPermission( String node, Object val, References refs )
	{
		Permission perm = PermissionGuard.createNode( node );
		if ( perm == null )
			throw new PermissionException( String.format( "The permission node %s is non-existent, you must create it first.", node ) );
		addPermission( perm, val, refs );
	}

	public void addTimedGroup( PermissibleGroup group, int lifetime, References refs )
	{
		if ( refs == null )
			refs = References.format();
		timedGroups.put( group, new TimedReferences( lifetime ).add( refs ) );
	}

	protected final void addTimedPermission( ChildPermission perm, TimedReferences refs )
	{
		permissions.put( perm, refs );
		if ( isDebug() )
			PermissionGuard.L.info( String.format( "%sThe permission `%s` with reference `%s` was attached to entity `%s`.", EnumColor.YELLOW, perm.getPermission().getNamespace(), refs.toString(), getId() ) );
		recalculatePermissions();
	}

	/**
	 * Adds timed permission with specified references and a lifetime to live
	 *
	 * @param perm     The Permission Node
	 * @param val      The custom permission value
	 * @param refs     The References
	 * @param lifeTime Lifetime of permission in seconds. 0 for transient permission (reference disappear only after server reload)
	 */
	public void addTimedPermission( final Permission perm, Object val, References refs, int lifeTime )
	{
		addTimedPermission( new ChildPermission( this, perm, perm.getModel().createValue( val ), isGroup() ? ( ( PermissibleGroup ) this ).getWeight() : -1 ), new TimedReferences( lifeTime ).add( refs ) );
	}

	public void addTimedPermission( String perm, Object val, References refs, int lifeTime )
	{
		addTimedPermission( PermissionGuard.createNode( perm ), val, refs, lifeTime );
	}

	public PermissionResult checkPermission( Permission perm )
	{
		return checkPermission( perm, References.format( "" ) );
	}

	public PermissionResult checkPermission( Permission perm, References refs )
	{
		Objs.notNull( perm );
		Objs.notNull( refs );

		/*
		 * We execute the results to reduce lag when a permission is checked multiple times over.
		 */
		PermissionResult result = cachedResults.get( perm.getNamespace() + "-" + refs.hash() );

		if ( result != null )
			if ( result.epoch > DateAndTime.epoch() - 600 ) // 600 Seconds = 10 Minutes
				return result;
			else
				cachedResults.remove( perm.getNamespace() + "-" + refs.hash() );

		result = new PermissionResult( this, perm, refs );

		cachedResults.put( perm.getNamespace() + "-" + refs.hash(), result );

		if ( isDebug() && !perm.getNamespace().equals( PermissionDefault.OP.getNamespace() ) )
			PermissionGuard.L.info( EnumColor.YELLOW + "Entity `" + getId() + "` checked for permission `" + perm.getNamespace() + "`" + ( refs.isEmpty() ? "" : " with reference `" + refs.toString() + "`" ) + " with result `" + result + "`" );

		return result;
	}

	public PermissionResult checkPermission( String perm )
	{
		return checkPermission( perm, References.format( "" ) );
	}

	public PermissionResult checkPermission( String perm, References ref )
	{
		perm = PermissionGuard.parseNode( perm );
		Permission permission = PermissionGuard.createNode( perm );
		PermissionResult result = checkPermission( permission, ref );

		return result;
	}

	protected void clearGroups()
	{
		groups.clear();
		recalculatePermissions();
	}

	protected void clearPermissions()
	{
		permissions.clear();
		recalculatePermissions();
	}

	protected void clearTimedGroups()
	{
		timedGroups.clear();
		recalculatePermissions();
	}

	protected void clearTimedPermissions()
	{
		timedPermissions.clear();
		recalculatePermissions();
	}

	public PermissibleGroup demote( PermissibleEntity demoter, String str )
	{
		return null;// TODO Auto-generated method stub
	}

	public boolean explainExpression( String expression )
	{
		if ( expression == null || expression.isEmpty() )
			return false;

		return !expression.startsWith( "-" ); // If expression have - (minus) before then that mean expression are negative
	}

	protected ChildPermission getChildPermission( Permission perm, References refs )
	{
		return getChildPermissions( refs ).filter( c -> c.getPermission() == perm ).findFirst().orElse( null );
		// Loader.L.debug( "Get PermissionChild on " + ( isGroup() ? "group" : "entity" ) + " " + getId() + " with result " + ( result != null ) );
	}

	protected Entry<ChildPermission, References> getChildPermissionEntry( Permission perm, References refs )
	{
		for ( Entry<ChildPermission, References> entry : permissions.entrySet() )
			if ( entry.getKey().getPermission() == perm && entry.getValue().match( refs ) )
				return entry;
		return null;
	}

	protected Entry<ChildPermission, References> getChildPermissionEntry( References refs )
	{
		for ( Entry<ChildPermission, References> entry : permissions.entrySet() )
			if ( entry.getValue().match( refs ) )
				return entry;
		return null;
	}

	/**
	 * Check it's self and each {@link PermissibleEntity} group until it finds the {@link ChildPermission} associated with {@link Permission}
	 *
	 * @param perm The {@link Permission} we associate with
	 * @param refs Reference to be looking for
	 *
	 * @return The resulting {@link ChildPermission}
	 */
	protected ChildPermission getChildPermissionRecursive( Permission perm, References refs )
	{
		/**
		 * Used as a constant tracker for already checked groups, prevents infinite looping. e.g., User -> Group1 -> Group2 -> Group3 -> Group1
		 */
		return getChildPermissionRecursive( new HashSet<>(), perm, refs );
	}

	protected ChildPermission getChildPermissionRecursive( Set<PermissibleGroup> stacker, Permission perm, References refs )
	{
		// First we try checking this PermissibleEntity
		ChildPermission result = getChildPermission( perm, refs );

		if ( result != null )
			return result;

		// Next we check each group recursively
		for ( PermissibleGroup group : getGroups( refs ) )
			if ( !stacker.contains( group ) )
			{
				stacker.add( group );
				result = group.getChildPermissionRecursive( stacker, perm, refs );
				if ( result != null )
					break;
			}

		return result;
	}

	protected Stream<ChildPermission> getChildPermissions( References refs )
	{
		return permissions.entrySet().stream().filter( e -> e.getValue().match( refs ) ).map( Entry::getKey );
	}

	public Map<PermissibleGroup, References> getGroupEntrys( References refs )
	{
		return groups.entrySet().stream().filter( e -> e.getValue().match( refs ) ).collect( Collectors.toMap( Entry::getKey, Entry::getValue ) );
	}

	public Collection<String> getGroupNames( References refs )
	{
		List<String> result = new ArrayList<>();
		for ( PermissibleGroup group : getGroups( refs ) )
			result.add( group.getId() );
		return result;
	}

	public References getGroupReferences()
	{
		References refs = new References();
		for ( References ref : timedGroups.values() )
			refs.add( ref );
		for ( References ref : groups.values() )
			refs.add( ref );
		return refs;
	}

	public References getGroupReferences( PermissibleGroup group )
	{
		References refs = new References();
		for ( Entry<PermissibleGroup, References> entry : groups.entrySet() )
			if ( entry.getKey() == group )
				refs.add( entry.getValue() );
		for ( Entry<PermissibleGroup, TimedReferences> entry : timedGroups.entrySet() )
			if ( entry.getKey() == group )
				refs.add( entry.getValue() );
		return refs;
	}

	public final List<PermissibleGroup> getGroups( References refs )
	{
		return Stream.concat( groups.entrySet().stream().filter( e -> e.getValue().match( refs ) ), timedGroups.entrySet().stream().filter( e -> e.getValue().match( refs ) && !e.getValue().isExpired() ) ).map( Entry::getKey ).collect( Collectors.toList() );
	}

	/**
	 * Return id of permission entity (Entity or Group) User should be equal to User's id on the server
	 *
	 * @return id
	 */
	public String getId()
	{
		return id;
	}

	private String getMatchingExpression( Collection<Permission> permissions, String permission )
	{
		for ( Permission exp : permissions )
			if ( PermissionGuard.getMatcher().isMatches( exp, permission ) )
				return exp.getNamespace().toString();
		return null;
	}

	public String getMatchingExpression( String permission, References refs )
	{
		return getMatchingExpression( getPermissions( refs ).keySet(), permission );
	}

	public <T> T getOption( String key, References refs, T def )
	{
		return def;// TODO Auto-generated method stub
	}

	public Map<String, String> getOptions( References refs )
	{
		return new HashMap<>(); // TODO Auto-generated method stub
	}

	public List<String> getPermissionNames( References refs )
	{
		return getPermissions( refs ).keySet().stream().map( Permission::getNamespace ).map( NamespaceBase::toString ).collect( Collectors.toList() );
	}

	public References getPermissionReferences()
	{
		return new References().addAll( timedPermissions.values() ).addAll( permissions.values() );
	}

	public References getPermissionReferences( Permission perm )
	{
		return Stream.concat( permissions.entrySet().stream(), timedPermissions.entrySet().stream() ).filter( e -> e.getKey().getPermission() == perm ).map( Entry::getValue ).collect( References.collector() );
	}

	public Pair<Permission, PermissionValue> getPermissionValue( Permission perm, References refs )
	{
		for ( Entry<ChildPermission, References> entry : permissions.entrySet() )
			if ( entry.getKey().getPermission() == perm && entry.getValue().match( refs ) )
				return new Pair<>( entry.getKey().getPermission(), entry.getKey().getValue() );
		for ( Entry<ChildPermission, TimedReferences> entry : timedPermissions.entrySet() )
			if ( !entry.getValue().isExpired() && entry.getKey().getPermission() == perm && entry.getValue().match( refs ) )
				return new Pair<>( entry.getKey().getPermission(), entry.getKey().getValue() );
		return null;
	}

	public Map<Permission, PermissionValue> getPermissionValues( References refs )
	{
		return Stream.concat( permissions.entrySet().stream(), timedPermissions.entrySet().stream().filter( e -> e.getValue().isExpired() ) ).filter( e -> e.getValue().match( refs ) ).collect( Collectors.toMap( e -> e.getKey().getPermission(), e -> e.getKey().getValue() ) );
	}

	public Map<Permission, References> getPermissions( References refs )
	{
		return permissions.entrySet().stream().filter( e -> e.getValue().match( refs ) ).collect( Collectors.toMap( e -> e.getKey().getPermission(), Entry::getValue ) );
	}

	public String getPrefix()
	{
		return null;
	}

	public void setPrefix( String prefix )
	{
		// TODO Auto-generated method stub
	}

	public String getPrefix( References refs )
	{
		return null;// TODO Auto-generated method stub
	}

	public String getSuffix()
	{
		return null;
	}

	public String getSuffix( References refs )
	{
		return null;// TODO Auto-generated method stub
	}

	/**
	 * Returns remaining lifetime of specified permission in ref
	 *
	 * @param perm Name of permission
	 * @param refs The permission references
	 *
	 * @return remaining lifetime in seconds of timed permission. 0 if permission is transient
	 */
	public long getTimedPermissionLifetime( Permission perm, References refs )
	{
		for ( Entry<ChildPermission, TimedReferences> entry : timedPermissions.entrySet() )
			if ( entry.getValue().match( refs ) && entry.getKey().getPermission() == perm )
				return DateAndTime.epoch() - entry.getValue().lifeTime;
		return -1;
	}

	public Collection<Permission> getTimedPermissions()
	{
		return getTimedPermissions( null );
	}

	/**
	 * Return entity timed (temporary) permission
	 *
	 * @param refs The Reference to check
	 *
	 * @return Collection of timed permissions
	 */
	public Collection<Permission> getTimedPermissions( References refs )
	{
		Set<Permission> result = new HashSet<>();
		for ( Entry<ChildPermission, TimedReferences> entry : timedPermissions.entrySet() )
			if ( entry.getValue().match( refs ) && !entry.getValue().isExpired() )
				result.add( entry.getKey().getPermission() );
		return result;
	}

	public boolean hasGroup( PermissibleGroup group )
	{
		return groups.containsKey( group );
	}

	public boolean hasTimedGroup( PermissibleGroup group )
	{
		return timedGroups.containsKey( group );
	}

	public boolean isAdmin()
	{
		PermissionResult result = checkPermission( PermissionDefault.ADMIN.getNode() );
		return result.isTrue();
	}

	public boolean isAdminOnly()
	{
		PermissionResult result = checkPermission( PermissionDefault.ADMIN.getNode() );
		return result.isTrue( false );
	}

	public boolean isBanned()
	{
		// You can't ban an OP entity, unless OPs are disabled.
		if ( PermissionGuard.allowOps && isOp() )
			return false;

		PermissionResult result = checkPermission( PermissionDefault.BANNED.getNode() );
		return result.isTrue();
	}

	public void setBanned( boolean banned )
	{
		PermissionResult result = checkPermission( PermissionDefault.BANNED.getNode() );
		if ( banned )
			result.assign();
		else
			result.unassign();
	}

	public boolean isCommitted()
	{
		// XXX Future, was it committed to backend?
		return true;
	}

	public boolean isDebug()
	{
		return debugMode || PermissionGuard.isDebug();
	}

	public void setDebug( boolean debug )
	{
		debugMode = debug;
	}

	public final boolean isGroup()
	{
		return this instanceof PermissibleGroup;
	}

	public boolean isNoneEntity()
	{
		return "none".equalsIgnoreCase( getId() );
	}

	public boolean isOp()
	{
		PermissionResult result = checkPermission( PermissionDefault.OP.getNode() );
		return result.isTrue( false ) || isRootEntity();
	}

	public boolean isRootEntity()
	{
		return "root".equalsIgnoreCase( getId() );
	}

	public final boolean isVirtual()
	{
		return virtual;
	}

	public void setVirtual( boolean virtual )
	{
		this.virtual = virtual;
	}

	public boolean isWhitelisted()
	{
		if ( isBanned() )
			return false;

		if ( !PermissionGuard.hasWhitelist() || PermissionGuard.allowOps && isOp() || isAdminOnly() || isNoneEntity() )
			return true;

		PermissionResult result = checkPermission( PermissionDefault.WHITELISTED.getNode() );
		return result.isTrue();
	}

	public void setWhitelisted( boolean whitelist )
	{
		PermissionResult result = checkPermission( PermissionDefault.WHITELISTED.getNode() );
		if ( whitelist )
			result.assign();
		else
			result.unassign();
	}

	public PermissibleGroup promote( PermissibleEntity promoter, String ladder )
	{
		return null;
	}

	public void recalculatePermissions()
	{
		for ( Entry<PermissibleGroup, TimedReferences> entry : timedGroups.entrySet() )
			if ( entry.getValue().isExpired() )
			{
				timedGroups.remove( entry.getKey() );
				Events.callEvent( new PermissibleEntityEvent( this, PermissibleEntityEvent.Action.TIMEDGROUP_EXPIRED ) );
			}
		for ( Entry<ChildPermission, TimedReferences> entry : timedPermissions.entrySet() )
			if ( entry.getValue().isExpired() )
			{
				timedPermissions.remove( entry.getKey() );
				Events.callEvent( new PermissibleEntityEvent( this, PermissibleEntityEvent.Action.TIMEDPERMISSION_EXPIRED ) );
			}
		for ( PermissionResult cache : cachedResults.values() )
			cache.recalculatePermissions();
		Events.callEvent( new PermissibleEntityEvent( this, PermissibleEntityEvent.Action.PERMISSIONS_CHANGED ) );
	}

	public void reload()
	{
		if ( isVirtual() )
			return;

		if ( isDebug() )
			PermissionGuard.L.info( EnumColor.YELLOW + "Entity '" + getId() + "' being reloaded from backend" );

		reloadGroups();
		reloadPermissions();
		recalculatePermissions();
	}

	/**
	 * Reload entity group references from backend
	 */
	public abstract void reloadGroups();

	/**
	 * Reload entity permissions from backend
	 */
	public abstract void reloadPermissions();

	/**
	 * Remove entity permission and group references from backend
	 */
	public abstract void remove();

	public final void removeAllPermissions()
	{
		permissions.clear();
		recalculatePermissions();
	}

	public void removeGroup( PermissibleGroup group, References refs )
	{
		if ( removeRefs( groups.get( group ), refs ) )
			groups.remove( group );
	}

	public void removeGroup( String group, References refs )
	{
		removeGroup( PermissionGuard.getGroup( group ), refs );
	}

	public final void removePermission( Permission perm, References refs )
	{
		for ( Entry<ChildPermission, References> entry : permissions.entrySet() )
			if ( entry.getKey().getPermission() == perm && entry.getValue().match( refs ) )
				permissions.remove( perm );
		recalculatePermissions();
	}

	public void removePermission( String permission, References refs )
	{
		removePermission( PermissionGuard.createNode( permission ), refs );
	}

	private boolean removeRefs( @NotNull References current, @NotNull References refs )
	{
		if ( current == null )
			return false;
		current.remove( refs );
		recalculatePermissions();
		return current.isEmpty();
	}

	/**
	 * Removes specified timed permission for references
	 * <p>
	 *
	 * @param group The PermissibleGroup
	 * @param refs  The references
	 */
	public void removeTimedGroup( PermissibleGroup group, References refs )
	{
		if ( removeRefs( timedGroups.get( group ), refs ) )
			timedGroups.remove( group );
	}

	public void removeTimedPermission( Permission perm, References refs )
	{
		for ( Entry<ChildPermission, TimedReferences> entry : timedPermissions.entrySet() )
			if ( entry.getKey().getPermission() == perm && entry.getValue().match( refs ) )
				timedPermissions.remove( entry.getKey() );
	}

	public void removeTimedPermission( String perm, References refs )
	{
		removeTimedPermission( PermissionGuard.createNode( perm ), refs );
	}

	/**
	 * Save entity data to backend
	 */
	public abstract void save();

	public void setGroups( Collection<PermissibleGroup> groups, References refs )
	{
		setGroups0( groups, refs );
		recalculatePermissions();
	}

	protected void setGroups0( Collection<PermissibleGroup> groups, References refs )
	{
		clearGroups();
		for ( PermissibleGroup group : groups )
			addGroup0( group, refs );
	}

	public void setOption( String key, String value, References ref )
	{
		// TODO Auto-generated method stub
	}

	public void setPrefix( String prefix, References ref )
	{
		// TODO Auto-generated method stub
	}

	public void setSuffix( String string, References refs )
	{
		// TODO Auto-generated method stub
	}

	@Override
	public String toString()
	{
		return this.getClass().getSimpleName() + "{" + getId() + "}";
	}
}
