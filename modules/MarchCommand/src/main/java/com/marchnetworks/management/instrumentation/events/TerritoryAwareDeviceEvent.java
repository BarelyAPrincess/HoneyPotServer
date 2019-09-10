package com.marchnetworks.management.instrumentation.events;

import com.marchnetworks.command.api.event.TerritoryAware;

import java.util.Set;

public abstract class TerritoryAwareDeviceEvent extends AbstractDeviceEvent implements TerritoryAware
{
	private Set<Long> territoryInfo;

	public TerritoryAwareDeviceEvent( String type, String deviceId, Set<Long> territoryInfo )
	{
		super( type, deviceId );
		this.territoryInfo = territoryInfo;
	}

	public Set<Long> getTerritoryInfo()
	{
		return territoryInfo;
	}
}

