package com.marchnetworks.audit.event;

import com.marchnetworks.app.events.AppEvent;
import com.marchnetworks.app.events.AppEventType;
import com.marchnetworks.audit.service.AuditLogService;
import com.marchnetworks.command.common.device.data.RegistrationStatus;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.management.instrumentation.events.DeviceRegistrationEvent;
import com.marchnetworks.server.event.EventListener;

public class AuditEventHandler implements EventListener
{
	private AuditLogService auditService;

	public void process( Event event )
	{
		if ( ( event instanceof DeviceRegistrationEvent ) )
		{
			DeviceRegistrationEvent registrationEvent = ( DeviceRegistrationEvent ) event;
			if ( registrationEvent.getRegistrationStatus() == RegistrationStatus.UNREGISTERED )
			{
				auditService.processDeviceUnregistration( registrationEvent );
			}
		}
		else if ( ( event instanceof AppEvent ) )
		{
			AppEvent appEvent = ( AppEvent ) event;
			if ( appEvent.getAppEventType() == AppEventType.UNINSTALLED )
			{
				auditService.deleteAuditLogsByAppid( appEvent.getAppID() );
			}
		}
		else
		{
			auditService.logAuditEvent( event );
		}
	}

	public String getListenerName()
	{
		return AuditEventHandler.class.getSimpleName();
	}

	public void setAuditService( AuditLogService auditService )
	{
		this.auditService = auditService;
	}
}
