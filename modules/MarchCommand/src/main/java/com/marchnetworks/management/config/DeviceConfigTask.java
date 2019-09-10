package com.marchnetworks.management.config;

import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.config.service.ConfigService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceConfigTask implements Runnable
{
	private static final Logger LOG = LoggerFactory.getLogger( DeviceConfigTask.class );

	private ConfigService configService;

	private String deviceId;
	private String imageId;

	public DeviceConfigTask( String deviceId, String imageId )
	{
		this.deviceId = deviceId;
		this.imageId = imageId;
	}

	private ConfigService getConfigService()
	{
		if ( configService == null )
		{
			configService = ( ( ConfigService ) ApplicationContextSupport.getBean( "configServiceProxy_internal" ) );
		}
		return configService;
	}

	public void run()
	{
		LOG.debug( "Configuring device {} with image {}.", new Object[] {deviceId, imageId} );
		getConfigService().applyImage( deviceId, imageId );
	}
}
