package com.marchnetworks.management.user.events;

import com.marchnetworks.app.data.AppStatus;
import com.marchnetworks.app.events.AppEvent;
import com.marchnetworks.app.events.AppEventType;
import com.marchnetworks.app.events.AppStateEvent;
import com.marchnetworks.command.common.topology.data.Group;
import com.marchnetworks.command.common.topology.data.LinkResource;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.management.topology.events.ResourceAssociationChangedEvent;
import com.marchnetworks.management.topology.events.ResourceRemovedEvent;
import com.marchnetworks.management.topology.events.TopologyEvent;
import com.marchnetworks.management.user.UserService;
import com.marchnetworks.server.event.EventListener;

public class UserEventHandler implements EventListener
{
	private UserService userService;

	public void process( Event aEvent )
	{
		if ( ( aEvent instanceof TopologyEvent ) )
		{
			Resource eventResource = ( ( TopologyEvent ) aEvent ).getResource();

			if ( ( aEvent instanceof ResourceRemovedEvent ) )
			{
				userService.removeTerritoryResourceFromMembers( eventResource.getId() );

				if ( ( eventResource instanceof LinkResource ) )
				{
					userService.verifyMembersPersonalResources();
				}
			}
			else if ( ( aEvent instanceof ResourceAssociationChangedEvent ) )
			{
				if ( ( eventResource instanceof LinkResource ) )
				{
					userService.verifyMembersPersonalResources();
				}

				if ( ( eventResource instanceof Group ) )
				{
					userService.replaceChildResource( eventResource.getParentResourceId(), eventResource.getId() );
				}
			}
		}
		else if ( ( aEvent instanceof UserLogoffEvent ) )
		{
			String memberName = ( ( UserLogoffEvent ) aEvent ).getUserName();
			userService.verifyMemberPersonalResources( memberName );
		}
		else if ( ( aEvent instanceof AppEvent ) )
		{
			AppEvent appEvent = ( AppEvent ) aEvent;
			if ( appEvent.getAppEventType().equals( AppEventType.INSTALLED ) )
			{
				userService.processAppInstalled( appEvent.getAppID() );
			}
			else if ( appEvent.getAppEventType().equals( AppEventType.UNINSTALLED ) )
			{
				userService.processAppUninstalled( appEvent.getAppID() );
			}
			else if ( appEvent.getAppEventType().equals( AppEventType.UPGRADED ) )
			{
				userService.processAppUpgraded( appEvent.getAppID() );
			}
			else if ( appEvent.getAppEventType().equals( AppEventType.STATE ) )
			{
				AppStateEvent appStateEvent = ( AppStateEvent ) appEvent;
				if ( appStateEvent.getStatus() == AppStatus.RUNNING )
				{
					userService.processAppStarted( appEvent.getAppID() );
				}
			}
		}
	}

	public String getListenerName()
	{
		return UserEventHandler.class.getSimpleName();
	}

	public void setUserService( UserService userService )
	{
		this.userService = userService;
	}
}

