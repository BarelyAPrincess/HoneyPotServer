package com.marchnetworks.management.topology;

import com.marchnetworks.alarm.model.AlarmSourceEntity;
import com.marchnetworks.command.api.query.Criteria;
import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.management.instrumentation.model.CompositeDevice;

import java.util.List;
import java.util.Map;

public abstract interface ResourceTopologyTestService
{
	public abstract void removeSimulatedDevices() throws TopologyException;

	public abstract List<Long> createSimulatedDevices( Long paramLong, List<CompositeDevice> paramList, Map<Long, List<AlarmSourceEntity>> paramMap );

	public abstract void createLogicalTree( Integer paramInteger );

	public abstract Map<String, Long> runBenchmark();

	public abstract Map<Class<?>, Integer> getResourceCount( Class<?>... paramVarArgs );

	public abstract Integer getResourceCount( Criteria paramCriteria );

	public abstract DeviceResource getLastDeviceResource();

	public abstract String getFirstChannelIdFromDevice( Long paramLong );
}

