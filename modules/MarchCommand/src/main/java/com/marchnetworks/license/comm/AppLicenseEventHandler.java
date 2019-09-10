package com.marchnetworks.license.comm;

import com.marchnetworks.command.common.device.data.RegistrationStatus;
import com.marchnetworks.command.common.topology.data.GenericResource;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.license.service.AppLicenseService;
import com.marchnetworks.management.instrumentation.events.DeviceChannelRemovedEvent;
import com.marchnetworks.management.instrumentation.events.DeviceRegistrationEvent;
import com.marchnetworks.management.topology.events.ResourceRemovedEvent;
import com.marchnetworks.server.event.EventListener;

public class AppLicenseEventHandler implements EventListener
{
	private AppLicenseService appLicenseService;

	public String getListenerName()
	{
		return AppLicenseEventHandler.class.getSimpleName();
	}

	public void process( Event aEvent )
	{
		if ( ( aEvent instanceof DeviceRegistrationEvent ) )
		{
			DeviceRegistrationEvent deviceRegistrationEvent = ( DeviceRegistrationEvent ) aEvent;
			RegistrationStatus status = deviceRegistrationEvent.getRegistrationStatus();

			if ( status == RegistrationStatus.UNREGISTERED )
			{
				getAppLicenseService().processDeviceUnregistered( deviceRegistrationEvent.getDeviceId() );
			}
		}
		else if ( ( aEvent instanceof DeviceChannelRemovedEvent ) )
		{
			DeviceChannelRemovedEvent channelEvent = ( DeviceChannelRemovedEvent ) aEvent;
			getAppLicenseService().processChannelRemoved( channelEvent.getDeviceId(), channelEvent.getChannelId() );
		}
		else if ( ( aEvent instanceof ResourceRemovedEvent ) )
		{
			ResourceRemovedEvent resourceRemovedEvent = ( ResourceRemovedEvent ) aEvent;
			Resource resource = resourceRemovedEvent.getResource();
			if ( ( resource instanceof GenericResource ) )
			{
				getAppLicenseService().processGenericResourceRemoved( ( GenericResource ) resource );
			}
		}
	}

	public AppLicenseService getAppLicenseService()
	{
		if ( appLicenseService == null )
		{
			appLicenseService = ( ( AppLicenseService ) ApplicationContextSupport.getBean( "appLicenseServiceProxy" ) );
		}
		return appLicenseService;
	}
}
