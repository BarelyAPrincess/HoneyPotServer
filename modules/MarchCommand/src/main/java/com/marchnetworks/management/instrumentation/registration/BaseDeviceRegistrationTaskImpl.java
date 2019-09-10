package com.marchnetworks.management.instrumentation.registration;

import com.marchnetworks.command.common.topology.data.DeviceResource;

import java.util.Map;

public abstract class BaseDeviceRegistrationTaskImpl implements Runnable
{
	protected String deviceId;
	protected DeviceResource deviceResource;
	protected Map<String, Object> additionalDeviceRegistrationInfo;

	public String getDeviceId()
	{
		return deviceId;
	}

	public DeviceResource getDeviceResource()
	{
		return deviceResource;
	}

	public void setDeviceResource( DeviceResource deviceResource )
	{
		this.deviceResource = deviceResource;
	}

	public void setDeviceId( String deviceId )
	{
		this.deviceId = deviceId;
	}

	public Map<String, Object> getAdditionalDeviceRegistrationInfo()
	{
		return additionalDeviceRegistrationInfo;
	}

	public void setAdditionalDeviceRegistrationInfo( Map<String, Object> additionalDeviceRegistrationInfo )
	{
		this.additionalDeviceRegistrationInfo = additionalDeviceRegistrationInfo;
	}
}

