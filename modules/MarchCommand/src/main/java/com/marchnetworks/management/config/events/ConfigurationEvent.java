package com.marchnetworks.management.config.events;

import com.marchnetworks.command.api.event.Notifiable;
import com.marchnetworks.common.event.Event;

abstract class ConfigurationEvent extends Event implements Notifiable
{
	protected String configurationId;

	public ConfigurationEvent()
	{
	}

	public ConfigurationEvent( String type )
	{
		super( type );
	}

	public String getConfigurationId()
	{
		return configurationId;
	}

	public void setConfigurationId( String configurationId )
	{
		this.configurationId = configurationId;
	}

	public boolean isTerritorySensitive()
	{
		return false;
	}
}
