package com.marchnetworks.management.instrumentation.task;

import com.marchnetworks.command.common.scheduling.NonConcurrentRunnable;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.instrumentation.DeviceService;

import java.util.Map;

public class DeviceSubscribeEventsTask implements NonConcurrentRunnable
{
	protected String deviceId;
	private Map<String, Object> extraInfoMap;

	public DeviceSubscribeEventsTask( String deviceId )
	{
		this.deviceId = deviceId;
	}

	public DeviceSubscribeEventsTask( String deviceId, Map<String, Object> extraInfoMap )
	{
		this.deviceId = deviceId;
		this.extraInfoMap = extraInfoMap;
	}

	public String getTaskId()
	{
		return DeviceSubscribeEventsTask.class.getSimpleName();
	}

	public void run()
	{
		DeviceService deviceService = ( DeviceService ) ApplicationContextSupport.getBean( "deviceServiceProxy" );
		deviceService.resubscribeDevice( deviceId, extraInfoMap );
	}

	public void setExtraInfoMap( Map<String, Object> extraInfoMap )
	{
		this.extraInfoMap = extraInfoMap;
	}
}

