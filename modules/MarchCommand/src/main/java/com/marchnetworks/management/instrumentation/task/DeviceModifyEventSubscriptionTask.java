package com.marchnetworks.management.instrumentation.task;

import com.marchnetworks.command.common.scheduling.NonConcurrentRunnable;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.instrumentation.DeviceService;
import com.marchnetworks.management.instrumentation.data.DeviceSubscriptionType;

public class DeviceModifyEventSubscriptionTask implements NonConcurrentRunnable
{
	private String deviceId;
	DeviceSubscriptionType subscriptionType;

	public DeviceModifyEventSubscriptionTask( String deviceId, DeviceSubscriptionType subscriptionType )
	{
		this.deviceId = deviceId;
		this.subscriptionType = subscriptionType;
	}

	public String getTaskId()
	{
		return DeviceModifyEventSubscriptionTask.class.getSimpleName();
	}

	public void run()
	{
		DeviceService deviceService = ( DeviceService ) ApplicationContextSupport.getBean( "deviceServiceProxy" );
		deviceService.modifyDeviceSubscription( deviceId, subscriptionType );
	}
}

