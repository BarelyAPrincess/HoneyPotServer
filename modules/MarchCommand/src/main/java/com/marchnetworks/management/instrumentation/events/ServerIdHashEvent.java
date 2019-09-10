package com.marchnetworks.management.instrumentation.events;

public class ServerIdHashEvent extends AbstractDeviceEvent
{
	public ServerIdHashEvent( String deviceId )
	{
		super( ServerIdHashEvent.class.getName(), deviceId );
	}
}

