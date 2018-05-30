/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <theameliadewitt@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation.plugins.loader;

import java.nio.file.Path;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import io.amelia.foundation.plugins.PluginMeta;
import io.amelia.lang.PluginDependencyUnknownException;
import io.amelia.lang.PluginInvalidException;
import io.amelia.lang.PluginMetaException;

/**
 * Represents a plugin loader, which handles direct access to specific types of plugins
 */
public interface PluginLoader
{
	/**
	 * Disables the specified plugin
	 * <p>
	 * Attempting to disable a plugin that is not enabled will have no effect
	 *
	 * @param plugin Plugin to disable
	 */
	void disablePlugin( Plugin plugin );

	/**
	 * Enables the specified plugin
	 * <p>
	 * Attempting to enable a plugin that is already enabled will have no effect
	 *
	 * @param plugin Plugin to enable
	 */
	void enablePlugin( Plugin plugin );

	/**
	 * Returns a list of all filename filters expected by this PluginLoader
	 *
	 * @return The filters
	 */
	Pattern[] getPluginFileFilters();

	/**
	 * Loads a PluginDescriptionFile from the specified file
	 *
	 * @param file File to attempt to load from
	 *
	 * @return A new PluginDescriptionFile loaded from the plugin.yml in the
	 * specified file
	 *
	 * @throws PluginMetaException If the plugin description file
	 *                             could not be created
	 */
	PluginMeta getPluginMeta( Path file ) throws PluginMetaException;

	/**
	 * Loads the plugin contained in the specified file
	 *
	 * @param file File to attempt to load
	 *
	 * @return Plugin that was contained in the specified file, or null if
	 * unsuccessful
	 *
	 * @throws PluginInvalidException           Thrown when the specified file is not a
	 *                                          plugin
	 * @throws PluginDependencyUnknownException If a required dependency could not
	 *                                          be found
	 */
	Plugin loadPlugin( @Nonnull Path file ) throws PluginInvalidException, PluginDependencyUnknownException;
}
