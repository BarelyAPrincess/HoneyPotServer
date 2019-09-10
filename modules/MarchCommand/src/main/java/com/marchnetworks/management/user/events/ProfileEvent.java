package com.marchnetworks.management.user.events;

import com.marchnetworks.command.api.event.Notifiable;
import com.marchnetworks.common.event.Event;

public abstract class ProfileEvent extends Event implements Notifiable
{
	public String profileId;

	public ProfileEvent()
	{
	}

	public ProfileEvent( String type )
	{
		super( type );
	}

	public String getProfileId()
	{
		return profileId;
	}

	public void setProfileID( String profileId )
	{
		this.profileId = profileId;
	}
}

