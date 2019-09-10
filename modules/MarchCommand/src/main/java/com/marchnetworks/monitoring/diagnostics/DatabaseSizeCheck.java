package com.marchnetworks.monitoring.diagnostics;

import com.marchnetworks.command.api.alert.AlertDefinitionEnum;
import com.marchnetworks.common.config.ConfigProperty;
import com.marchnetworks.common.diagnostics.database.DatabaseSize;
import com.marchnetworks.common.diagnostics.database.SystemInfoService;
import com.marchnetworks.common.scheduling.PeriodicTransactionalTask;
import com.marchnetworks.health.input.AlertInput;
import com.marchnetworks.health.input.ServerAlertInput;
import com.marchnetworks.health.service.HealthServiceIF;
import com.marchnetworks.server.event.StateCacheService;
import com.marchnetworks.shared.config.CommonConfiguration;

import java.util.List;

public class DatabaseSizeCheck implements PeriodicTransactionalTask
{
	private static final long DATABASE_SIZE_LIMIT = 10000L;
	private SystemInfoService systemInfoService;
	private HealthServiceIF healthService;
	private CommonConfiguration configuration;
	private StateCacheService stateCacheService;
	private boolean clearedAlerts = false;

	public void handlePeriodicTransactionalTask()
	{
		if ( ( "external".equals( configuration.getProperty( ConfigProperty.DB_STATE ) ) ) || ( "true".equals( configuration.getProperty( ConfigProperty.DB_EXTERNAL_INSTALL ) ) ) )
		{
			return;
		}

		List<DatabaseSize> databaseSize = systemInfoService.getDatabaseSize();
		long total = 0L;
		for ( DatabaseSize info : databaseSize )
		{
			total += info.getDatabaseUsedSize();
		}

		if ( total > 9000.0D )
		{
			long percentage = ( long ) (total / 10000.0D * 100.0D);
			if ( percentage > 100L )
			{
				percentage = 100L;
			}
			AlertInput alert = new ServerAlertInput( AlertDefinitionEnum.DATABASE_SIZE, "CES", System.currentTimeMillis(), "", String.valueOf( percentage ), true );

			healthService.processHealthAlert( alert );

			DatabaseSizeStateEvent stateEvent = new DatabaseSizeStateEvent( ( int ) percentage );
			stateCacheService.putIntoCache( stateEvent );
			clearedAlerts = false;

		}
		else if ( !clearedAlerts )
		{
			AlertInput alert = new ServerAlertInput( AlertDefinitionEnum.DATABASE_SIZE, "CES", System.currentTimeMillis(), "", "", false );

			healthService.processHealthAlert( alert );

			stateCacheService.removeFromCache( new DatabaseSizeStateEvent() );
			clearedAlerts = true;
		}
	}

	public void setSystemInfoService( SystemInfoService systemInfoService )
	{
		this.systemInfoService = systemInfoService;
	}

	public void setHealthService( HealthServiceIF healthService )
	{
		this.healthService = healthService;
	}

	public void setConfiguration( CommonConfiguration configuration )
	{
		this.configuration = configuration;
	}

	public void setStateCacheService( StateCacheService stateCacheService )
	{
		this.stateCacheService = stateCacheService;
	}
}

