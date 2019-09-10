package com.marchnetworks.audit.data;

import java.util.Map;
import java.util.TreeMap;

public enum AuditEventNameEnum
{
	USER_LOGIN( "user.login" ),
	USER_LOGOUT( "user.logout" ),
	USER_LOGIN_FAILED( "user.login.failed" ),
	LIVE_VIDEO_REQUEST( "channel.liverequest" ),
	PTZ_CONTROL( "channel.ptz.control" ),
	PTZ_TOUR( "channel.ptz.tour" ),
	PTZ_PRESET( "channel.ptz.preset" ),
	ARCHIVE_VIDEO_REQUEST( "channel.archiverequest" ),
	ARCHIVE_VIDEO_EXPORT_REQUEST( "channel.exportrequest" ),
	TALK_CHANNEL_CONTROL( "audioOut.state" ),
	SWITCH_CONTROL( "switch.state" ),
	ALARM_SOURCE_CONTROL( "alarm.state" ),
	ALARM_HANDLING( "alarm.entry" ),
	ALARM_CLOSE( "alarm.entry.closed" ),
	ALARM_HISTORY_SEARCH( "alarm.historicalsearch" ),
	ALERT_CLOSE( "alert.closed" ),
	ALERT_THRESHOLD( "alert.threshold" ),
	TOPOLOGY_CREATE( "topology.created" ),
	TOPOLOGY_UPDATE( "topology.updated" ),
	TOPOLOGY_REMOVE( "topology.removed" ),
	TOPOLOGY_MOVE( "topology.moved" ),
	USER_CREATE( "user.created" ),
	USER_UPDATE( "user.updated" ),
	USER_REMOVED( "user.removed" ),
	PROFILE_CREATE( "profile.created" ),
	PROFILE_UPDATE( "profile.updated" ),
	PROFILE_REMOVED( "profile.removed" ),
	APPS_INSTALLED( "app.installed" ),
	APPS_START( "app.start" ),
	APPS_STOP( "app.stop" ),
	APPS_UNINSTALLED( "app.uninstalled" ),
	APPS_UPGRADED( "app.upgraded" ),
	LICENSE_IMPORT( "license.import" ),
	LICENSE_ALLOCATE( "license.count.allocated" ),
	LICENSE_UPDATE( "license.updated" ),
	LICENSE_REMOVE( "license.removed" ),
	DEVICE_REGISTRATION( "device.registration" ),
	DEVICE_REPLACEMENT( "device.replacement" ),
	DEVICE_MARK_FOR_REPLACEMENT( "device.markForReplacement" ),
	DEVICE_UNDO_MARK_FOR_REPLACEMENT( "device.undoMarkForReplacement" ),
	DEVICE_UNREGISTRATION( "device.unregistration" ),
	DEVICE_EDIT_ADDRESS( "device.address.updated" ),
	CONFIGURATION_IMPORT( "configuration.created" ),
	CONFIGURATION_APPLY( "configuration.device.updated" ),
	FIRMWARE_UPGRADE( "firmware.upgrade" ),
	GROUP_FIRMWARE_UPGRADE( "group.firmware.upgrade" ),
	DOWNLOAD_SERVER_LOGS( "server.logs.download" ),
	DOWNLOAD_DEVICE_LOGS( "system.logdownload" ),
	AUDIT_LOGS_SEARCH( "audit.search" ),
	VIDEO_SNAPSHOT( "video.snapshot" ),
	DOWNLOAD_CLIENT_LOGS( "client.logs.download" ),
	OPEN_VIEW( "open.view" ),
	CLOSE_VIEW( "close.view" ),
	OPEN_MAP( "open.map" ),
	CLOSE_MAP( "close.map" ),
	VIDEO_EXPORT_PDF( "video.export.pdf" ),
	NOTIFICATION_CREATED( "notification.created" ),
	NOTIFICATION_UPDATED( "notification.updated" ),
	NOTIFICATION_DELETED( "notification.deleted" ),
	SCHEDULE_CREATED( "schedule.created" ),
	SCHEDULE_UPDATED( "schedule.updated" ),
	SCHEDULE_DELETED( "schedule.deleted" ),
	CASE_CREATE( "case.created" ),
	CASE_UPDATE( "case.updated" ),
	CASE_DELETE( "case.deleted" ),
	CASE_EXPORT( "case.exported" ),
	LOCAL_GROUP_CREATE( "local.group.created" ),
	LOCAL_GROUP_UPDATE( "local.group.updated" ),
	LOCAL_GROUP_DELETE( "local.group.deleted" );

	private static final Map<String, AuditEventNameEnum> stringToEnum = new TreeMap<String, AuditEventNameEnum>();

	static
	{
		for ( AuditEventNameEnum eventNames : values() )
			stringToEnum.put( eventNames.getName(), eventNames );
	}

	public static AuditEventNameEnum fromString( String eventName )
	{
		if ( eventName.equals( "channel.ptzcontrol" ) )
			return PTZ_CONTROL;

		return stringToEnum.get( eventName );
	}

	private String name;

	private AuditEventNameEnum( String eventName )
	{
		name = eventName;
	}

	public String getName()
	{
		return name;
	}
}
