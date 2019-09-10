package com.marchnetworks.command.api.event;

import java.util.Set;

public abstract interface TerritoryAware
{
	public abstract Set<Long> getTerritoryInfo();
}
