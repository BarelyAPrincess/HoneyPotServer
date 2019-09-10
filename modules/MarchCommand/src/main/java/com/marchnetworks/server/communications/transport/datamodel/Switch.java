package com.marchnetworks.server.communications.transport.datamodel;

import com.marchnetworks.command.common.transport.data.Pair;

public class Switch implements DeviceOutput
{
	private String id;
	private String type;
	private String switchDeviceId;
	private String switchDeviceAddress;
	private String name;
	private String state;
	private Pair[] info;

	public String getId()
	{
		return id;
	}

	public void setId( String id )
	{
		this.id = id;
	}

	public String getType()
	{
		return type;
	}

	public void setType( String type )
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

	public String getState()
	{
		return state;
	}

	public void setState( String state )
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

