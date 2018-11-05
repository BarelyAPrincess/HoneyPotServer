/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.networking;

import io.amelia.foundation.ConfigData;
import io.amelia.foundation.ConfigRegistry;
import io.amelia.lang.NetworkException;

public interface NetworkWorker<T>
{
	default ConfigData getConfig()
	{
		return ConfigRegistry.getChildOrCreate( "config.network." + getId() );
	}

	default ConfigData getConfig( String key )
	{
		return getConfig().getChild( key );
	}

	String getId();

	void heartbeat();

	boolean isStarted();

	T start() throws NetworkException.Error;

	T stop() throws NetworkException.Error;
}
