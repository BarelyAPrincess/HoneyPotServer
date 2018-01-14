package io.amelia.foundation.binding;

import io.amelia.foundation.ApplicationInterface;
import io.amelia.foundation.Foundation;
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
