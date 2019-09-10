package com.marchnetworks.management.instrumentation.registration;

import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.instrumentation.DeviceService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceRegistrationTaskImpl extends BaseDeviceRegistrationTaskImpl
{
	private static final Logger LOG = LoggerFactory.getLogger( DeviceRegistrationTaskImpl.class );

	public void run()
	{
		LOG.debug( "{}: Registering {}.", this, deviceId );

		DeviceService deviceService = ( DeviceService ) ApplicationContextSupport.getBean( "deviceServiceProxy" );
		deviceService.beginRegister( deviceId );
		deviceService.register( deviceId, additionalDeviceRegistrationInfo );
	}
}

