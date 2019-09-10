package com.marchnetworks.common.types;

import com.marchnetworks.command.api.alert.AlertDefinitionEnum;

public enum AlertThresholdDefinitionEnum
{
	CHANNEL_STATE( AlertDefinitionEnum.CHANNEL_STATE, AlertThresholdNotificationEnum.FREQUENCYORDURATION, true, true, 60, 100, 86400 ),
	CHANNEL_ANALYTIC_AREA_OBSTRUCTION_LICENSE( AlertDefinitionEnum.CHANNEL_ANALYTIC_AREA_OBSTRUCTION_LICENSE, AlertThresholdNotificationEnum.ALWAYS, true, true ),
	CHANNEL_ANALYTIC_OBJECT_TRACKING_LICENSE( AlertDefinitionEnum.CHANNEL_ANALYTIC_OBJECT_TRACKING_LICENSE, AlertThresholdNotificationEnum.ALWAYS, true, false ),
	CHANNEL_ANALYTIC_PEOPLE_COUNTING_LICENSE( AlertDefinitionEnum.CHANNEL_ANALYTIC_PEOPLE_COUNTING_LICENSE, AlertThresholdNotificationEnum.ALWAYS, true, false ),
	CHANNEL_ANALYTIC_SCENE_VERIFICATION_LICENSE( AlertDefinitionEnum.CHANNEL_ANALYTIC_SCENE_VERIFICATION_LICENSE, AlertThresholdNotificationEnum.ALWAYS, true, false ),
	CHANNEL_ANALYTIC_SCENE_VERIFICATION_LEARN( AlertDefinitionEnum.CHANNEL_ANALYTIC_SCENE_VERIFICATION_LEARN, AlertThresholdNotificationEnum.ALWAYS, true, true ),
	CHANNEL_RECEIVING( AlertDefinitionEnum.CHANNEL_RECEIVING, AlertThresholdNotificationEnum.FREQUENCYORDURATION, true, true, 60, 10, 3600 ),
	DISK_STATE( AlertDefinitionEnum.DISK_STATE, AlertThresholdNotificationEnum.ALWAYS, true, true ),
	DISK_SMART( AlertDefinitionEnum.DISK_SMART, AlertThresholdNotificationEnum.ALWAYS, true, true ),

	SYSTEM_CLOCK( AlertDefinitionEnum.SYSTEM_CLOCK, AlertThresholdNotificationEnum.ALWAYS, false, false ),
	SYSTEM_GPS( AlertDefinitionEnum.SYSTEM_GPS, AlertThresholdNotificationEnum.FREQUENCYORDURATION, true, true, 300, 10, 3600 ),
	SYSTEM_IOBOARD( AlertDefinitionEnum.SYSTEM_IOBOARD, AlertThresholdNotificationEnum.ALWAYS, true, true ),
	SYSTEM_OVERCURRENT( AlertDefinitionEnum.SYSTEM_OVERCURRENT, AlertThresholdNotificationEnum.ALWAYS, true, true ),
	SYSTEM_DATAPORT( AlertDefinitionEnum.SYSTEM_DATAPORT, AlertThresholdNotificationEnum.ALWAYS, true, true ),
	SYSTEM_AUTH_LOCAL( AlertDefinitionEnum.SYSTEM_AUTH_LOCAL, AlertThresholdNotificationEnum.ALWAYS, true, true ),
	SYSTEM_SOFTWARE( AlertDefinitionEnum.SYSTEM_SOFTWARE, AlertThresholdNotificationEnum.FREQUENCY, false, true, 60, 2, 86400 ),
	SYSTEM_RECORDING( AlertDefinitionEnum.SYSTEM_RECORDING, AlertThresholdNotificationEnum.FREQUENCYORDURATION, true, true, 60, 10, 3600 ),
	SYSTEM_STARTUP( AlertDefinitionEnum.SYSTEM_STARTUP, AlertThresholdNotificationEnum.FREQUENCY, false, true, 60, 2, 86400 ),
	SYSTEM_BATTERY( AlertDefinitionEnum.SYSTEM_BATTERY, AlertThresholdNotificationEnum.ALWAYS, false, false ),
	SYSTEM_KEY( AlertDefinitionEnum.SYSTEM_KEY, AlertThresholdNotificationEnum.ALWAYS, false, true, 60, 10, 600 ),
	SYSTEM_TEMPERATURE( AlertDefinitionEnum.SYSTEM_TEMPERATURE, AlertThresholdNotificationEnum.FREQUENCYORDURATION, true, true, 60, 10, 3600 ),
	SYSTEM_FAN( AlertDefinitionEnum.SYSTEM_FAN, AlertThresholdNotificationEnum.FREQUENCYORDURATION, true, true, 60, 10, 3600 ),
	SYSTEM_POWER( AlertDefinitionEnum.SYSTEM_POWER, AlertThresholdNotificationEnum.FREQUENCY, false, true, 60, 2, 86400 ),
	SYSTEM_HARDWARE( AlertDefinitionEnum.SYSTEM_HARDWARE, AlertThresholdNotificationEnum.ALWAYS, false, false ),
	SYSTEM_RETENTION( AlertDefinitionEnum.SYSTEM_RETENTION, AlertThresholdNotificationEnum.FREQUENCYORDURATION, true, true, 60, 10, 3600 ),
	SYSTEM_STREAMING( AlertDefinitionEnum.SYSTEM_STREAMING, AlertThresholdNotificationEnum.FREQUENCYORDURATION, true, true, 60, 10, 3600 ),
	SYSTEM_CONFIGURED( AlertDefinitionEnum.SYSTEM_CONFIGURED, AlertThresholdNotificationEnum.ALWAYS, false, false ),
	SYSTEM_BANDWIDTH_RECORDING_LIMIT_EXCEEDED( AlertDefinitionEnum.SYSTEM_BANDWIDTH_RECORDING_EXCEEDED, AlertThresholdNotificationEnum.FREQUENCYORDURATION, true, true, 60, 10, 3600 ),
	SYSTEM_BANDWIDTH_INCOMING_ANALOG_LIMIT_EXCEEDED( AlertDefinitionEnum.SYSTEM_BANDWIDTH_INCOMING_ANALOG_LIMIT_EXCEEDED, AlertThresholdNotificationEnum.FREQUENCYORDURATION, true, true, 60, 10, 3600 ),
	SYSTEM_BANDWIDTH_INCOMING_IP_LIMIT_EXCEEDED( AlertDefinitionEnum.SYSTEM_BANDWIDTH_INCOMING_IP_LIMIT_EXCEEDED, AlertThresholdNotificationEnum.FREQUENCYORDURATION, true, true, 60, 10, 3600 ),
	SYSTEM_BANDWIDTH_OUTGOING_LIMIT_EXCEEDED( AlertDefinitionEnum.SYSTEM_BANDWIDTH_OUTGOING_LIMIT_EXCEEDED, AlertThresholdNotificationEnum.FREQUENCYORDURATION, true, true, 60, 10, 3600 ),
	SYSTEM_CPULOAD_LIMIT_EXCEEDED( AlertDefinitionEnum.SYSTEM_CPULOAD_LIMIT_EXCEEDED, AlertThresholdNotificationEnum.FREQUENCYORDURATION, true, true, 60, 10, 3600 ),
	SYSTEM_MEMORYUSED_LIMIT_EXCEEDED( AlertDefinitionEnum.SYSTEM_MEMORYUSED_LIMIT_EXCEEDED, AlertThresholdNotificationEnum.FREQUENCYORDURATION, true, true, 60, 10, 3600 ),
	SYSTEM_MEMORYUSED_LIMIT_PROCEXCEEDING( AlertDefinitionEnum.SYSTEM_MEMORYUSED_LIMIT_PROCEXCEEDING, AlertThresholdNotificationEnum.FREQUENCYORDURATION, true, true, 60, 10, 3600 ),
	SWITCH_STATE( AlertDefinitionEnum.SWITCH_STATE, AlertThresholdNotificationEnum.FREQUENCYORDURATION, true, true, 600, 10, 3600 ),
	LICENSE_EXPIRING( AlertDefinitionEnum.LICENSE_EXPIRING, AlertThresholdNotificationEnum.ALWAYS, false, false ),

	SYSTEM_GATEWAY_MOBILE( "system.gateway.mobile", AlertThresholdNotificationEnum.FREQUENCYORDURATION, true, true, 600, 10, 3600 ),
	SYSTEM_LASTUPDATE( "system.lastupdate", AlertThresholdNotificationEnum.ALWAYS, false, false ),
	ALARM_STATE( "alarm.state", AlertThresholdNotificationEnum.FREQUENCYORDURATION, true, true, 60, 10, 3600 ),
	DISK_CHANGED( "disk.changed", AlertThresholdNotificationEnum.ALWAYS, true, true );

	private AlertThresholdNotificationEnum notificationType;
	private String alertCode;
	private int duration;
	private int frequencyDuration;
	private int frequencyCount;
	private boolean hasDurationSupport;
	private boolean hasFrequencySupport;

	private AlertThresholdDefinitionEnum( String alertCode, AlertThresholdNotificationEnum notificationType, boolean hasDurationSupport, boolean hasFrequencySupport )
	{
		this( alertCode, notificationType, hasDurationSupport, hasFrequencySupport, 60, 10, 3600 );
	}

	private AlertThresholdDefinitionEnum( String alertCode, AlertThresholdNotificationEnum notificationType, boolean hasDurationSupport, boolean hasFrequencySupport, int duration, int frequencyCount, int frequencyTimeSpan )
	{
		this.notificationType = notificationType;
		this.alertCode = alertCode;
		this.duration = duration;
		frequencyDuration = frequencyTimeSpan;
		this.frequencyCount = frequencyCount;
		this.hasDurationSupport = hasDurationSupport;
		this.hasFrequencySupport = hasFrequencySupport;
	}

	private AlertThresholdDefinitionEnum( AlertDefinitionEnum alert, AlertThresholdNotificationEnum notificationType, boolean hasDurationSupport, boolean hasFrequencySupport )
	{
		this( alert, notificationType, hasDurationSupport, hasFrequencySupport, 60, 10, 3600 );
	}

	private AlertThresholdDefinitionEnum( AlertDefinitionEnum alert, AlertThresholdNotificationEnum notificationType, boolean hasDurationSupport, boolean hasFrequencySupport, int duration, int frequencyCount, int frequencyTimeSpan )
	{
		this( alert.getPath(), notificationType, hasDurationSupport, hasFrequencySupport, duration, frequencyCount, frequencyTimeSpan );
	}

	public boolean HasDurationSupport()
	{
		return hasDurationSupport;
	}

	public boolean HasFrequencySupport()
	{
		return hasFrequencySupport;
	}

	public AlertThresholdNotificationEnum getNotificationType()
	{
		return notificationType;
	}

	public int getDuration()
	{
		return duration;
	}

	public int getFrequencyDuration()
	{
		return frequencyDuration;
	}

	public int getFrequencyCount()
	{
		return frequencyCount;
	}

	public String getAlertCode()
	{
		return alertCode;
	}

	public static AlertThresholdDefinitionEnum fromPath( String path )
	{
		if ( path == null )
		{
			return null;
		}
		for ( AlertThresholdDefinitionEnum def : values() )
		{
			if ( path.equalsIgnoreCase( def.getAlertCode() ) )
			{
				return def;
			}
		}

		return null;
	}
}
