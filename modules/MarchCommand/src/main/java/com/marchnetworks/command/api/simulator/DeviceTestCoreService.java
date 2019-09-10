package com.marchnetworks.command.api.simulator;

import com.marchnetworks.command.common.simulator.DeviceInfo;
import com.marchnetworks.command.common.simulator.DeviceSpecification;

import java.util.List;

public interface DeviceTestCoreService
{
	List<DeviceInfo> createSimulatedDevices( Long paramLong, DeviceSpecification paramDeviceSpecification, String paramString );
}
