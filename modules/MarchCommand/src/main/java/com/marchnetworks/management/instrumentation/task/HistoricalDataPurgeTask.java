package com.marchnetworks.management.instrumentation.task;

import com.marchnetworks.alarm.service.AlarmService;
import com.marchnetworks.audit.service.AuditLogService;
import com.marchnetworks.common.config.ConfigProperty;
import com.marchnetworks.health.service.HealthServiceIF;
import com.marchnetworks.management.instrumentation.DeviceService;
import com.marchnetworks.shared.config.CommonConfiguration;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HistoricalDataPurgeTask
{
	private static final long DEFAULT_CLEAN_AGE = 365L;
	private static final long DEFAULT_AUDIT_CLEAN_AGE = 182L;
	private static final Logger LOG = LoggerFactory.getLogger( HistoricalDataPurgeTask.class );

	private CommonConfiguration configuration;
	private HealthServiceIF healthService;
	private AlarmService alarmService;
	private DeviceService deviceService;
	private AuditLogService auditLogService;

	public void purgeOldData()
	{
		long cleanAlertsAge = getCleanAgeValue( ConfigProperty.ALERT_PURGE_MAX_AGE, 365L );
		long cleanAlarmsAge = getCleanAgeValue( ConfigProperty.ALARM_PURGE_MAX_AGE, 365L );
		try
		{
			healthService.purgeOldAlerts( Long.valueOf( cleanAlertsAge ) );
		}
		catch ( Exception e )
		{
			LOG.error( "Error during purging of alerts", e );
		}
		try
		{
			alarmService.purgeOldAlarms( cleanAlarmsAge );
		}
		catch ( Exception e )
		{
			LOG.error( "Error during purging of alarm entries", e );
		}

		try
		{
			List<Long> referencedDeletedDevices = healthService.getReferencedDeletedDevices();
			List<Long> referencedFromAlarms = alarmService.getReferencedDeletedDevices();
			referencedDeletedDevices.addAll( referencedFromAlarms );

			deviceService.removeDeletedDevices( referencedDeletedDevices );
		}
		catch ( Exception e )
		{
			LOG.error( "Error during purging of deleted devices", e );
		}

		long cleanAuditLogsAge = getCleanAgeValue( ConfigProperty.AUDIT_LOG_MAX_AGE, 182L );
		try
		{
			auditLogService.purgeOldAuditLogs( cleanAuditLogsAge );
		}
		catch ( Exception e )
		{
			LOG.error( "Error during purge of Audit entries", e );
		}
	}

	private long getCleanAgeValue( ConfigProperty property, long defaultValue )
	{
		long cleanAge = defaultValue;
		String propertyValue = configuration.getProperty( property );
		if ( ( propertyValue == null ) || ( propertyValue.isEmpty() ) )
		{
			return cleanAge;
		}
		try
		{
			cleanAge = Long.parseLong( propertyValue );
		}
		catch ( NumberFormatException nfe )
		{
			LOG.info( "Found invalid setting in configuration file for property {}", property.name() );
		}
		return cleanAge;
	}

	public void setConfiguration( CommonConfiguration configuration )
	{
		this.configuration = configuration;
	}

	public void setHealthService( HealthServiceIF healthService )
	{
		this.healthService = healthService;
	}

	public void setAlarmService( AlarmService alarmService )
	{
		this.alarmService = alarmService;
	}

	public void setDeviceService( DeviceService deviceService )
	{
		this.deviceService = deviceService;
	}

	public void setAuditLogService( AuditLogService auditLogService )
	{
		this.auditLogService = auditLogService;
	}
}

