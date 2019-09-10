package com.marchnetworks.management.instrumentation.task;

import com.marchnetworks.management.instrumentation.DeviceService;

public class DeviceRetryReplacementTask implements Runnable
{
	private String deviceId;

	public DeviceRetryReplacementTask()
	{
	}

	public DeviceRetryReplacementTask( String deviceId )
	{
		this.deviceId = deviceId;
	}

	public void run()
	{
		DeviceService deviceService = ( DeviceService ) com.marchnetworks.common.spring.ApplicationContextSupport.getBean( "deviceServiceProxy" );
		deviceService.retryReplaceDevice( deviceId );
	}

	public String getDeviceId()
	{
		return deviceId;
	}
}

