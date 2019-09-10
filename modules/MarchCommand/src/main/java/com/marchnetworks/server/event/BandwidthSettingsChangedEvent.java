package com.marchnetworks.server.event;

import com.marchnetworks.common.configuration.ConfigSettings;
import com.marchnetworks.common.event.Event;

public class BandwidthSettingsChangedEvent extends Event
{
	private ConfigSettings settings;

	public BandwidthSettingsChangedEvent( ConfigSettings settings )
	{
		super( BandwidthSettingsChangedEvent.class.getName() );
		this.settings = settings;
	}

	public ConfigSettings getSettings()
	{
		return settings;
	}
}

