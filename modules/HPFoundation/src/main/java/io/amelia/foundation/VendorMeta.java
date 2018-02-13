/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import io.amelia.support.Maps;
import io.amelia.support.data.ParcelLoader;

public class VendorMeta extends MetaMap
{
	public static final String NAME = "name";
	public static final String AUTHORS = "authors";
	public static final String DESCRIPTION = "description";
	public static final String GITHUB_BASE_URL = "gitHubBaseUrl";
	public static final String PREFIX = "prefix";
	public static final String VERSION = "version";
	public static final String WEBSITE = "website";

	public VendorMeta()
	{

	}

	public VendorMeta( final Map<String, String> data )
	{
		putAll( data );
	}

	public VendorMeta( final File file ) throws IOException
	{
		Maps.builder( ParcelLoader.decodeToMap( file, ParcelLoader.Type.AUTO_DETECT ) ).to( this );
	}

	public VendorMeta( final InputStream inputStream, ParcelLoader.Type type ) throws IOException
	{
		Maps.builder( ParcelLoader.decodeToMap( inputStream, ParcelLoader.Type.AUTO_DETECT ) ).to( this );
	}

	public VendorMeta( final ConfigMap config )
	{
		Maps.builder( config.values() ).to( this );
	}

	/**
	 * Gives the list of authors for the plugin.
	 * <ul>
	 * <li>Gives credit to the developer.
	 * <li>Used in some server error messages to provide helpful feedback on who to contact when an error occurs.
	 * <li>A email address is recommended.
	 * <li>Is displayed when a user types <code>/version PluginName</code>
	 * <li><code>authors</code> must be in <a href="http://en.wikipedia.org/wiki/YAML#Lists">YAML list format</a>.
	 * </ul>
	 * <p>
	 * In the plugin.yaml, this has two entries, <code>author</code> and <code>authors</code>.
	 * <p>
	 * Single author example: <blockquote>
	 * <p>
	 * <pre>
	 * author: BobJones
	 * </pre>
	 * <p>
	 * </blockquote> Multiple author example: <blockquote>
	 * <p>
	 * <pre>
	 * authors: [ChioriGreene, elfinpen, Joey13]
	 * </pre>
	 * <p>
	 * </blockquote> When both are specified, author will be the first entry in the list, so this example: <blockquote>
	 * <p>
	 * <pre>
	 * author: Grum
	 * authors:
	 * - feildmaster
	 * - amaranth
	 * </pre>
	 * <p>
	 * </blockquote> Is equivilant to this example: <blockquote>
	 * <p>
	 * <pre>authors: [Grum, feildmaster, aramanth]
	 *
	 * <pre>
	 * </blockquote>
	 *
	 * @return an immutable list of the plugin's authors
	 */
	public List<String> getAuthors()
	{
		return getList( AUTHORS, "|" );
	}

	/**
	 * Gives a human-friendly description of the functionality the plugin provides.
	 * <ul>
	 * <li>The description can have multiple lines.
	 * <li>Displayed when a user types <code>/version PluginName</code>
	 * </ul>
	 * <p>
	 * In the plugin.yaml, this entry is named <code>description</code>.
	 * <p>
	 * Example: <blockquote>
	 * <p>
	 * <pre>
	 * description: This plugin is so 31337. You can set yourself on fire.
	 * </pre>
	 * <p>
	 * </blockquote>
	 *
	 * @return description of this plugin, or null if not specified
	 */
	public String getDescription()
	{
		return getString( DESCRIPTION );
	}

	/**
	 * Returns the name of a plugin, including the version. This method is provided for convenience; it uses the {@link #getName()} and {@link #getVersion()} entries.
	 *
	 * @return a descriptive name of the plugin and respective version
	 */
	public String getDisplayName()
	{
		return getName() + " v" + getVersion();
	}

	/**
	 * Gets the URL base for the plugin source code.
	 * e.g., https://raw.githubusercontent.com/[username]/[repository]/[branch]/src/main/java/
	 *
	 * @return The github raw file base URL
	 */
	public String getGitHubBaseUrl()
	{
		return getString( GITHUB_BASE_URL );
	}

	/**
	 * Gives the token to prefix plugin-specific logging messages with.
	 * <ul>
	 * <li>This includes all messages using Plugin#getLogger()
	 * <li>If not specified, the server uses the plugin's {@link #getName() name}.
	 * <li>This should clearly indicate what plugin is being logged.
	 * </ul>
	 * <p>
	 * In the plugin.yaml, this entry is named <code>prefix</code>.
	 * <p>
	 * Example:<blockquote>
	 * <p>
	 * <pre>
	 * prefix: ex-why-zee
	 * </pre>
	 * <p>
	 * </blockquote>
	 *
	 * @return the prefixed logging token, or null if not specified
	 */
	public String getLoggerPrefix()
	{
		return getString( PREFIX );
	}

	public String getName()
	{
		return getString( NAME );
	}

	public String getVersion()
	{
		return getString( VERSION );
	}

	/**
	 * Gives the version of the plugin.
	 * <ul>
	 * <li>Version is an arbitrary string, however the most common format is MajorRelease.MinorRelease.Build (eg: 1.4.1).
	 * <li>Typically you will increment this every time you release a new feature or bug fix.
	 * <li>Displayed when a user types <code>/version PluginName</code>
	 * </ul>
	 * <p>
	 * In the plugin.yaml, this entry is named <code>version</code>.
	 * <p>
	 * Example:<blockquote>
	 * <p>
	 * <pre>
	 * version: 1.4.1
	 * </pre>
	 * <p>
	 * </blockquote>
	 *
	 * @return the version of the plugin
	 */
	public String getVersionRequired()
	{
		return getRequired( VERSION, "Version is not defined" );
	}

	/**
	 * Gives the plugin's or plugin's author's website.
	 * <ul>
	 * <li>A link to the Curse page that includes documentation and downloads is highly recommended.
	 * <li>Displayed when a user types <code>/version PluginName</code>
	 * </ul>
	 * <p>
	 * In the plugin.yaml, this entry is named <code>website</code>.
	 * <p>
	 * Example: <blockquote>
	 * <p>
	 * <pre>
	 * website: http://www.curse.com/server-mods/minecraft/myplugin
	 * </pre>
	 * <p>
	 * </blockquote>
	 *
	 * @return description of this plugin, or null if not specified
	 */
	public String getWebsite()
	{
		return getString( WEBSITE );
	}
}
