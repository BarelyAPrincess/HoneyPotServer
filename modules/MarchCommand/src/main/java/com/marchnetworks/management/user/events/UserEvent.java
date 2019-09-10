package com.marchnetworks.management.user.events;

import com.marchnetworks.command.api.event.Notifiable;
import com.marchnetworks.common.event.Event;

public abstract class UserEvent extends Event implements Notifiable
{
	public String userName;

	public UserEvent()
	{
	}

	public UserEvent( String type, String userName )
	{
		super( type );
		this.userName = userName;
	}

	public String getUserName()
	{
		return userName;
	}
}

