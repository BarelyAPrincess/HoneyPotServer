package com.marchnetworks.common.event;

import com.marchnetworks.command.api.event.TerritoryAware;

import java.util.Set;

public abstract class AbstractTerritoryAwareEvent extends Event implements TerritoryAware
{
	private Set<Long> territoryInfo;

	public AbstractTerritoryAwareEvent()
	{
	}

	public AbstractTerritoryAwareEvent( String type )
	{
		super( type );
	}

	public AbstractTerritoryAwareEvent( String type, Set<Long> territoryInfo )
	{
		super( type );
		this.territoryInfo = territoryInfo;
	}

	public Set<Long> getTerritoryInfo()
	{
		return territoryInfo;
	}
}
