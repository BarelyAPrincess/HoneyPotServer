package io.amelia.networking;

import io.amelia.foundation.ConfigMap;
import io.amelia.foundation.ConfigRegistry;
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

	T start() throws NetworkException.Error;

	T stop() throws NetworkException.Error;
}
