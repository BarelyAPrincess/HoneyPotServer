package com.marchnetworks.management.topology.event;

import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.common.device.data.RegistrationStatus;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.management.instrumentation.events.DeviceRegistrationEvent;
import com.marchnetworks.management.topology.ArchiverAssociationService;
import com.marchnetworks.server.event.EventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArchiverAssociationListener implements EventListener
{
	private static final Logger LOG = LoggerFactory.getLogger( ArchiverAssociationListener.class );

	private ArchiverAssociationService archiverAssociationService;

	public void process( Event event )
	{
		RegistrationStatus status = null;
		if ( ( event instanceof DeviceRegistrationEvent ) )
		{
			LOG.debug( "Processing event {}", event );
			DeviceRegistrationEvent deviceEvent = ( DeviceRegistrationEvent ) event;

			status = deviceEvent.getRegistrationStatus();
			if ( status.equals( RegistrationStatus.UNREGISTERED ) )
			{
				EventNotification eventNotification = deviceEvent.getNotificationInfo();
				String resourceid = eventNotification.getInfo( "CES_DEVICE_RESOURCE_ID" );
				if ( resourceid != null )
				{
					archiverAssociationService.removeDevice( Long.valueOf( Long.parseLong( resourceid ) ) );
				}
			}
		}
	}

	public String getListenerName()
	{
		return ArchiverAssociationListener.class.getSimpleName();
	}

	public void setArchiverAssociationService( ArchiverAssociationService archiverAssociationService )
	{
		this.archiverAssociationService = archiverAssociationService;
	}
}

