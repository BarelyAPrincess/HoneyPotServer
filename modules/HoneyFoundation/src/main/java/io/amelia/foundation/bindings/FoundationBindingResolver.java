/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation.bindings;

import io.amelia.foundation.ApplicationInterface;
import io.amelia.foundation.Foundation;
import io.amelia.foundation.bindings.BindingResolver;
import io.amelia.foundation.bindings.DynamicBinding;
import io.amelia.foundation.bindings.ProvidesBinding;
import io.amelia.foundation.plugins.Plugins;

public class FoundationBindingResolver extends BindingResolver
{
	Plugins pluginServiceManager = null;

	public FoundationBindingResolver()
	{
		addAlias( Plugins.class, "plugins.manager" );
		addAlias( "plugins.mgr", "plugins.manager" );
	}

	@ProvidesBinding( "plugins.manager" )
	public Plugins pluginManager()
	{
		if ( pluginServiceManager == null )
			pluginServiceManager = new Plugins();
		return pluginServiceManager;
	}

	@DynamicBinding
	public ApplicationInterface router()
	{
		return Foundation.getApplication();
	}
}
