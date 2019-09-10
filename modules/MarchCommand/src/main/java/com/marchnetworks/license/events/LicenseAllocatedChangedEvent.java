package com.marchnetworks.license.events;

import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.common.event.EventTypesEnum;
import com.marchnetworks.license.model.LicenseType;

public class LicenseAllocatedChangedEvent extends LicenseExternalEvent
{
	private LicenseType m_licenseType;
	private int m_iAllocated;

	public LicenseAllocatedChangedEvent( LicenseType t, int allocatedCount )
	{
		super( LicenseExternalEvent.class.getName() );
		m_licenseType = t;
		m_iAllocated = allocatedCount;
	}

	public String getEventNotificationType()
	{
		return EventTypesEnum.LICENSE_COUNT_ALLOCATED.getFullPathEventName();
	}

	public EventNotification getNotificationInfo()
	{
		return new Builder( getEventNotificationType() ).source( m_licenseType.toString() ).value( Integer.valueOf( m_iAllocated ) ).build();
	}
}
