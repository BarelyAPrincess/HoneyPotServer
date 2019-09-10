package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "Switch", propOrder = {"id", "type", "switchDeviceId", "switchDeviceAddress", "name", "state", "info"} )
public class Switch
{
	@XmlElement( required = true )
	protected String id;
	@XmlElement( required = true )
	protected String type;
	@XmlElement( required = true )
	protected String switchDeviceId;
	@XmlElement( required = true )
	protected String switchDeviceAddress;
	@XmlElement( required = true )
	protected String name;
	@XmlElement( required = true )
	protected String state;
	protected ArrayOfPair info;

	public String getId()
	{
		return id;
	}

	public void setId( String value )
	{
		id = value;
	}

	public String getType()
	{
		return type;
	}

	public void setType( String value )
	{
		type = value;
	}

	public String getSwitchDeviceId()
	{
		return switchDeviceId;
	}

	public void setSwitchDeviceId( String value )
	{
		switchDeviceId = value;
	}

	public String getSwitchDeviceAddress()
	{
		return switchDeviceAddress;
	}

	public void setSwitchDeviceAddress( String value )
	{
		switchDeviceAddress = value;
	}

	public String getName()
	{
		return name;
	}

	public void setName( String value )
	{
		name = value;
	}

	public String getState()
	{
		return state;
	}

	public void setState( String value )
	{
		state = value;
	}

	public ArrayOfPair getInfo()
	{
		return info;
	}

	public void setInfo( ArrayOfPair value )
	{
		info = value;
	}
}
