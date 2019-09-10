package com.marchnetworks.license.events;

import com.marchnetworks.command.api.event.Notifiable;

public abstract class LicenseExternalEvent extends LicenseEvent implements Notifiable
{
	public LicenseExternalEvent()
	{
	}

	public LicenseExternalEvent( String type )
	{
		super( type );
	}
}
