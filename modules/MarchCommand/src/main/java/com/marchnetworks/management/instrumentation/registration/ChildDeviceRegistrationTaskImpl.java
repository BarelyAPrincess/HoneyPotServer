package com.marchnetworks.management.instrumentation.registration;

import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.instrumentation.DeviceService;

public class ChildDeviceRegistrationTaskImpl extends BaseDeviceRegistrationTaskImpl
{
	private String channelId;

	public void run()
	{
		DeviceService deviceService = ( DeviceService ) ApplicationContextSupport.getBean( "deviceServiceProxy" );
		deviceService.addChannelToDevice( deviceId, channelId );
	}

	public String getChannelId()
	{
		return channelId;
	}

	public void setChannelId( String channelId )
	{
		this.channelId = channelId;
	}
}

