package com.marchnetworks.common.transport;

import com.marchnetworks.common.diagnostics.DiagnosticSettings;

public class DeviceServiceTransportSettings
{
	public static final String DEVICE_SERVICE_TRANSPORT = "https";
	public static final int RESTFUL_TIMEOUT = 30;
	public static final String RESTFUL_SERVICE_CONTEXT = "/device";
	public static final String TOKEN_SERVICE_CONTEXT = "/tokens/local";

	public static String getServiceURL( String deviceAddress )
	{
		return getTransport() + "://" + deviceAddress;
	}

	public static String getRestfulServiceURL( String deviceAddress )
	{
		return getTransport() + "://" + getRestAddress( deviceAddress ) + "/device";
	}

	public static String getDeviceTokenServiceURL( String deviceAddress )
	{
		return getTransport() + "://" + getRestAddress( deviceAddress ) + "/tokens/local";
	}

	private static String getTransport()
	{
		String transport = "https";
		transport = DiagnosticSettings.onGetTransport( transport );
		return transport;
	}

	private static String getRestAddress( String address )
	{
		return DiagnosticSettings.onGetAddress( address );
	}
}
