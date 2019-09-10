package com.marchnetworks.common.diagnostics.database;

import com.marchnetworks.command.common.diagnostics.IndexFragmentation;

import java.util.List;

public abstract interface SystemInfoDAO
{
	public abstract List<DatabaseSize> getDatabaseSize();

	public abstract List<IndexFragmentation> getIndexFragmentation( String paramString1, String paramString2 );

	public abstract void reorgIndexes( String paramString1, String paramString2, String... paramVarArgs );

	public abstract void rebuildIndexes( String paramString1, String paramString2, String... paramVarArgs );
}
