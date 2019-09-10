package com.marchnetworks.common.migration;

import com.marchnetworks.common.utils.DateUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Migration
{
	private static final Logger LOG = LoggerFactory.getLogger( MigrationService.class );
	static final DatabaseVersion TARGET_DATABASE_VERSION = DatabaseVersion.VERSION_2_6;

	private MigrationService migrationService;

	public void init()
	{
		DatabaseVersion currentVersion = migrationService.getVersion();

		if ( currentVersion != null )
		{
			LOG.info( "Current database version is {}", currentVersion );
		}
		else
		{
			currentVersion = DatabaseVersion.VERSION_1_6;
			LOG.info( "Database version was not found, defaulting to {}", currentVersion );
		}

		if ( currentVersion == TARGET_DATABASE_VERSION )
		{
			LOG.info( "Current database version is up to date, no migration to perform" );
			return;
		}

		DatabaseVersion lastSuccessVersion = currentVersion;

		try
		{
			while ( currentVersion.getValue() < TARGET_DATABASE_VERSION.getValue() )
			{
				currentVersion = DatabaseVersion.fromValue( currentVersion.getValue() + 1L );
				LOG.info( "Migrating database to version {}", currentVersion );

				long start = DateUtils.getCurrentUTCTimeInMillis();

				migrationService.migrateToVersion( currentVersion );

				lastSuccessVersion = currentVersion;
				long end = DateUtils.getCurrentUTCTimeInMillis();

				LOG.info( "Total migration time for {} was {} ms", currentVersion, Long.valueOf( end - start ) );
			}
		}
		finally
		{
			migrationService.setVersion( lastSuccessVersion );
		}
	}

	public void setMigrationService( MigrationService migrationService )
	{
		this.migrationService = migrationService;
	}
}
