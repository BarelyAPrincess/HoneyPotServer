package com.marchnetworks.command.common.device.data;

import com.marchnetworks.command.common.transport.data.Pair;

import javax.xml.bind.annotation.XmlElement;

public class SwitchView implements DeviceOutputView
{
	private String switchId;
	private SwitchType type;
	private String switchDeviceId;
	private String switchDeviceAddress;
	private String name;
	private SwitchState state;
	private Pair[] info;

	public SwitchView()
	{
	}

	public SwitchView( String switchId, SwitchType type, String switchDeviceId, String switchDeviceAddress, String name, SwitchState state, Pair[] info )
	{
		this.switchId = switchId;
		this.type = type;
		this.switchDeviceId = switchDeviceId;
		this.switchDeviceAddress = switchDeviceAddress;
		this.name = name;
		this.state = state;
		this.info = info;
	}

	public String getSwitchId()
	{
		return switchId;
	}

	public void setSwitchId( String switchId )
	{
		this.switchId = switchId;
	}

	@XmlElement( required = true )
	public SwitchType getType()
	{
		return type;
	}

	public void setType( SwitchType type )
	{
		this.type = type;
	}

	public String getSwitchDeviceId()
	{
		return switchDeviceId;
	}

	public void setSwitchDeviceId( String switchDeviceId )
	{
		this.switchDeviceId = switchDeviceId;
	}

	public String getSwitchDeviceAddress()
	{
		return switchDeviceAddress;
	}

	public void setSwitchDeviceAddress( String switchDeviceAddress )
	{
		this.switchDeviceAddress = switchDeviceAddress;
	}

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	@XmlElement( required = true )
	public SwitchState getState()
	{
		return state;
	}

	public void setState( SwitchState state )
	{
		this.state = state;
	}

	public Pair[] getInfo()
	{
		return info;
	}

	public void setInfo( Pair[] info )
	{
		this.info = info;
	}
}
