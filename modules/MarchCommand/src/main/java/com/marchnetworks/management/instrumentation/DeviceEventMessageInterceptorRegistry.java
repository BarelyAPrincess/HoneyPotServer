package com.marchnetworks.management.instrumentation;

import java.util.List;

public class DeviceEventMessageInterceptorRegistry
{
	private List<DeviceEventMessageInterceptor> interceptors;

	public List<DeviceEventMessageInterceptor> getInterceptors()
	{
		return interceptors;
	}

	public void setInterceptors( List<DeviceEventMessageInterceptor> interceptors )
	{
		this.interceptors = interceptors;
	}
}

