package com.marchnetworks.management.instrumentation.events;

public class DeviceAlarmConfigEvent extends DeviceAlarmEvent
{
	public DeviceAlarmConfigEvent( String deviceId, long timestamp )
	{
		super( DeviceAlarmConfigEvent.class.getName(), DeviceAlarmEventType.ALARM_CONFIG, deviceId, timestamp, null, null, null );
	}
}

