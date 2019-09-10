package com.marchnetworks.management.instrumentation.events;

import com.marchnetworks.management.instrumentation.data.DeviceSubscriptionType;

public class DeviceModifySubscriptionEvent extends AbstractDeviceEvent
{
	private DeviceSubscriptionType subscriptionType;

	public DeviceModifySubscriptionEvent( String deviceId, DeviceSubscriptionType subscriptionType )
	{
		super( DeviceModifySubscriptionEvent.class.getName(), deviceId );
		this.subscriptionType = subscriptionType;
	}

	public DeviceSubscriptionType getSubscriptionType()
	{
		return subscriptionType;
	}
}

