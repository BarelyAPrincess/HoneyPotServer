package com.marchnetworks.command.common.topology.data;

import com.marchnetworks.command.common.device.data.DeviceView;

public class DeviceResource extends Resource
{
	private String deviceId;
	private DeviceView deviceView;

	public void update( Resource updatedResource )
	{
		if ( ( updatedResource instanceof DeviceResource ) )
		{
			super.update( updatedResource );
			DeviceResource updatedDeviceResource = ( DeviceResource ) updatedResource;
			deviceId = updatedDeviceResource.getDeviceId();
			deviceView = updatedDeviceResource.getDeviceView();
		}
	}

	public DeviceView getDeviceView()
	{
		return deviceView;
	}

	public void setDeviceView( DeviceView deviceView )
	{
		this.deviceView = deviceView;
	}

	public String getDeviceId()
	{
		return deviceId;
	}

	public void setDeviceId( String deviceId )
	{
		this.deviceId = deviceId;
	}

	public boolean isRootDevice()
	{
		if ( deviceView == null )
		{
			return false;
		}
		return deviceView.getParentDeviceId() == null;
	}
}
