package com.marchnetworks.web.test;

import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.instrumentation.subscription.DeviceSubscriptionManager;

public class CheckOfflineThread implements Runnable
{
	private static final DeviceSubscriptionManager deviceSubscriptionManager = ( DeviceSubscriptionManager ) ApplicationContextSupport.getBean( "deviceSubscriptionManager" );

	public void run()
	{
		deviceSubscriptionManager.checkForOfflineDevices();
	}
}
