package com.marchnetworks.common.event;

import com.google.common.base.Predicate;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public enum EventTypesEnum implements Predicate<String>
{
	TOPOLOGY( "topology" ),
	TOPOLOGY_CREATED( "topology.created" ),
	TOPOLOGY_REMOVED( "topology.removed" ),
	TOPOLOGY_UPDATED( "topology.updated" ),
	TOPOLOGY_MOVED( "topology.moved" ),

	USER( "user" ),
	USER_CREATED( "user.created" ),
	USER_UPDATED( "user.updated" ),
	USER_REMOVED( "user.removed" ),
	USER_LOGOFF( "user.logoff" ),
	USER_SUPPORT_LDAP( "user.support.ldap" ),

	PROFILE( "profile" ),
	PROFILE_CREATED( "profile.created" ),
	PROFILE_UPDATED( "profile.updated" ),
	PROFILE_REMOVED( "profile.removed" ),

	CONFIGURATION( "configuration" ),
	CONFIGURATION_UPDATED( "configuration.updated" ),
	CONFIGURATION_ADDED( "configuration.created" ),
	CONFIGURATION_REMOVED( "configuration.removed" ),
	CONFIGURATION_DEVICE_UPDATED( "configuration.device.updated" ),

	FIRMWARE( "firmware" ),
	FIRMWARE_ADDED( "firmware.added" ),
	FIRMWARE_REMOVED( "firmware.removed" ),

	HEALTH( "alert" ),
	HEALTH_CLOSED( "alert.closed" ),
	HEALTH_CREATED( "alert.created" ),
	HEALTH_CLEARED( "alert.cleared" ),
	HEALTH_UPDATED( "alert.updated" ),
	HEALTH_THRESHOLD_UPDATED( "alert.threshold.updated" ),

	STATISTIC_SYSTEM_BANDWIDTH_INCOMING_IP( "system.bandwidth.incoming.ip" ),
	STATISTIC_CHANNEL_BANDWIDTH_INCOMING( "channel.bandwidth.incoming" ),
	STATISTIC_CHANNEL_BANDWIDTH_RECORDING( "channel.bandwidth.recording" ),
	STATISTIC_SYSTEM_BANDWIDTH_OUTGOING( "system.bandwidth.outgoing" ),
	STATISTIC_SYSTEM_BANDWIDTH_RECORDING( "system.bandwidth.recording" ),
	STATISTIC_SYSTEM_CPULOAD( "system.cpuload" ),
	STATISTIC_SYSTEM_CPULOAD_TOTAL( "system.cpuload.total" ),
	STATISTIC_SYSTEM_MEMORYUSED( "system.memoryUsed" ),
	STATISTIC_SYSTEM_MEMORY_USED_TOTAL( "system.memoryUsed.total" ),
	STATISTIC_SYSTEM_MEMORY_TOTAL( "system.memoryTotal" ),

	DEVICE( "device" ),
	DEVICE_REGISTRATION( "device.registration" ),
	DEVICE_UNREGISTRATION( "device.unregistration" ),
	DEVICE_REPLACEMENT( "device.replacement" ),
	DEVICE_CHANNELS_MAX( "device.channels.max" ),
	DEVICE_CHANNELS_INUSE( "device.channels.inuse" ),
	DEVICE_CONNECTION_STATE( "device.connection.state" ),
	DEVICE_RESTART( "device.restart" ),
	DEVICE_IP_CHANGED( "device.ip.changed" ),

	EVENT_ALERT_CHANNEL_RECORDING( "channel.recording" ),
	EVENT_ALERT_CHANNEL_STREAMING( "channel.streaming" ),
	EVENT_ALERT_CHANNEL_CONFIGURED( "channel.configured" ),
	EVENT_ALERT_SYSTEM_STREAMING( "system.streaming" ),
	EVENT_ALERT_SYSTEM_CONFIGURED( "system.configured" ),
	EVENT_ALERT_SYSTEM_RECORDING( "system.recording" ),

	CHANNEL( "channel" ),
	SYSTEM( "system" ),
	SYSTEM_CHANGED( "system.changed" ),

	LICENSE( "license" ),
	LICENSE_COUNT_ALLOCATED( "license.count.allocated" ),
	LICENSE_IMPORT_FINISHED( "license.import.finished" ),
	LICENSE_NAG_SCREEN( "license.nagscreen" ),
	LICENSE_UPDATED( "license.updated" ),
	LICENSE_REMOVED( "license.removed" ),
	LICENSE_INVALID( "license.invalid" ),

	WATCHDOG( "watchdog" ),
	WATCHDOG_DATABASE_DISCONNECTED( "watchdog.database.disconnected" ),
	WATCHDOG_DATABASE_CONNECTED( "watchdog.database.connected" ),

	ALARM( "alarm" ),
	ALARM_ENTRY( "alarm.entry" ),
	ALARM_ENTRY_CLOSED( "alarm.entry.closed" ),
	ALARM_STATE( "alarm.state" ),
	ALARM_REMOVED( "alarm.removed" ),

	CLIENT( "client" ),
	CLIENT_BUFFER_LIVE( "client.buffer.live" ),
	CLIENT_BUFFER_LIVE_PTZ( "client.buffer.liveptz" ),

	CERTIFICATE( "certificate" ),
	CERTIFICATE_ADDED( "certificate.added" ),
	CERTIFICATE_REMOVED( "certificate.removed" ),

	APP( "app" ),
	APP_INSTALLED( "app.installed" ),
	APP_STATE( "app.state" ),
	APP_UNINSTALLED( "app.uninstalled" ),
	APP_UPGRADED( "app.upgraded" ),

	OBJECT( "object" ),
	OBJECT_UPDATED( "object.updated" ),
	OBJECT_REMOVED( "object.removed" ),

	NOTIFICATION( "notification" ),
	NOTIFICATION_EMAIL_SENT( "notification.email.sent" ),
	NOTIFICATION_EMAIL_ERROR( "notification.email.error" ),
	NOTIFICATION_SMS_SENT( "notification.sms.sent" ),
	NOTIFICATION_SMS_ERROR( "notification.sms.error" ),
	NOTIFICATION_DELETED( "notification.deleted" ),

	SCHEDULE_UPDATED( "schedule.updated" ),
	SCHEDULE_DELETED( "schedule.deleted" ),
	SCHEDULE_NOTIFICATION( "schedule.notification" ),

	FIRMWARE_UPGRADE_CHANGED( "firmware.upgrade.configured" ),
	GROUP_FIRMWARE_UPGRADE_CHANGED( "group.firmware.upgrade.configured" ),
	FIRMWARE_UPGRADE_PENDING( "firmware.upgrade.pending" ),
	FIRMWARE_UPGRADE_COMPLETED( "firmware.upgrade.completed" ),
	FIRMWARE_UPGRADE_FAILED( "firmware.upgrade.failed" ),
	FIRMWARE_UPGRADE_TIMEOUT( "firmware.upgrade.timeout" ),
	FIRMWARE_UPGRADE_ACCEPTED( "firmware.upgrade.accepted" ),

	TIME_SYNC_DISABLED( "timesync.disabled" ),

	DATABASE_SIZE( "database.size" ),

	ARCHIVER_ASSOCIATION( "archiver.association" ),
	ARCHIVER_ASSOCIATION_UPDATED( "archiver.association.updated" ),
	ARCHIVER_ASSOCIATION_REMOVED( "archiver.association.removed" );

	private static final Map<String, EventTypesEnum> stringToEnum = new TreeMap<>();

	static
	{
		for ( EventTypesEnum eventTypes : values() )
			if ( !eventTypes.getFullPathEventName().contains( "." ) )
				stringToEnum.put( eventTypes.getFullPathEventName(), eventTypes );
	}

	public static EventTypesEnum fromFullPathName( String fullPathName )
	{
		return ( EventTypesEnum ) stringToEnum.get( fullPathName );
	}

	public static Set<String> getFullPathEventSet()
	{
		return stringToEnum.keySet();
	}

	private String fullPathEventName;

	private EventTypesEnum( String fullPathName )
	{
		fullPathEventName = fullPathName;
	}

	public boolean apply( String fullPathPredicate )
	{
		return fullPathPredicate.startsWith( fullPathEventName );
	}

	public String getFullPathEventName()
	{
		return fullPathEventName;
	}
}
