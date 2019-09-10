package com.marchnetworks.management.firmware.task;

import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.firmware.data.ChannelGroupFirmware;
import com.marchnetworks.management.firmware.service.FirmwareService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelFirmwareUpgradeTask implements Runnable
{
	private static final Logger LOG = LoggerFactory.getLogger( ChannelFirmwareUpgradeTask.class );
	ChannelGroupFirmware channelFW;

	public ChannelFirmwareUpgradeTask( ChannelGroupFirmware channelFW )
	{
		this.channelFW = channelFW;
	}

	public void run()
	{
		LOG.debug( "Upgrading channel devices {}.", channelFW.getChannelDeviceIDs() );
		FirmwareService firmwareService = ( FirmwareService ) ApplicationContextSupport.getBean( "firmwareServiceProxy_internal" );
		firmwareService.applyMultipleChannelFirmware( channelFW );
	}
}

