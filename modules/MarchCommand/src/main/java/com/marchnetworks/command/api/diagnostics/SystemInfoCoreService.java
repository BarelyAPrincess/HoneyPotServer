package com.marchnetworks.command.api.diagnostics;

import com.marchnetworks.command.common.diagnostics.IndexFragmentation;

import java.util.List;

public abstract interface SystemInfoCoreService
{
	public abstract List<IndexFragmentation> getAppsIndexFragmentation( List<String> paramList );

	public abstract void defragmentIndex( IndexFragmentation paramIndexFragmentation );
}
