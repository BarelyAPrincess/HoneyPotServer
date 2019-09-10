package com.marchnetworks.management.instrumentation.events;

import com.marchnetworks.command.common.device.data.ConnectState;

import java.util.Calendar;

public class DeviceConnectionStateEvent extends AbstractDeviceEvent
{
	private ConnectState connectState;
	private Calendar lastConnectionTime;

	public DeviceConnectionStateEvent( String deviceId, ConnectState cs, Calendar time )
	{
		super( DeviceConnectionStateEvent.class.getName(), deviceId );
		connectState = cs;
		lastConnectionTime = time;
	}

	public DeviceConnectionStateEvent( String deviceId, ConnectState cs )
	{
		super( DeviceConnectionStateEvent.class.getName(), deviceId );
		connectState = cs;
	}

	public ConnectState getConnectState()
	{
		return connectState;
	}

	public Calendar getLastConnectionTime()
	{
		return lastConnectionTime;
	}
}

