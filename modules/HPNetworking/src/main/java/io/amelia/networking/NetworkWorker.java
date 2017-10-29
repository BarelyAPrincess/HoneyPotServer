package io.amelia.networking;

import io.amelia.config.ConfigMap;
import io.amelia.config.ConfigRegistry;
import io.amelia.lang.NetworkException;

public interface NetworkWorker<T>
{
	default ConfigMap getConfig()
	{
		return ConfigRegistry.getChildOrCreate( "config.network." + getId() );
	}

	default ConfigMap getConfig( String key )
	{
		return getConfig().getChild( key );
	}

	String getId();

	void heartbeat();

	boolean isStarted();

	T start() throws NetworkException;

	T stop() throws NetworkException;
}
