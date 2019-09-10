package com.marchnetworks.common.migration;

import com.marchnetworks.command.api.migration.MigrationDAO;

public abstract interface CoreMigrationDAO extends MigrationDAO
{
	public abstract void updateGenericStorage( Object[] paramArrayOfObject );

	public abstract void explicitColumnConversion( String paramString1, String paramString2, String paramString3 );

	public abstract void updateLoginCacheUsernames();

	public abstract void migrateOldAuditLogs();

	public abstract void updateChannelResourceNames();

	public abstract void migrateLoginCache();

	public abstract void migrateMemberViews();

	public abstract void migrateProfileRights();

	public abstract void migrateTerritoryToSchedules();
}
