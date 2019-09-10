package com.marchnetworks.server.communications.transport.datamodel;

public class RegistrationDetails
{
	boolean isRegistered;

	String registeredServer;

	String registeredServerPath;

	String registeredDeviceId;

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

