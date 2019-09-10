package com.marchnetworks.command.common.simulator;

public class DeviceInfo
{
	private Long resourceId;

	private Long deviceId;

	private String subscriptionId;

	public DeviceInfo( Long resourceId, Long deviceId, String subscriptionId )
	{
		this.resourceId = resourceId;
		this.deviceId = deviceId;
		this.subscriptionId = subscriptionId;
	}

	public Long getResourceId()
	{
		return resourceId;
	}

	public void setResourceId( Long resourceId )
	{
		this.resourceId = resourceId;
	}

	public Long getDeviceId()
	{
		return deviceId;
	}

	public void setDeviceId( Long deviceId )
	{
		this.deviceId = deviceId;
	}

	public String getSubscriptionId()
	{
		return subscriptionId;
	}

	public void setSubscriptionId( String subscriptionId )
	{
		this.subscriptionId = subscriptionId;
	}
}
