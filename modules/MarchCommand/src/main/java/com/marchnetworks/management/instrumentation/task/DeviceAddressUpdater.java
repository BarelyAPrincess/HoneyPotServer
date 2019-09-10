package com.marchnetworks.management.instrumentation.task;

import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.instrumentation.DeviceService;

public class DeviceAddressUpdater implements Runnable
{
	private String deviceId;
	private String deviceAddress;

	public DeviceAddressUpdater()
	{
	}

	public DeviceAddressUpdater( String deviceId, String deviceAddress )
	{
		this.deviceId = deviceId;
		this.deviceAddress = deviceAddress;
	}

	public void run()
	{
		DeviceService deviceService = ( DeviceService ) ApplicationContextSupport.getBean( "deviceServiceProxy" );
		deviceService.updateDeviceAddress( deviceId, deviceAddress );
	}

	public String getDeviceId()
	{
		return deviceId;
	}

	public String getDeviceAddress()
	{
		return deviceAddress;
	}
}

