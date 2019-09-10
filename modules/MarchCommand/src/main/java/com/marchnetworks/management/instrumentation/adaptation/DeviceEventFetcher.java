package com.marchnetworks.management.instrumentation.adaptation;

import java.util.Map;

public abstract interface DeviceEventFetcher
{
	public abstract boolean processFetchNotification( Map<String, String> paramMap );
}

