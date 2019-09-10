package com.marchnetworks.management.user.events;

import com.marchnetworks.common.event.Event;
import com.marchnetworks.security.authentication.SessionService;
import com.marchnetworks.server.event.EventListener;

public class UserLogoffEventHandler implements EventListener
{
	private SessionService sessionService;

	public void process( Event aEvent )
	{
		if ( ( aEvent instanceof UserLogoffEvent ) )
		{
			sessionService.deleteSessionsByPrincipalName( ( ( UserLogoffEvent ) aEvent ).getUser() );
		}
	}

	public String getListenerName()
	{
		return UserLogoffEventHandler.class.getSimpleName();
	}

	public void setSessionService( SessionService sessionService )
	{
		this.sessionService = sessionService;
	}
}

