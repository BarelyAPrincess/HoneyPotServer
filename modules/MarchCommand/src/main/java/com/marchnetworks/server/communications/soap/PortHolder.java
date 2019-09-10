package com.marchnetworks.server.communications.soap;

import com.marchnetworks.device_ws.DeviceServiceSoap;

public class PortHolder
{
	private DeviceServiceSoap port;

	public DeviceServiceSoap getPort()
	{
		if ( port == null )
		{
			port = SoapProxyInvocationHandler.SERVICE.getDeviceServiceSoap();
		}
		return port;
	}

	public boolean isInitialized()
	{
		return port != null;
	}
}

