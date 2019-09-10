package com.marchnetworks.management.firmware.task;

import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.firmware.service.FirmwareService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FirmwareUpgradeTask implements Runnable
{
	private static final Logger LOG = LoggerFactory.getLogger( FirmwareUpgradeTask.class );

	private String deviceId;
	private String firmwareFileObjectId;
	private String optParams;

	public FirmwareUpgradeTask( String deviceId, String firmwareFileObjectId, String optParams )
	{
		this.deviceId = deviceId;
		this.firmwareFileObjectId = firmwareFileObjectId;
		this.optParams = optParams;
	}

	public void run()
	{
		LOG.debug( "Upgrading device {}.", deviceId );
		FirmwareService firmwareService = ( FirmwareService ) ApplicationContextSupport.getBean( "firmwareServiceProxy_internal" );
		firmwareService.applyFirmware( deviceId, firmwareFileObjectId, optParams );
	}
}

