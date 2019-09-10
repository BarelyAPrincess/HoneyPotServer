package com.marchnetworks.management.instrumentation;

import com.marchnetworks.command.common.device.data.SwitchState;
import com.marchnetworks.command.common.device.data.SwitchType;
import com.marchnetworks.command.common.topology.ResourceAssociationType;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.command.common.topology.data.SwitchResource;
import com.marchnetworks.common.transport.datamodel.DeviceException;
import com.marchnetworks.management.instrumentation.model.SwitchEntity;
import com.marchnetworks.server.communications.transport.datamodel.DeviceOutput;
import com.marchnetworks.server.communications.transport.datamodel.Switch;

import java.util.List;

public class DeviceSwitchServiceImpl extends DeviceOutputServiceImpl<SwitchEntity> implements DeviceSwitchService
{
	protected List<? extends DeviceOutput> getOutputs( Long deviceId ) throws DeviceException
	{
		RemoteCompositeDeviceOperations adaptor = getAdaptor( deviceId );
		return adaptor.getSwitches();
	}

	protected Resource createResource( SwitchEntity outputEntity )
	{
		SwitchResource result = new SwitchResource();
		result.setSwitchId( outputEntity.getId() );
		return result;
	}

	protected SwitchEntity createEntity( Long deviceId, DeviceOutput deviceOutput )
	{
		if ( ( deviceOutput instanceof Switch ) )
		{
			Switch switchOutput = ( Switch ) deviceOutput;
			SwitchEntity switchEntity = new SwitchEntity();
			switchEntity.setOutputId( switchOutput.getId() );
			switchEntity.setDeviceId( deviceId );
			switchEntity.setType( SwitchType.fromValue( switchOutput.getType() ) );
			switchEntity.setOutputDeviceId( switchOutput.getSwitchDeviceId() );
			switchEntity.setOutputDeviceAddress( switchOutput.getSwitchDeviceAddress() );
			switchEntity.setName( switchOutput.getName() );
			switchEntity.setState( SwitchState.fromValue( switchOutput.getState() ) );
			switchEntity.setInfo( switchOutput.getInfo() );
			return switchEntity;
		}
		return null;
	}

	protected String getResourceAssociationType()
	{
		return ResourceAssociationType.SWITCH.name();
	}

	protected void updateState( SwitchEntity outputEntity, String state )
	{
		outputEntity.setState( SwitchState.fromValue( state ) );
	}

	protected boolean isDisabled( String state )
	{
		return SwitchState.DISABLED.getValue().equals( state );
	}
}

