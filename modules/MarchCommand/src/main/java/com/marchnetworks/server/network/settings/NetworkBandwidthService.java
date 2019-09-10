package com.marchnetworks.server.network.settings;

import com.marchnetworks.common.configuration.ConfigSettings;

public abstract interface NetworkBandwidthService
{
	public abstract ConfigSettings getSettings();

	public abstract void updateSettings( ConfigSettings paramConfigSettings );

	public abstract void register( String paramString );

	public abstract void unregister( String paramString );

	public abstract void getPermit( String paramString, int paramInt );
}

