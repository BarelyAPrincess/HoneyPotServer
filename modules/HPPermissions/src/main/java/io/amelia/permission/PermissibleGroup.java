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

import io.amelia.permission.event.PermissibleEntityEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class PermissibleGroup extends PermissibleEntity implements Comparable<PermissibleGroup>
{
	private int rank = -1;
	private int weight = 0;

	public PermissibleGroup( String groupName )
	{
		super( groupName );
	}

	@Override
	public final int compareTo( PermissibleGroup o )
	{
		return getWeight() - o.getWeight();
	}

	// TODO THIS!!! New Ref Groups
	public Map<String, Collection<PermissibleGroup>> getAllParentGroups()
	{
		return new HashMap<>();
		// return Collections.unmodifiableMap( groups );
	}

	// TODO Prevent StackOverflow
	public Collection<PermissibleEntity> getChildEntities( boolean recursive, References refs )
	{
		List<PermissibleEntity> children = new ArrayList<>();
		for ( PermissibleEntity entity : PermissionGuard.getEntities() )
			if ( entity.getGroups( refs ).contains( this ) )
				children.add( entity );
		if ( recursive )
			for ( PermissibleGroup group : getChildGroups( true, refs ) )
				children.addAll( group.getChildEntities( true, refs ) );
		return children;
	}

	public Collection<PermissibleEntity> getChildEntities( References refs )
	{
		return getChildEntities( false, refs );
	}

	// TODO Prevent StackOverflow
	public Collection<PermissibleGroup> getChildGroups( boolean recursive, References refs )
	{
		List<PermissibleGroup> children = new ArrayList<>();
		for ( PermissibleGroup group : PermissionGuard.getGroups() )
			if ( group.getGroups( refs ).contains( this ) )
			{
				children.add( group );
				if ( recursive )
					children.addAll( group.getChildGroups( true, refs ) );
			}
		return children;
	}

	public Collection<PermissibleGroup> getChildGroups( References refs )
	{
		return getChildGroups( false, refs );
	}

	// XXX THIS TOO!
	public Map<String, String> getOptions()
	{
		return new HashMap<>();
	}

	public Collection<String> getParentGroupsNames( References refs )
	{
		Set<String> result = new HashSet<>();
		for ( PermissibleGroup group : getGroups( refs ) )
			result.add( group.getId() );
		return result;
	}

	public int getRank()
	{
		return rank;
	}

	public void setRank( int rank )
	{
		this.rank = rank;
	}

	public String getRankLadder()
	{
		return null;// TODO Auto-generated method stub
	}

	public void setRankLadder( String rank )
	{
		// TODO Auto-generated method stub
	}

	public final int getWeight()
	{
		return weight;
	}

	public final void setWeight( int weight )
	{
		this.weight = weight;
		PermissionGuard.callEvent( new PermissibleEntityEvent( this, PermissibleEntityEvent.Action.WEIGHT_CHANGED ) );
	}

	public boolean isRanked()
	{
		return rank >= 0;
	}

	public void setDefault( boolean isDef )
	{
		// TODO Auto-generated method stub
	}
}
