package com.marchnetworks.license.event;

import com.marchnetworks.command.api.event.AppNotifiable;
import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.command.common.license.data.AppLicenseInfo;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.common.event.EventTypesEnum;

public class AppLicenseEvent extends Event implements AppNotifiable
{
	protected AppLicenseEventType eventType;
	protected AppLicenseInfo appLicenseInfo;

	public AppLicenseEvent( AppLicenseEventType eventType, AppLicenseInfo appLicenseInfo )
	{
		super( AppLicenseEvent.class.getName() );
		this.eventType = eventType;
		this.appLicenseInfo = appLicenseInfo;
	}

	public String getEventNotificationType()
	{
		if ( eventType == AppLicenseEventType.UPDATED )
		{
			return EventTypesEnum.LICENSE_UPDATED.getFullPathEventName();
		}
		return EventTypesEnum.LICENSE_REMOVED.getFullPathEventName();
	}

	public EventNotification getNotificationInfo()
	{
		Builder builder = new Builder( getEventNotificationType() ).source( appLicenseInfo.getAppId() ).value( appLicenseInfo );

		EventNotification en = builder.build();
		return en;
	}
}
