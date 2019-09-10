package com.marchnetworks.common.diagnostics.database;

import com.marchnetworks.command.common.diagnostics.IndexFragmentation;

import java.util.List;

public abstract interface SystemInfoService
{
	public abstract List<DatabaseSize> getDatabaseSize();

	public abstract List<IndexFragmentation> getCommandIndexFragmentation( List<String> paramList );
}
