package com.marchnetworks.management.config;

import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.config.service.ConfigService;
import com.marchnetworks.management.config.service.ConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceUnassociateImageTask implements Runnable
{
	private static final Logger LOG = LoggerFactory.getLogger( DeviceUnassociateImageTask.class );

	private ConfigService configService;
	private String deviceId;

	public DeviceUnassociateImageTask( String deviceId )
	{
		this.deviceId = deviceId;
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
		LOG.debug( "Unassociating device {}.", deviceId );
		try
		{
			getConfigService().unassignImage( deviceId );
		}
		catch ( ConfigurationException e )
		{
			LOG.warn( "Failed to unassign Image for device {} Message Details: {}", new Object[] {deviceId, e.getMessage()} );
		}
	}
}
