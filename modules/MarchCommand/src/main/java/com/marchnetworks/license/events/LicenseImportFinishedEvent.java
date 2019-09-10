package com.marchnetworks.license.events;

import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.common.event.EventTypesEnum;

public class LicenseImportFinishedEvent extends LicenseExternalEvent
{
	public LicenseImportFinishedEvent()
	{
		super( LicenseExternalEvent.class.getName() );
	}

	public String getEventNotificationType()
	{
		return EventTypesEnum.LICENSE_IMPORT_FINISHED.getFullPathEventName();
	}

	public EventNotification getNotificationInfo()
	{
		return new Builder( getEventNotificationType() ).source( "" ).build();
	}
}
