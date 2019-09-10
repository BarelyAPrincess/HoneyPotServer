package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "RegistrationDetails", propOrder = {"isRegistered", "registeredServer", "registeredServerPath", "registeredDeviceId"} )
public class RegistrationDetails
{
	protected boolean isRegistered;
	@XmlElement( required = true )
	protected String registeredServer;
	@XmlElement( required = true )
	protected String registeredServerPath;
	@XmlElement( required = true )
	protected String registeredDeviceId;

	public boolean isIsRegistered()
	{
		return isRegistered;
	}

	public void setIsRegistered( boolean value )
	{
		isRegistered = value;
	}

	public String getRegisteredServer()
	{
		return registeredServer;
	}

	public void setRegisteredServer( String value )
	{
		registeredServer = value;
	}

	public String getRegisteredServerPath()
	{
		return registeredServerPath;
	}

	public void setRegisteredServerPath( String value )
	{
		registeredServerPath = value;
	}

	public String getRegisteredDeviceId()
	{
		return registeredDeviceId;
	}

	public void setRegisteredDeviceId( String value )
	{
		registeredDeviceId = value;
	}
}
