/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation.plugins;

import io.amelia.config.ConfigLoader;
import io.amelia.config.ConfigNode;
import io.amelia.foundation.MetaMap;
import io.amelia.foundation.VendorMeta;
import io.amelia.foundation.injection.MavenReference;
import io.amelia.lang.PluginMetaException;
import io.amelia.lang.RunLevel;
import io.amelia.support.Lists;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This type is the runtime-container for the information in the plugin.yaml. All plugins must have a respective
 * plugin.yaml. For plugins written in java using the standard plugin loader, this file must be in the root of the jar
 * file.
 * <p>
 * When the server loads a plugin, it needs to know some basic information about it. It reads this information from a YAML file, 'plugin.yaml'. This file consists of a set of attributes, each defined on a new line and with no indentation.
 * <p>
 * Every (almost* every) method corresponds with a specific entry in the plugin.yaml. These are the <b>required</b> entries for every plugin.yaml:
 * <ul>
 * <li>{@link #getName()} - <code>name</code>
 * <li>{@link #getVersion()} - <code>version</code>
 * <li>{@link #getMain()} - <code>main</code>
 * </ul>
 * <p>
 * Failing to include any of these items will throw an exception and cause the server to ignore your plugin.
 * <p>
 * This is a list of the possible yaml keys, with specific details included in the respective method documentations:
 * <getTable border=1>
 * <tr>
 * <th>Node</th>
 * <th>Method</th>
 * <th>Summary</th>
 * </tr>
 * <tr>
 * <td><code>name</code></td>
 * <td>{@link #getName()}</td>
 * <td>The unique name of plugin</td>
 * </tr>
 * <tr>
 * <td><code>version</code></td>
 * <td>{@link #getVersion()}</td>
 * <td>A plugin revision identifier</td>
 * </tr>
 * <tr>
 * <td><code>main</code></td>
 * <td>{@link #getMain()}</td>
 * <td>The plugin's initial class file</td>
 * </tr>
 * <tr>
 * <td><code>author</code><br>
 * <code>authors</code></td>
 * <td>{@link #getAuthors()}</td>
 * <td>The plugin contributors</td>
 * </tr>
 * <tr>
 * <td><code>description</code></td>
 * <td>{@link #getDescription()}</td>
 * <td>Human readable plugin summary</td>
 * </tr>
 * <tr>
 * <td><code>website</code></td>
 * <td>{@link #getWebsite()}</td>
 * <td>The URL to the plugin's site</td>
 * </tr>
 * <tr>
 * <td><code>prefix</code></td>
 * <td>{@link #getLoggerPrefix()}</td>
 * <td>The token to prefix plugin log entries</td>
 * </tr>
 * <tr>
 * <td><code>load</code></td>
 * <td>{@link #getLoad()}</td>
 * <td>The phase of server-startup this plugin will load during</td>
 * </tr>
 * <tr>
 * <td><code>depend</code></td>
 * <td>{@link #getDepend()}</td>
 * <td>Other required plugins</td>
 * </tr>
 * <tr>
 * <td><code>libraries</code></td>
 * <td>{@link #getLibraries()}</td>
 * <td>Required java libraries</td>
 * </tr>
 * <tr>
 * <td><code>softdepend</code></td>
 * <td>{@link #getSoftDepend()}</td>
 * <td>Other plugins that add functionality</td>
 * </tr>
 * <tr>
 * <td><code>loadbefore</code></td>
 * <td>{@link #getLoadBefore()}</td>
 * <td>The inverse softdepend</td>
 * </tr>
 * </getTable>
 * <p>
 * A plugin.yaml example:<blockquote>
 * <p>
 * <pre>
 * name: SuperAwesomePlugin
 * version: 1.0.4
 * description: This plugin does something really awesome to the server
 * author: SomeAuthor
 * authors: [SomeAuthor, God, Jesus]
 * website: http://www.superawesomeplugin.com
 *
 * main: com.superawesomeplugin.plugin.Main
 * depend: [EmailPlugin]
 *
 * commands:
 *   doit:
 *     description: Does that super awesome thing
 *     aliases: [doit2, ihateyou]
 *     permission: com.chiorichan.destruction
 *     usage: Type /&lt;doit&gt; to do that super awesome thing.
 * </pre>
 * <p>
 * </blockquote>
 * <p>
 * XXX Rewrite the description file read process to make it easier to implement
 */
public class PluginMeta extends VendorMeta
{
	public PluginMeta( final File file ) throws IOException
	{
		super( file );
	}

	public PluginMeta( final InputStream stream, ConfigLoader.StreamType streamType ) throws IOException
	{
		super( stream, streamType );
	}

	public PluginMeta( final ConfigNode config )
	{
		super( config );
	}

	/**
	 * Gives a list of other plugins that the plugin requires.
	 * <ul>
	 * <li>Use the value in the {@link #getName()} of the target plugin to specify the dependency.
	 * <li>If any plugin listed here is not found, your plugin will fail to load at startup.
	 * <li>If multiple plugins list each other in <code>depend</code>, creating a network with no individual plugin does not list another plugin in the <a href=https://en.wikipedia.org/wiki/Circular_dependency>network</a>, all plugins in that network will
	 * fail.
	 * <li><code>depend</code> must be in must be in <a href="http://en.wikipedia.org/wiki/YAML#Lists">YAML list format</a>.
	 * </ul>
	 * <p>
	 * In the plugin.yaml, this entry is named <code>depend</code>.
	 * <p>
	 * Example: <blockquote>
	 * <p>
	 * <pre>
	 * depend:
	 * - OnePlugin
	 * - AnotherPlugin
	 * </pre>
	 * <p>
	 * </blockquote>
	 *
	 * @return immutable list of the plugin's dependencies
	 */
	public List<String> getDepend()
	{
		return getList( "depend", "|" );
	}

	/**
	 * Gives a list of java libraries required by this plugin.
	 * <ul>
	 * <li>Use the maven group:name:version string to specify the library.
	 * <li>If any libraries listed here are not found or can't be parsed, your plugin will fail to load at startup.
	 * <li><code>libraries</code> must be in <a href="http://en.wikipedia.org/wiki/YAML#Lists">YAML list format</a>.
	 * <li>For the time being, libraries are downloaded from the Central Maven Repository. We have plans to implement the ability to specify repositories, maybe even upload your libraries and plugins to our own central download server.
	 * </ul>
	 * <p>
	 * In the plugin.yaml, this entry is named <code>libraries</code>.
	 * <p>
	 * Example: <blockquote>
	 * <p>
	 * <pre>
	 * libraries:
	 * - com.dropbox.core:dropbox-core-sdk:1.7.7
	 * - org.apache.commons:commons-lang3:3.3.2
	 * </pre>
	 * <p>
	 * </blockquote>
	 *
	 * @return immutable list of the plugin's dependencies
	 */
	public List<MavenReference> getLibraries()
	{
		return getList( "libraries", "|", e ->
		{
			try
			{
				return new MavenReference( getName(), e );
			}
			catch ( IllegalArgumentException ee )
			{
				PluginManager.L.severe( "Could not parse the library '" + e + "' for plugin '" + getName() + "', expected pattern 'group:name:version'. Unless fixed, it will be ignored.", ee );
			}
			return null;
		} );
	}

	/**
	 * Gives the phase of server startup that the plugin should be loaded.
	 * <ul>
	 * <li>Possible values are in {@link RunLevel}.
	 * <li>Defaults to {@link RunLevel#INITIALIZED}.
	 * <li>Certain caveats apply to each phase.
	 * <li>When different, {@link #getDepend()}, {@link #getSoftDepend()}, and {@link #getLoadBefore()}
	 * become relative in order loaded per-phase. If a plugin loads at <code>STARTUP</code>, but a dependency
	 * loads at <code>RUNNING</code>, the dependency will not be loaded before the plugin is loaded.
	 * </ul>
	 * <p>
	 * In the plugin.yaml, this entry is named <code>runlevel</code>.
	 * <p>
	 * Example:<blockquote>
	 * <p>
	 * <pre>
	 * load: STARTUP
	 * </pre>
	 * <p>
	 * </blockquote>
	 *
	 * @return the phase when the plugin should be loaded
	 */
	public RunLevel getLoad()
	{
		try
		{
			return RunLevel.valueOf( getString( "runlevel" ) );
		}
		catch ( IllegalArgumentException | NullPointerException e )
		{
			return RunLevel.STARTUP;
		}
	}

	/**
	 * Gets the list of plugins that should consider this plugin a soft-dependency.
	 * <ul>
	 * <li>Use the value in the {@link #getName()} of the target plugin to specify the dependency.
	 * <li>The plugin should load before any other plugins listed here.
	 * <li>Specifying another plugin here is strictly equivalent to having the specified plugin's {@link #getSoftDepend()} include {@link #getName() this plugin}.
	 * <li><code>loadbefore</code> must be in <a href="http://en.wikipedia.org/wiki/YAML#Lists">YAML list format</a>.
	 * </ul>
	 * <p>
	 * In the plugin.yaml, this entry is named <code>loadbefore</code>.
	 * <p>
	 * Example: <blockquote>
	 * <p>
	 * <pre>
	 * loadbefore:
	 * - OnePlugin
	 * - AnotherPlugin
	 * </pre>
	 * <p>
	 * </blockquote>
	 *
	 * @return immutable list of plugins that should consider this plugin a soft-dependency
	 */
	public List<String> getLoadBefore()
	{
		return getList( "loadbefore" );
	}

	public String getMain()
	{
		return getString( "main" );
	}

	/**
	 * Gives the fully qualified name of the main class for a plugin. The format should follow the {@link ClassLoader#loadClass(String)} syntax to successfully be resolved at runtime. For most plugins, this is the
	 * class that extends
	 * <ul>
	 * <li>This must contain the full namespace including the class file itself.
	 * <li>If your namespace is <code>com.chiorichan.plugin</code>, and your class file is called <code>MyPlugin</code> then this must be <code>com.chiorichan.plugin.MyPlugin</code>
	 * <li>No plugin can use <code>org.bukkit.</code> as a base package for <b>any class</b>, including the main class.
	 * </ul>
	 * <p>
	 * In the plugin.yaml, this entry is named <code>main</code>.
	 * <p>
	 * Example: <blockquote>
	 * <p>
	 * <pre>
	 * main: org.bukkit.plugin.MyPlugin
	 * </pre>
	 * <p>
	 * </blockquote>
	 *
	 * @return the fully qualified main class for the plugin
	 */
	public String getMainRequired() throws PluginMetaException
	{
		return getRequired( "main", "Main is not defined" );
	}

	/**
	 * Gives the name of the plugin. This name is a unique identifier for plugins.
	 * <ul>
	 * <li>Must consist of all alphanumeric characters, underscores, hyphon, and period (a-z,A-Z,0-9, _.-). Any other character will cause the plugin.yaml to fail loading.
	 * <li>Used to determine the name of the plugin's data folder. Data folders are placed in the ./plugins/ directory by default, but this behavior should not be relied on.
	 * <li>It is good practice to name your jar the same as this, for example 'MyPlugin.jar'.
	 * <li>Case sensitive.
	 * <li>The is the token referenced in {@link #getDepend()}, {@link #getSoftDepend()}, and {@link #getLoadBefore()}.
	 * </ul>
	 * <p>
	 * In the plugin.yaml, this entry is named <code>name</code>.
	 * <p>
	 * Example:<blockquote>
	 * <p>
	 * <pre>
	 * name: MyPlugin
	 * </pre>
	 * <p>
	 * </blockquote>
	 *
	 * @return the name of the plugin
	 */
	public String getNameWithException() throws PluginMetaException
	{
		String name = getName();

		if ( name == null )
			throw new PluginMetaException( "Plugin name is not defined." );

		if ( !name.matches( "^[A-Za-z0-9 _.-]+$" ) )
			throw new PluginMetaException( "Plugin name '" + name + "' contains invalid characters." );

		return name;
	}

	public Map<String, List<String>> getNatives()
	{
		Map<String, List<String>> natives = new HashMap<>();
		MetaMap map = getMap( "natives" );

		for ( Entry<String, Object> entry : map.entrySet() )
			natives.put( entry.getKey(), Optional.ofNullable( entry.getValue() ).map( v -> ( List<String> ) ( v instanceof List ? v : v instanceof String ? Lists.newArrayList( v ) : null ) ).orElse( new ArrayList<>() ) );

		return natives;
	}

	/**
	 * Gives a list of other plugins that the plugin requires for full functionality. The {@link PluginManager} will make
	 * best effort to treat all entries here as if they were a {@link #getDepend() dependency}, but will never fail
	 * because of one of these entries.
	 * <ul>
	 * <li>Use the value in the {@link #getName()} of the target plugin to specify the dependency.
	 * <li>When an unresolvable plugin is listed, it will be ignored and does not affect load order.
	 * <li>When a circular dependency occurs (a network of plugins depending or soft-dependending each other), it will arbitrarily choose a plugin that can be resolved when ignoring soft-dependencies.
	 * <li><code>softdepend</code> must be in <a href="http://en.wikipedia.org/wiki/YAML#Lists">YAML list format</a>.
	 * </ul>
	 * <p>
	 * In the plugin.yaml, this entry is named <code>softdepend</code>.
	 * <p>
	 * Example: <blockquote>
	 * <p>
	 * <pre>
	 * softdepend: [OnePlugin, AnotherPlugin]
	 * </pre>
	 * <p>
	 * </blockquote>
	 *
	 * @return immutable list of the plugin's preferred dependencies
	 */
	public List<String> getSoftDepend()
	{
		return getList( "softdepend", "|" );
	}
}
