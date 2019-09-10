package com.marchnetworks.app.event;

import com.marchnetworks.app.data.AppStatus;
import com.marchnetworks.app.events.AppEvent;
import com.marchnetworks.app.events.AppEventType;
import com.marchnetworks.app.events.AppStateEvent;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.server.event.AppEventService;
import com.marchnetworks.server.event.EventListener;

public class AppManagementEventHandler implements EventListener
{
	private AppEventService appEventService;

	public void process( Event aEvent )
	{
		if ( ( aEvent instanceof AppStateEvent ) )
		{
			AppStateEvent appEvent = ( AppStateEvent ) aEvent;
			if ( !appEvent.getStatus().equals( AppStatus.RUNNING ) )
			{
				appEventService.processAppStopped( appEvent.getAppID() );
			}
		}
		else if ( ( aEvent instanceof AppEvent ) )
		{
			AppEvent appEvent = ( AppEvent ) aEvent;
			if ( appEvent.getAppEventType() == AppEventType.UNINSTALLED )
			{
				appEventService.processAppStopped( appEvent.getAppID() );
			}
		}
	}

	public String getListenerName()
	{
		return AppManagementEventHandler.class.getSimpleName();
	}

	public void setAppEventService( AppEventService appEventService )
	{
		this.appEventService = appEventService;
	}
}
