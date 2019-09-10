package com.marchnetworks.management.instrumentation.registration;

import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.transport.datamodel.DeviceException;
import com.marchnetworks.management.instrumentation.DeviceService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceUnregistrationTaskImpl extends BaseDeviceRegistrationTaskImpl
{
	private static final Logger LOG = LoggerFactory.getLogger( DeviceUnregistrationTaskImpl.class );

	public void run()
	{
		LOG.debug( "{}: Unregistering {}.", this, deviceResource.getDeviceId() );
		try
		{
			long start = System.currentTimeMillis();
			DeviceService deviceService = ( DeviceService ) ApplicationContextSupport.getBean( "deviceServiceProxy" );
			deviceService.unregister( deviceResource );
			LOG.info( "Device {} unregistered in {} ms.", deviceResource.getDeviceId(), Long.valueOf( System.currentTimeMillis() - start ) );
		}
		catch ( DeviceException ex )
		{
			LOG.error( "Could not unregister " + deviceResource.getDeviceId() + ". Error: " + ex.getMessage(), ex );
		}
	}
}

