package com.marchnetworks.monitoring.diagnostics;

import com.marchnetworks.command.api.alert.AlertDefinitionEnum;
import com.marchnetworks.command.api.initialization.InitializationListener;
import com.marchnetworks.command.common.scheduling.TaskScheduler;
import com.marchnetworks.command.common.transport.data.Pair;
import com.marchnetworks.common.config.ConfigProperty;
import com.marchnetworks.common.diagnostics.DiagnosticError;
import com.marchnetworks.health.input.AlertInput;
import com.marchnetworks.health.input.ServerAlertInput;
import com.marchnetworks.health.service.HealthServiceIF;
import com.marchnetworks.management.instrumentation.DeviceService;
import com.marchnetworks.monitoring.diagnostics.event.TimeSyncDisabledStateEvent;
import com.marchnetworks.server.event.StateCacheService;
import com.marchnetworks.shared.config.CommonConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class SystemTimeDiagnosticsServiceImpl implements SystemTimeDiagnosticsService, InitializationListener
{
	private static final Logger LOG = LoggerFactory.getLogger( SystemTimeDiagnosticsServiceImpl.class );

	private static long TIME_CHECK_FREQUENCY = 15000L;
	private static String EXECUTOR_ID = SystemTimeDiagnosticsServiceImpl.class.getSimpleName();

	private long lastSystemTime;

	private boolean hasTimeJumped;
	private TaskScheduler taskScheduler;
	private HealthServiceIF healthService;
	private DiagnosticsServiceIF diagnosticsService;
	private CommonConfiguration commonConfiguration;
	private StateCacheService stateCacheService;
	private DeviceService deviceService;
	private ScheduledFuture<?> timeCheckTask;

	public void onAppInitialized()
	{
		lastSystemTime = System.currentTimeMillis();

		timeCheckTask = taskScheduler.scheduleFixedPoolAtFixedRate( new SystemTimeChecker(), EXECUTOR_ID, 1, TIME_CHECK_FREQUENCY, TIME_CHECK_FREQUENCY, TimeUnit.MILLISECONDS );

		boolean isFeatureEnabled = commonConfiguration.getBooleanProperty( ConfigProperty.DEVICE_TIME_SYNC_FEATURE, true );
		if ( !isFeatureEnabled )
		{
			hasTimeJumped = true;
			stateCacheService.putIntoCache( new TimeSyncDisabledStateEvent() );
		}
		else
		{
			AlertInput alertInput = new ServerAlertInput( AlertDefinitionEnum.SERVER_TIME, "CES", lastSystemTime + TIME_CHECK_FREQUENCY, "", "", false );

			healthService.processHealthAlert( alertInput );
		}
	}

	public long getSystemTime()
	{
		if ( hasTimeJumped )
		{
			return -1L;
		}
		return System.currentTimeMillis();
	}

	private void checkSystemTime()
	{
		long rawTimeDiff = System.currentTimeMillis() - lastSystemTime - TIME_CHECK_FREQUENCY;
		long timeDiff = Math.abs( rawTimeDiff );

		LOG.debug( "Elapsed time since last check was {} ms", Long.valueOf( rawTimeDiff ) );

		if ( timeDiff > 300000L )
		{
			LOG.info( "Time jump of {} ms detected.", Long.valueOf( timeDiff ) );

			AlertInput alertInput = new ServerAlertInput( AlertDefinitionEnum.SERVER_TIME, "CES", lastSystemTime + TIME_CHECK_FREQUENCY, "", String.valueOf( timeDiff / 1000L ), true );

			if ( timeDiff >= 3600000L )
			{
				hasTimeJumped = true;
				taskScheduler.cancelFixedPoolSchedule( EXECUTOR_ID, timeCheckTask );

				Pair pair = new Pair( "feature", "disabled" );
				String infoString = AlertInput.pairsToString( new Pair[] {pair} );
				alertInput.setInfo( infoString );

				commonConfiguration.setProperty( ConfigProperty.DEVICE_TIME_SYNC_FEATURE, "false" );
			}

			deviceService.massUpdateTimeDelta( -rawTimeDiff );

			healthService.processHealthAlert( alertInput );

			diagnosticsService.notifyFailure( DiagnosticError.TIME_JUMP, "Time jump detected, check system clock" );
		}

		lastSystemTime = System.currentTimeMillis();
	}

	private class SystemTimeChecker implements Runnable
	{
		private SystemTimeChecker()
		{
		}

		public void run()
		{
			SystemTimeDiagnosticsServiceImpl.this.checkSystemTime();
		}
	}

	public void setTaskScheduler( TaskScheduler taskScheduler )
	{
		this.taskScheduler = taskScheduler;
	}

	public void setHealthService( HealthServiceIF healthService )
	{
		this.healthService = healthService;
	}

	public void setDiagnosticsService( DiagnosticsServiceIF diagnosticsService )
	{
		this.diagnosticsService = diagnosticsService;
	}

	public void setCommonConfiguration( CommonConfiguration commonConfiguration )
	{
		this.commonConfiguration = commonConfiguration;
	}

	public void setStateCacheService( StateCacheService stateCacheService )
	{
		this.stateCacheService = stateCacheService;
	}

	public void setDeviceService( DeviceService deviceService )
	{
		this.deviceService = deviceService;
	}
}

