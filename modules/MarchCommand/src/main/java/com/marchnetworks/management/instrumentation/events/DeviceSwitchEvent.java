package com.marchnetworks.management.instrumentation.events;

import com.marchnetworks.command.common.transport.data.Pair;

public class DeviceSwitchEvent extends AbstractDeviceEvent implements DeviceOutputEvent
{
	private String switchId;
	private String state;
	private DeviceOutputEventType switchEventType;
	private Pair[] extraInfo;

	public DeviceSwitchEvent( String deviceId, DeviceOutputEventType eventType, String switchId, String switchState, Pair[] extraInfo )
	{
		super( DeviceSwitchEvent.class.getName(), deviceId );
		this.switchId = switchId;
		state = switchState;
		switchEventType = eventType;
		this.extraInfo = extraInfo;
	}

	protected DeviceSwitchEvent( String eventType, String deviceId, DeviceOutputEventType type, String switchId, String switchState, Pair[] extraInfo )
	{
		super( eventType, deviceId );
		this.switchId = switchId;
		state = switchState;
		switchEventType = type;
		this.extraInfo = extraInfo;
	}

	public String getDeviceOutputId()
	{
		return switchId;
	}

	public String getState()
	{
		return state;
	}

	public DeviceOutputEventType getType()
	{
		return switchEventType;
	}

	public Pair[] getExtraInfo()
	{
		return extraInfo;
	}
}

