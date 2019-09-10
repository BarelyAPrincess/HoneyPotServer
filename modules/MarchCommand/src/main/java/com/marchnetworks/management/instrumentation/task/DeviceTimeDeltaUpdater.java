package com.marchnetworks.management.instrumentation.task;

import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.instrumentation.DeviceService;

public class DeviceTimeDeltaUpdater implements Runnable
{
	private String deviceId;
	private long timeDelta;

	public DeviceTimeDeltaUpdater()
	{
	}

	public DeviceTimeDeltaUpdater( String deviceId, long timeDelta )
	{
		this.deviceId = deviceId;
		this.timeDelta = timeDelta;
	}

	public void run()
	{
		DeviceService deviceService = ( DeviceService ) ApplicationContextSupport.getBean( "deviceServiceProxy" );
		deviceService.updateDeviceTimeDeltaAsync( deviceId, timeDelta );
	}
}

