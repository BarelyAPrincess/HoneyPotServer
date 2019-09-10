package com.marchnetworks.license.events;

import com.marchnetworks.common.event.Event;

abstract class LicenseEvent extends Event
{
	public LicenseEvent()
	{
	}

	public LicenseEvent( String type )
	{
		super( type );
	}
}
