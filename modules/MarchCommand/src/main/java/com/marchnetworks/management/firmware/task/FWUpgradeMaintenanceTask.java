package com.marchnetworks.management.firmware.task;

import com.marchnetworks.management.firmware.service.FirmwareService;

public class FWUpgradeMaintenanceTask
{
	private FirmwareService firmwareService;

	public void upgradeMaintenanceCheck()
	{
		firmwareService.checkUpgradeTaskTimeout();
	}

	public void setFirmwareService( FirmwareService firmwareService )
	{
		this.firmwareService = firmwareService;
	}
}

