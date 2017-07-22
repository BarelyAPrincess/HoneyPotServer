/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.events;

public interface EventRegistrar
{
	/**
	 * Returns a value indicating whether or not this plugin is currently enabled
	 *
	 * @return true if this plugin is enabled, otherwise false
	 */
	boolean isEnabled();

	/**
	 * Returns the name of the plugin.
	 * <p>
	 * This should return the bare name of the plugin and should be used for comparison.
	 *
	 * @return name of the plugin
	 */
	String getName();
}
