package io.amelia.networking;

import io.amelia.config.ConfigNode;
import io.amelia.lang.NetworkException;

public interface NetworkWorker
{
	void start( ConfigNode config ) throws NetworkException;

	void stop() throws NetworkException;

	String getId();

	boolean isStarted();

	void heartbeat();
}
