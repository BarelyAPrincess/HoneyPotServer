package com.marchnetworks.health.data;

public enum HealthSummaryCodeCategoryMapEnum
{
	CHANNEL_RECORDING( "channel.recording", HealthSummaryCategoryEnum.DRIVE ),
	DISK_ADDED( "disk.added", HealthSummaryCategoryEnum.DRIVE ),
	DISK_REMOVED( "disk.removed", HealthSummaryCategoryEnum.DRIVE ),
	DISK_STATE( "disk.state", HealthSummaryCategoryEnum.DRIVE ),
	DISK_SMART( "disk.smart", HealthSummaryCategoryEnum.DRIVE ),
	DISK_CHANGED( "disk.changed", HealthSummaryCategoryEnum.DRIVE ),
	SYSTEM_RECORDING( "system.recording", HealthSummaryCategoryEnum.DRIVE ),
	SYSTEM_RETENTION( "system.retention", HealthSummaryCategoryEnum.DRIVE ),
	ARCHIVER_STORAGE( "extractor.storage", HealthSummaryCategoryEnum.DRIVE ),

	CHANNEL_CONFIGURED( "channel.configured", HealthSummaryCategoryEnum.UNIT ),
	SYSTEM_CLOCK( "system.clock", HealthSummaryCategoryEnum.UNIT ),
	SYSTEM_OVERCURRENT( "system.overcurrent", HealthSummaryCategoryEnum.UNIT ),
	SYSTEM_AUTH_LOCAL( "system.auth.local", HealthSummaryCategoryEnum.UNIT ),
	SYSTEM_SOFTWARE( "system.software", HealthSummaryCategoryEnum.UNIT ),
	SYSTEM_STARTUP( "system.startup", HealthSummaryCategoryEnum.UNIT ),
	SYSTEM_FAN( "system.fan", HealthSummaryCategoryEnum.UNIT ),
	SYSTEM_KEY( "system.key", HealthSummaryCategoryEnum.UNIT ),
	SYSTEM_BATTERY( "system.battery", HealthSummaryCategoryEnum.UNIT ),
	SYSTEM_TEMP( "channel.temperature", HealthSummaryCategoryEnum.UNIT ),
	SYSTEM_HARDWARE( "system.hardware", HealthSummaryCategoryEnum.UNIT ),
	SYSTEM_LASTUPDATE( "system.lastupdate", HealthSummaryCategoryEnum.UNIT ),
	ALERT_LICENSE_EXPIRING( "license.expiring", HealthSummaryCategoryEnum.UNIT ),
	SYSTEM_CONFIGURED( "system.configured", HealthSummaryCategoryEnum.UNIT ),
	SYSTEM_CPULOAD_LIMIT_EXCEEDED( "system.cpuload.limit.exceeded", HealthSummaryCategoryEnum.UNIT ),
	SYSTEM_MEMORYUSED_LIMIT_PROCEXCEEDING( "system.memoryUsed.limit.procexceeding", HealthSummaryCategoryEnum.UNIT ),
	SYSTEM_GATEWAY_MOBILE( "system.gateway.mobile", HealthSummaryCategoryEnum.UNIT ),
	ALERT_LICENSE_EXPIRE( "license.expire", HealthSummaryCategoryEnum.UNIT ),
	WATCHDOG_RESTART( "watchdog.restart", HealthSummaryCategoryEnum.UNIT ),
	CES_LOW_MEMORY( "server.memory", HealthSummaryCategoryEnum.UNIT ),
	EXTRACTOR_STORAGE( "extractor.storage", HealthSummaryCategoryEnum.UNIT ),
	EXTRACTOR_TRANSLATION_TRANSLATOR( "extractor.transaction.translator", HealthSummaryCategoryEnum.UNIT ),
	EXTRACTOR_TRANSLATION_DOWNLOAD( "extractor.transaction.download", HealthSummaryCategoryEnum.UNIT ),
	EXTRACTOR_IMAGE_DOWNLOAD( "extractor.image.download", HealthSummaryCategoryEnum.UNIT ),

	SYSTEM_BANDWIDTH_RECORDING_LIMIT_EXCEEDED( "system.bandwidth.recording.limit.exceeded", HealthSummaryCategoryEnum.NETWORK ),
	SYSTEM_BANDWIDTH_INCOMING_IP_LIMIT_EXCEEDED( "system.bandwidth.incoming.ip.limit.exceeded", HealthSummaryCategoryEnum.NETWORK ),
	SYSTEM_BANDWIDTH_INCOMING_ANALOG_LIMIT_EXCEEDED( "system.bandwidth.incoming.analog.limit.exceeded", HealthSummaryCategoryEnum.NETWORK ),
	SYSTEM_BANDWIDTH_OUTGOING_LIMIT_EXCEEDED( "system.bandwidth.outgoing.limit.exceeded", HealthSummaryCategoryEnum.NETWORK ),
	DEVICE_ADDRESS_UNREACHABLE( "server.device.address.unreachable", HealthSummaryCategoryEnum.NETWORK ),
	DEVICE_NETWORK_CONFIG_CHANGED( "server.device.network.config.changed", HealthSummaryCategoryEnum.NETWORK ),
	DEVICE_DISCONNECTED( "device.disconnected", HealthSummaryCategoryEnum.NETWORK ),

	CHANNEL_STATE( "channel.state", HealthSummaryCategoryEnum.VIDEO ),
	CHANNEL_CAPTURE( "channel.capture", HealthSummaryCategoryEnum.VIDEO ),
	CHANNEL_STREAMING( "channel.streaming", HealthSummaryCategoryEnum.VIDEO ),
	CHANNEL_ANALYTIC_OBSTRUCTION_LICENSE( "channel.analytic.area_obstruction.license", HealthSummaryCategoryEnum.VIDEO ),
	CHANNEL_ANALYTIC_TRACKING_LICENSE( "channel.analytic.object_tracking.license", HealthSummaryCategoryEnum.VIDEO ),
	CHANNEL_ANALYTIC_PEOPLE_COUNTING_LICENSE( "channel.analytic.people_counting.license", HealthSummaryCategoryEnum.VIDEO ),
	CHANNEL_ANALYTIC_SCENE_VERIFICATION_LICENSE( "channel.analytic.scene_verification.license", HealthSummaryCategoryEnum.VIDEO ),
	CHANNEL_ANALYTIC_SCENE_VERIFICATION_LEARN( "channel.analytic.scene_verification.learn", HealthSummaryCategoryEnum.VIDEO ),
	CHANNEL_RECEIVING( "channel.receiving", HealthSummaryCategoryEnum.VIDEO ),
	SYSTEM_STREAMING( "system.streaming", HealthSummaryCategoryEnum.VIDEO ),

	SYSTEM_POWER( "system.power", HealthSummaryCategoryEnum.POWER ),

	SYSTEM_GPS( "system.gps", HealthSummaryCategoryEnum.PERIPHERAL ),
	SYSTEM_IOBOARD( "system.ioboard", HealthSummaryCategoryEnum.PERIPHERAL ),
	SYSTEM_DATAPORT( "system.dataport", HealthSummaryCategoryEnum.PERIPHERAL ),
	SWITCH_STATE( "switch.state", HealthSummaryCategoryEnum.PERIPHERAL ),
	ALARM_STATE( "alarm.state", HealthSummaryCategoryEnum.PERIPHERAL );

	private String path;
	private HealthSummaryCategoryEnum category;

	private HealthSummaryCodeCategoryMapEnum( String path, HealthSummaryCategoryEnum category )
	{
		this.path = path;
		this.category = category;
	}

	public String getPath()
	{
		return path;
	}

	public HealthSummaryCategoryEnum getCategory()
	{
		return category;
	}

	public static HealthSummaryCodeCategoryMapEnum getMapFromPath( String path )
	{
		if ( path != null )
		{
			for ( HealthSummaryCodeCategoryMapEnum map : values() )
			{
				if ( path.equalsIgnoreCase( path ) )
				{
					return map;
				}
			}
		}

		return null;
	}

	public static HealthSummaryCategoryEnum getCategoryFromPath( String path )
	{
		if ( path != null )
			for ( HealthSummaryCodeCategoryMapEnum map : values() )
				if ( path.equalsIgnoreCase( map.path ) )
					return map.category;

		return null;
	}
}
