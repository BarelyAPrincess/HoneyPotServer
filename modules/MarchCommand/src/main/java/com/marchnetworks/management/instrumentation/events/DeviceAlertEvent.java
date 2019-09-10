package com.marchnetworks.management.instrumentation.events;

import com.marchnetworks.command.common.transport.data.Event;

public class DeviceAlertEvent extends AbstractDeviceEvent
{
	private Event alertEvent;
	private DeviceAlertEventType deviceAlertEventType;

	public DeviceAlertEvent( DeviceAlertEventType deviceAlertEventType, String deviceId, Event alertEvent )
	{
		super( DeviceAlertEvent.class.getName(), deviceId );
		this.alertEvent = alertEvent;
		this.deviceAlertEventType = deviceAlertEventType;
	}

	public Event getAlertEvent()
	{
		return alertEvent;
	}

	public DeviceAlertEventType getDeviceAlertEventType()
	{
		return deviceAlertEventType;
	}

	public void setDeviceAlertEventType( DeviceAlertEventType deviceAlertEventType )
	{
		this.deviceAlertEventType = deviceAlertEventType;
	}
}

