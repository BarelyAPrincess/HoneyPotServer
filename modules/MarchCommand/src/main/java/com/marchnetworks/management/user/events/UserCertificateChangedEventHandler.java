package com.marchnetworks.management.user.events;

import com.marchnetworks.common.event.Event;
import com.marchnetworks.common.event.EventTypesEnum;
import com.marchnetworks.management.file.events.FileStorageEvent;
import com.marchnetworks.management.user.UserService;
import com.marchnetworks.server.event.EventListener;

public class UserCertificateChangedEventHandler implements EventListener
{
	private UserService userService;

	public void process( Event aEvent )
	{
		if ( ( ( aEvent instanceof FileStorageEvent ) ) && ( ( ( FileStorageEvent ) aEvent ).getEventNotificationType().equals( EventTypesEnum.CERTIFICATE_REMOVED.getFullPathEventName() ) ) )
		{
			userService.certRemoved( ( ( FileStorageEvent ) aEvent ).getFileStorageId() );
		}
	}

	public void setUserService( UserService userService )
	{
		this.userService = userService;
	}

	public String getListenerName()
	{
		return UserCertificateChangedEventHandler.class.getSimpleName();
	}
}

