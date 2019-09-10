package com.marchnetworks.command.api.migration;

import com.marchnetworks.command.api.app.AppCoreService;
import com.marchnetworks.command.api.initialization.InitializationListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MigrationService implements InitializationListener
{
	private static final Logger LOG = LoggerFactory.getLogger( MigrationService.class );
	private AppCoreService appCoreService;
	private String appId = getAppId();

	public void onAppInitialized()
	{
		init();
	}

	public void init()
	{
		Integer currentVersion = appCoreService.getCurrentVersion( appId );
		int targetVersion = getTargetVersion();
		if ( !upgraded( appId ) )
		{
			LOG.info( "The database is empty, no migration to perform" );
			appCoreService.setDatabaseVersion( appId, Integer.valueOf( targetVersion ) );
			return;
		}
		if ( currentVersion != null )
		{
			LOG.info( "Current database version for AppID {} is {}", appId, currentVersion );
		}
		else
		{
			currentVersion = Integer.valueOf( 0 );
			LOG.info( "Database version for AppID {} was not found, defaulting to version {}", appId, currentVersion );
		}
		if ( currentVersion.equals( Integer.valueOf( targetVersion ) ) )
		{
			LOG.info( "Current database version for AppID {} is up to date, no migration to perform", appId );
			return;
		}
		while ( currentVersion.intValue() < targetVersion )
		{
			Integer localInteger1 = currentVersion;
			Integer localInteger2 = currentVersion = Integer.valueOf( currentVersion.intValue() + 1 );
			LOG.info( "Migrating app database to version {}", currentVersion );
			migrateToVersion( currentVersion.intValue() );
			appCoreService.setDatabaseVersion( appId, currentVersion );
		}
	}

	private boolean upgraded( String appId )
	{
		return appCoreService.upgraded( appId );
	}

	public abstract String getAppId();

	public abstract int getTargetVersion();

	public abstract void migrateToVersion( int paramInt );

	public void setAppCoreService( AppCoreService appCoreService )
	{
		this.appCoreService = appCoreService;
	}
}
