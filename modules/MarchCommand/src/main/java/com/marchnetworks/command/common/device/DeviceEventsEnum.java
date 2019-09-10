package com.marchnetworks.command.common.device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum DeviceEventsEnum
{
	CHANNEL( "channel" ),

	CHANNEL_STATE( "channel.state" ),
	CHANNEL_CAPTURE( "channel.capture" ),
	CHANNEL_CONFIG( "channel.config" ),
	CHANNEL_UPDATE( "channel.update" ),
	CHANNEL_CONFIGHASH( "channel.confighash" ),
	CHANNEL_CHANGED( "channel.changed" ),
	CHANNEL_CREATED( "channel.created" ),
	CHANNEL_REMOVED( "channel.removed" ),

	CHANNEL_RECORDING( "channel.recording" ),
	CHANNEL_STREAMING( "channel.streaming" ),
	CHANNEL_CONFIGURED( "channel.configured" ),
	CHANNEL_BANDWIDH_INCOMING( "channel.bandwidth.incoming" ),
	CHANNEL_BANDWIDH_RECORDING( "channel.bandwidth.recording" ),

	CHANNEL_LIVEREQUEST( "channel.liverequest" ),
	CHANNEL_PTZ_CONTROL( "channel.ptz.control" ),
	CHANNEL_PTZCONTROL( "channel.ptzcontrol" ),
	CHANNEL_PTZ_PRESET( "channel.ptz.preset" ),
	CHANNEL_PTZ_TOUR( "channel.ptz.tour" ),
	CHANNEL_ARCHIVEREQUEST( "channel.archiverequest" ),
	CHANNEL_EXPORTREQUEST( "channel.exportrequest" ),

	SYSTEM( "system" ),

	SYSTEM_CONFIG( "system.config" ),
	SYSTEM_CONFIGHASH( "system.confighash" ),
	SYSTEM_CHANGED( "system.changed" ),
	SYSTEM_LASTUPDATE( "system.lastupdate" ),
	SYSTEM_MAX_CHANNELS_SUPPORTED( "system.maxChannelsSupported" ),
	SYSTEM_LASTCONFIG( "system.lastconfig" ),

	SYSTEM_STREAMING( "system.streaming" ),
	SYSTEM_CONFIGURED( "system.configured" ),
	SYSTEM_BANDWIDTH_RECORDING( "system.bandwidth.recording" ),
	SYSTEM_BANDWIDTH_INCOMING_IP( "system.bandwidth.incoming.ip" ),
	SYSTEM_BANDWIDTH_OUTGOING( "system.bandwidth.outgoing" ),
	SYSTEM_CPULOAD( "system.cpuload" ),
	SYSTEM_MEMORYUSED( "system.memoryUsed" ),
	SYSTEM_RECORDING( "system.recording" ),
	SYSTEM_CPULOAD_TOTAL( "system.cpuload.total" ),
	SYSTEM_MEMORYUSED_TOTAL( "system.memoryUsed.total" ),
	SYSTEM_MEMORY_TOTAL( "system.memoryTotal" ),

	SYSTEM_POWER( "system.power" ),
	SYSTEM_BATTERY( "system.battery" ),
	SYSTEM_STARTUP( "system.startup" ),
	SYSTEM_KEY( "system.key" ),
	SYSTEM_TEMPTERATURE( "system.temperature" ),
	SYSTEM_FAN( "system.fan" ),
	SYSTEM_HARDWARE( "system.hardware" ),
	SYSTEM_RETENTION( "system.retention" ),
	SYSTEM_BANDWIDTH_RECORDING_LIMIT_EXCEEDED( "system.bandwidth.recording.limit.exceeded" ),
	SYSTEM_MEMORYUSED_LIMIT_EXCEEDED( "system.memoryUsed.limit.exceeded" ),
	SYSTEM_MEMORYUSED_LIMIT_PROCEXCEEDING( "system.memoryUsed.limit.procexceeding" ),
	SYSTEM_BANDWIDTH_INCOMING_ANALOG_LIMIT_EXCEEDED( "system.bandwidth.incoming.analog.limit.exceeded" ),
	SYSTEM_BANDWIDTH_INCOMING_IP_LIMIT_EXCEEDED( "system.bandwidth.incoming.ip.limit.exceeded" ),
	SYSTEM_BANDWIDTH_OUTGOING_LIMIT_EXCEEDED( "system.bandwidth.outgoing.limit.exceeded" ),

	SYSTEM_LOGDOWNLOAD( "system.logdownload" ),
	SYSTEM_AUDIT_ENTRY( "system.audit.entry" ),

	SYSTEM_EXPORT_QUEUE( "system.localExport.queue" ),

	LICENSE( "license" ),

	LICENSE_CHANNELS_INUSE( "license.channels.inuse" ),

	ALARM( "alarm" ),

	ALARM_CONFIG( "alarm.config" ),
	ALARM_ENTRY( "alarm.entry" ),
	ALARM_ENTRY_CLOSED( "alarm.entry.closed" ),
	ALARM_STATE( "alarm.state" ),

	SWITCH( "switch" ),

	SWITCH_STATE( "switch.state" ),
	SWITCH_CONFIG( "switch.config" ),

	AUDIO_OUT( "audioOut" ),

	AUDIO_OUT_STATE( "audioOut.state" ),
	AUDIO_OUT_CONFIG( "audioOut.config" ),

	CLIENT( "client" ),

	CLIENT_BUFFER_LIVE( "client.buffer.live" ),
	CLIENT_BUFFER_LIVEPTZ( "client.buffer.liveptz" ),

	DISK( "disk" ),

	DISK_SMART( "disk.smart" ),
	DISK_STATE( "disk.state" ),
	DISK_ADDED( "disk.added" ),
	DISK_REMOVED( "disk.removed" ),

	ALERT( "alert" ),
	ALERT_UPDATED( "alert.updated" ),
	ALERT_CLOSED( "alert.closed" ),

	ANALYTICS( "analytics" ),
	ANALYTICS_STATISTICS( "analytics.statistics" ),

	EXTRACTOR( "extractor" ),
	EXTRACTOR_MEDIA_JOB( "extractor.media.job" ),
	EXTRACTOR_TRANSACTION_JOB( "extractor.transaction.job" ),
	EXTRACTOR_IMAGE_JOB( "extractor.image.job" ),
	EXTRACTOR_STORAGE_FREE( "extractor.storage.free" );

	private static final Map<DeviceEventsEnum, List<DeviceEventsEnum>> groups = new HashMap<DeviceEventsEnum, List<DeviceEventsEnum>>();

	static
	{
		for ( DeviceEventsEnum eventType : values() )
		{
			String path = eventType.getPath();

			if ( path.contains( "." ) )
			{
				String prefix = path.substring( 0, path.indexOf( "." ) );
				DeviceEventsEnum prefixEnum = getByPath( prefix );

				List<DeviceEventsEnum> group = groups.get( prefixEnum );
				if ( group == null )
				{
					group = new ArrayList<DeviceEventsEnum>();
					groups.put( prefixEnum, group );
				}
				group.add( eventType );
			}
		}
	}

	public static List<DeviceEventsEnum> getAllByPrefix( DeviceEventsEnum prefix )
	{
		return groups.get( prefix );
	}

	public static List<String> getAllPathsByPrefix( DeviceEventsEnum prefix )
	{
		List<DeviceEventsEnum> events = getAllByPrefix( prefix );
		List<String> result = new ArrayList<String>( events.size() );

		for ( DeviceEventsEnum event : events )
		{
			result.add( event.getPath() );
		}
		return result;
	}

	public static DeviceEventsEnum getByPath( String path )
	{
		for ( DeviceEventsEnum eventType : values() )
			if ( eventType.getPath().equals( path ) )
				return eventType;

		return null;
	}

	private String fullPathEventName;

	DeviceEventsEnum( String fullPathName )
	{
		fullPathEventName = fullPathName;
	}

	public String getPath()
	{
		return fullPathEventName;
	}
}
