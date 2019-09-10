package com.marchnetworks.server.communications.soap;

import java.util.List;

public class PortInitializerTask implements Runnable
{
	private List<String> urls;

	public PortInitializerTask( List<String> urls )
	{
		this.urls = urls;
	}

	public void run()
	{
		SoapProxyInvocationHandler.initializePorts( urls );
	}
}

