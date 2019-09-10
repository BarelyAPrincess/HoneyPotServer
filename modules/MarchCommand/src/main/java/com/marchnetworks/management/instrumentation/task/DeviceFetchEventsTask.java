package com.marchnetworks.management.instrumentation.task;

import com.marchnetworks.command.common.scheduling.NonConcurrentRunnable;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.instrumentation.DeviceService;

public class DeviceFetchEventsTask implements NonConcurrentRunnable
{
	private String deviceId;
	private String deviceSubscriptionId;
	private String deviceRequestRemoteAddress;
	private long deviceTimestamp;
	private boolean hasEvents;

	public DeviceFetchEventsTask( String deviceId, String deviceSubscriptionId, String deviceRequestRemoteAddress, long deviceTimestamp, boolean hasEvents )
	{
		this.deviceId = deviceId;
		this.deviceSubscriptionId = deviceSubscriptionId;
		this.deviceRequestRemoteAddress = deviceRequestRemoteAddress;
		this.hasEvents = hasEvents;
		this.deviceTimestamp = deviceTimestamp;
	}

	public DeviceFetchEventsTask( String deviceId )
	{
		this.deviceId = deviceId;
	}

	public String getTaskId()
	{
		return DeviceFetchEventsTask.class.getSimpleName();
	}

	public void run()
	{
		DeviceService deviceService = ( DeviceService ) ApplicationContextSupport.getBean( "deviceServiceProxy" );
		deviceService.fetchEvents( deviceId, deviceSubscriptionId, deviceRequestRemoteAddress, deviceTimestamp, hasEvents );
	}
}

