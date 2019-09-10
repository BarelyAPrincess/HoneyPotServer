package com.marchnetworks.command.api.migration;

public abstract interface MigrationHandler
{
	public abstract void migrateToVersion( int paramInt );
}
