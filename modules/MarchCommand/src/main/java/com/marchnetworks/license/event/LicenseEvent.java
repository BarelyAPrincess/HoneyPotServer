package com.marchnetworks.license.event;

import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.command.api.event.Notifiable;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.common.event.EventTypesEnum;

public class LicenseEvent extends Event implements Notifiable
{
	protected LicenseEventType eventType;
	protected String licenseID;

	public LicenseEvent( LicenseEventType eventType, String licenseID )
	{
		super( LicenseEvent.class.getName() );
		this.eventType = eventType;
		this.licenseID = licenseID;
	}

	public LicenseEvent( String type, LicenseEventType eventType )
	{
		super( type );
		this.eventType = eventType;
	}

	public String getEventNotificationType()
	{
		if ( eventType == LicenseEventType.UPDATED )
			return EventTypesEnum.LICENSE_UPDATED.getFullPathEventName();
		if ( eventType == LicenseEventType.REMOVED )
		{
			return EventTypesEnum.LICENSE_REMOVED.getFullPathEventName();
		}
		return EventTypesEnum.LICENSE_INVALID.getFullPathEventName();
	}

	public EventNotification getNotificationInfo()
	{
		Builder builder = new Builder( getEventNotificationType() ).source( licenseID );

		EventNotification en = builder.build();
		return en;
	}
}
