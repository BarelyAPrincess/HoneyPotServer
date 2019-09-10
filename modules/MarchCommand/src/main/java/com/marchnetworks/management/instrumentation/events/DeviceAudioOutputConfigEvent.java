package com.marchnetworks.management.instrumentation.events;

public class DeviceAudioOutputConfigEvent extends DeviceAudioOutputEvent
{
	public DeviceAudioOutputConfigEvent( String deviceId )
	{
		super( DeviceAudioOutputConfigEvent.class.getName(), deviceId, DeviceOutputEventType.OUTPUT_CONFIG, null, null, null );
	}
}

