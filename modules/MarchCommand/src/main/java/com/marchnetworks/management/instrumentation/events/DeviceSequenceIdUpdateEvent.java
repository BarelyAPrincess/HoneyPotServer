package com.marchnetworks.management.instrumentation.events;

public class DeviceSequenceIdUpdateEvent extends AbstractDeviceEvent
{
	private Long eventSequenceId;

	public DeviceSequenceIdUpdateEvent( String deviceId, Long eventSequenceId )
	{
		super( DeviceSequenceIdUpdateEvent.class.getName(), deviceId );
		this.eventSequenceId = eventSequenceId;
	}

	public Long getEventSequenceId()
	{
		return eventSequenceId;
	}
}

