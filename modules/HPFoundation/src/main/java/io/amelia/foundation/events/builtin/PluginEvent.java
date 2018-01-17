/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation.events.builtin;

import io.amelia.foundation.plugins.PluginBase;

/**
 * Used for plugin enable and disable events
 */
public abstract class PluginEvent extends ApplicationEvent
{
	private final PluginBase plugin;

	public PluginEvent( final PluginBase plugin )
	{
		this.plugin = plugin;
	}

	/**
	 * Gets the plugin involved in this event
	 *
	 * @return Plugin for this event
	 */
	public PluginBase getPlugin()
	{
		return plugin;
	}
}
