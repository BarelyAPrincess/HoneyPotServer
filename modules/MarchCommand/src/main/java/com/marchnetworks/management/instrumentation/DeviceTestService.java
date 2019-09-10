package com.marchnetworks.management.instrumentation;

import com.marchnetworks.command.common.simulator.DeviceInfo;
import com.marchnetworks.command.common.simulator.DeviceSpecification;
import com.marchnetworks.command.common.transport.data.Event;

import java.util.List;

public abstract interface DeviceTestService
{
	public abstract List<DeviceInfo> createSimulatedDevices( Long paramLong, DeviceSpecification paramDeviceSpecification, String paramString );

	public abstract void sendDevicesToSimulator( List<DeviceInfo> paramList, int paramInt1, int paramInt2 );

	public abstract void removeSimulatedDevices();

	public abstract boolean registerSimulator( String paramString );

	public abstract boolean unregisterSimulator();

	public abstract SimulatorInfo getSimulatorInfo();

	public abstract void injectSimulatedAlarms( int paramInt1, int paramInt2, int paramInt3 );

	public abstract void injectDeviceEvent( Long paramLong, Event paramEvent );

	public abstract void injectDeviceEvents( Long paramLong, List<Event> paramList );

	public abstract void injectSimulatedAlerts( int paramInt1, int paramInt2, int paramInt3 );

	public abstract void testBlockDatabaseConnection( int paramInt );

	public abstract void updateTimeDelta( String paramString, long paramLong );
}

