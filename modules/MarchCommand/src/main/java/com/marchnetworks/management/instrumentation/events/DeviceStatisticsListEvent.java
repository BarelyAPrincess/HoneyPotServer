package com.marchnetworks.management.instrumentation.events;

import com.marchnetworks.common.event.StateCacheable;

import java.util.List;

public class DeviceStatisticsListEvent extends AbstractDeviceEvent
{
	private List<StateCacheable> stateEvents;

	public DeviceStatisticsListEvent( String deviceId, List<StateCacheable> stateEvents )
	{
		super( DeviceStatisticsListEvent.class.getName(), deviceId );
		this.stateEvents = stateEvents;
	}

	public List<StateCacheable> getStateEvents()
	{
		return stateEvents;
	}
}

