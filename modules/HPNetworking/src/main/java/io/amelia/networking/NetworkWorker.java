/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <theameliadewitt@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
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
