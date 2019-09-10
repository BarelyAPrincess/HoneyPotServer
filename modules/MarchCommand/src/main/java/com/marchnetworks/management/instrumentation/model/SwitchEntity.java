package com.marchnetworks.management.instrumentation.model;

import com.marchnetworks.command.common.device.data.DeviceOutputView;
import com.marchnetworks.command.common.device.data.SwitchState;
import com.marchnetworks.command.common.device.data.SwitchType;
import com.marchnetworks.command.common.device.data.SwitchView;
import com.marchnetworks.server.communications.transport.datamodel.DeviceOutput;
import com.marchnetworks.server.communications.transport.datamodel.Switch;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Entity
@DiscriminatorValue( "switch" )
public class SwitchEntity extends DeviceOutputEntity
{
	@Column( name = "SWITCH_TYPE" )
	@Enumerated( EnumType.STRING )
	private SwitchType type;
	@Column( name = "SWITCH_STATE" )
	@Enumerated( EnumType.STRING )
	private SwitchState state;

	public SwitchState getState()
	{
		return state;
	}

	public void setState( SwitchState state )
	{
		this.state = state;
	}

	public SwitchType getType()
	{
		return type;
	}

	public void setType( SwitchType type )
	{
		this.type = type;
	}

	public DeviceOutputView toDataObject()
	{
		SwitchView view = new SwitchView( outputId, type, outputDeviceId, outputDeviceAddress, name, state, getInfoAsPairs() );
		return view;
	}

	public boolean readFromTransportObject( DeviceOutput deviceOutput )
	{
		boolean updated = false;
		if ( ( deviceOutput != null ) && ( ( deviceOutput instanceof Switch ) ) )
		{
			Switch switchData = ( Switch ) deviceOutput;

			SwitchType type = SwitchType.fromValue( switchData.getType() );
			String switchDeviceId = switchData.getSwitchDeviceId();
			String switchDeviceAddress = switchData.getSwitchDeviceAddress();
			String name = switchData.getName();

			if ( ( type != this.type ) || ( !switchDeviceId.equals( outputDeviceId ) ) || ( !switchDeviceAddress.equals( outputDeviceAddress ) ) || ( !name.equals( this.name ) ) )
			{
				this.type = type;
				outputDeviceId = switchDeviceId;
				outputDeviceAddress = switchDeviceAddress;
				this.name = name;
				updated = true;
			}
		}
		return updated;
	}

	public Class<SwitchView> getDataObjectClass()
	{
		return SwitchView.class;
	}
}

