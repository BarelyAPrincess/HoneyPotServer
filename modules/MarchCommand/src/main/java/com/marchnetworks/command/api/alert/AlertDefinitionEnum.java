package com.marchnetworks.command.api.alert;

public enum AlertDefinitionEnum
{
	CHANNEL_CAPTURE( "channel.capture", AlertCategoryEnum.VIDEO, new String[] {"online", "disabled"} ),
	CHANNEL_STATE( "channel.state", AlertCategoryEnum.VIDEO, new String[] {"online", "disabled"} ),
	CHANNEL_RECEIVING( "channel.receiving", AlertCategoryEnum.VIDEO, new String[] {"ok"} ),

	SYSTEM_RECORDING( "system.recording", AlertCategoryEnum.NOT_RECORDING, new String[] {"ok"} ),
	SYSTEM_POWER( "system.power", AlertCategoryEnum.POWER, new String[] {"ok"} ),

	SYSTEM_BATTERY( "system.battery", AlertCategoryEnum.HW, new String[] {"ok"} ),
	SYSTEM_STARTUP( "system.startup", AlertCategoryEnum.HW, new String[] {"ok"} ),
	SYSTEM_KEY( "system.key", AlertCategoryEnum.HW, new String[] {""}, "off" ),
	SYSTEM_TEMPERATURE( "system.temperature", AlertCategoryEnum.HW, new String[] {"ok"} ),
	SYSTEM_FAN( "system.fan", AlertCategoryEnum.HW, new String[] {"ok"} ),
	SYSTEM_HARDWARE( "system.hardware", AlertCategoryEnum.HW, new String[] {"ok"} ),
	SYSTEM_CLOCK( "system.clock", AlertCategoryEnum.HW, new String[] {"ok"} ),
	SYSTEM_GPS( "system.gps", AlertCategoryEnum.HW, new String[] {"ok"} ),
	SYSTEM_IOBOARD( "system.ioboard", AlertCategoryEnum.HW, new String[] {"ok"} ),
	SYSTEM_OVERCURRENT( "system.overcurrent", AlertCategoryEnum.HW, new String[] {"ok"} ),
	SYSTEM_DATAPORT( "system.dataport", AlertCategoryEnum.HW, new String[] {"ok"} ),
	SYSTEM_AUTH_LOCAL( "system.auth.local", AlertCategoryEnum.HW, new String[] {"ok"} ),
	SYSTEM_SOFTWARE( "system.software", AlertCategoryEnum.HW, new String[] {"ok"} ),

	SYSTEM_RETENTION( "system.retention", AlertCategoryEnum.STORAGE, new String[] {"ok"} ),
	DISK_SMART( "disk.smart", AlertCategoryEnum.STORAGE, new String[] {"ok"} ),
	DISK_STATE( "disk.state", AlertCategoryEnum.STORAGE, new String[] {"ok"} ),
	DISK_ADDED( "disk.added", AlertCategoryEnum.STORAGE, new String[] {"ok"} ),
	DISK_REMOVED( "disk.removed", AlertCategoryEnum.STORAGE, new String[] {"ok"} ),

	DEVICE_DISCONNECTED( "device.disconnected", AlertCategoryEnum.SW, new String[] {"ok"} ),
	LICENSE_EXPIRE( "license.expire", AlertCategoryEnum.LICENSE, new String[] {"ok"} ),
	WATCHDOG_RESTART( "watchdog.restart", AlertCategoryEnum.SW, new String[] {"ok"} ),
	SERVER_MEMORY( "server.memory", AlertCategoryEnum.SW, new String[] {"ok"} ),
	DEVICE_REGISTRATION_ADDRESS_UNREACHABLE( "server.device.address.unreachable", AlertCategoryEnum.NETWORK, new String[] {"ok"} ),
	DEVICE_NETWORK_CONFIG_CHANGED( "server.device.network.config.changed", AlertCategoryEnum.NETWORK, new String[] {"ok"} ),
	SERVER_TIME( "server.time", AlertCategoryEnum.SW, new String[] {"ok"} ),
	DATABASE_SIZE( "database.size", AlertCategoryEnum.SW, new String[] {"ok"} ),

	SYSTEM_STREAMING( "system.streaming", AlertCategoryEnum.SW, new String[] {"ok"} ),
	SYSTEM_CONFIGURED( "system.configured", AlertCategoryEnum.SW, new String[] {"ok"} ),
	SYSTEM_BANDWIDTH_RECORDING_EXCEEDED( "system.bandwidth.recording.limit.exceeded", AlertCategoryEnum.NETWORK, new String[] {"off"} ),
	SYSTEM_MEMORYUSED_LIMIT_EXCEEDED( "system.memoryUsed.limit.exceeded", AlertCategoryEnum.HW, new String[] {"off"} ),
	SYSTEM_MEMORYUSED_LIMIT_PROCEXCEEDING( "system.memoryUsed.limit.procexceeding", AlertCategoryEnum.HW, new String[] {"off"} ),
	SYSTEM_BANDWIDTH_INCOMING_ANALOG_LIMIT_EXCEEDED( "system.bandwidth.incoming.analog.limit.exceeded", AlertCategoryEnum.NETWORK, new String[] {"off"} ),
	SYSTEM_BANDWIDTH_INCOMING_IP_LIMIT_EXCEEDED( "system.bandwidth.incoming.ip.limit.exceeded", AlertCategoryEnum.NETWORK, new String[] {"off"} ),
	SYSTEM_BANDWIDTH_OUTGOING_LIMIT_EXCEEDED( "system.bandwidth.outgoing.limit.exceeded", AlertCategoryEnum.NETWORK, new String[] {"off"} ),

	SYSTEM_CPULOAD_LIMIT_EXCEEDED( "system.cpuload.limit.exceeded", AlertCategoryEnum.NETWORK, new String[] {"off"} ),

	SWITCH_STATE( "switch.state", AlertCategoryEnum.DEVICE, new String[] {"on", "off", "unknown", "disabled"}, "offline" ),
	LICENSE_EXPIRING( "license.expiring", AlertCategoryEnum.LICENSE, new String[] {"ok"} ),

	CHANNEL_ANALYTIC_AREA_OBSTRUCTION_LICENSE( "channel.analytic.area_obstruction.license", AlertCategoryEnum.VIDEO, new String[] {"ok"} ),
	CHANNEL_ANALYTIC_OBJECT_TRACKING_LICENSE( "channel.analytic.object_tracking.license", AlertCategoryEnum.VIDEO, new String[] {"ok"} ),
	CHANNEL_ANALYTIC_PEOPLE_COUNTING_LICENSE( "channel.analytic.people_counting.license", AlertCategoryEnum.VIDEO, new String[] {"ok"} ),
	CHANNEL_ANALYTIC_SCENE_VERIFICATION_LICENSE( "channel.analytic.scene_verification.license", AlertCategoryEnum.VIDEO, new String[] {"ok"} ),
	CHANNEL_ANALYTIC_SCENE_VERIFICATION_LEARN( "channel.analytic.scene_verification.learn", AlertCategoryEnum.VIDEO, new String[] {"ok"} );

	private String path;

	private AlertCategoryEnum category;

	private String[] clearedValue;

	private String triggerValue;

	private AlertDefinitionEnum( String path, AlertCategoryEnum category, String... clearedValue )
	{
		this.path = path;
		this.category = category;
		this.clearedValue = clearedValue;
		triggerValue = "";
	}

	private AlertDefinitionEnum( String path, AlertCategoryEnum category, String[] clearedValue, String triggerValue )
	{
		this.path = path;
		this.category = category;
		this.clearedValue = clearedValue;
		this.triggerValue = triggerValue;
	}

	public String getPath()
	{
		return path;
	}

	public AlertCategoryEnum getCategory()
	{
		return category;
	}

	public String[] getClearedValue()
	{
		return clearedValue;
	}

	public String getTriggerValue()
	{
		return triggerValue;
	}

	public static AlertDefinitionEnum fromPath( String path )
	{
		if ( path != null )
		{
			for ( AlertDefinitionEnum def : values() )
			{
				if ( path.equalsIgnoreCase( path ) )
				{
					return def;
				}
			}
		}
		return null;
	}
}
