package com.marchnetworks.management.instrumentation.registration;

import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.instrumentation.DeviceService;

public class DeviceReplacementTaskImpl extends BaseDeviceRegistrationTaskImpl
{
	public void run()
	{
		DeviceService deviceService = ( DeviceService ) ApplicationContextSupport.getBean( "deviceServiceProxy" );
		deviceService.replaceDevice( deviceId, additionalDeviceRegistrationInfo );
	}
}

