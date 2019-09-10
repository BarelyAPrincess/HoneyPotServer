package com.marchnetworks.management.instrumentation.events;

import com.marchnetworks.command.common.transport.data.Pair;

public class DeviceAudioOutputEvent extends AbstractDeviceEvent implements DeviceOutputEvent
{
	private String audioOutputId;
	private String state;
	private DeviceOutputEventType type;
	private Pair[] extraInfo;

	public DeviceAudioOutputEvent( String deviceId, DeviceOutputEventType type, String audioOutputId, String audioOutputState, Pair[] extraInfo )
	{
		super( DeviceAudioOutputEvent.class.getName(), deviceId );
		this.audioOutputId = audioOutputId;
		state = audioOutputState;
		this.type = type;
		this.extraInfo = extraInfo;
	}

	protected DeviceAudioOutputEvent( String eventType, String deviceId, DeviceOutputEventType type, String audioOutputId, String audioOutputState, Pair[] extraInfo )
	{
		super( eventType, deviceId );
		this.audioOutputId = audioOutputId;
		state = audioOutputState;
		this.type = type;
		this.extraInfo = extraInfo;
	}

	public String getDeviceOutputId()
	{
		return audioOutputId;
	}

	public String getState()
	{
		return state;
	}

	public DeviceOutputEventType getType()
	{
		return type;
	}

	public Pair[] getExtraInfo()
	{
		return extraInfo;
	}
}

