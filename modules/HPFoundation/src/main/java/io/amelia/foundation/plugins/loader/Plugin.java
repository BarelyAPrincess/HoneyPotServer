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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.Nonnull;

import io.amelia.foundation.ConfigMap;
import io.amelia.foundation.ConfigRegistry;
import io.amelia.foundation.VendorMeta;
import io.amelia.foundation.plugins.PluginBase;
import io.amelia.foundation.plugins.PluginMeta;
import io.amelia.lang.PluginException;
import io.amelia.logcompat.LogBuilder;
import io.amelia.logcompat.Logger;
import io.amelia.support.IO;
import io.amelia.support.Objs;
import io.amelia.support.Strs;
import io.amelia.data.parcel.Parcel;
import io.amelia.data.parcel.ParcelLoader;

public abstract class Plugin extends PluginBase
{
	/**
	 * This method provides fast access to the plugin that has {@link #getProvidingPlugin(Class) provided} the given plugin class, which is usually the plugin that implemented it.
	 * <p>
	 * An exception to this would be if plugin's jar that contained the class does not extend the class, where the intended plugin would have resided in a different jar / classloader.
	 *
	 * @param clazz the class desired
	 *
	 * @return the plugin that provides and implements said class
	 *
	 * @throws IllegalArgumentException if clazz is null
	 * @throws IllegalArgumentException if clazz does not extend {@link Plugin}
	 * @throws IllegalStateException    if clazz was not provided by a plugin, for example, if called with <code>Plugin.getPlugin(Plugin.class)</code>
	 * @throws IllegalStateException    if called from the static initializer for given Plugin
	 * @throws ClassCastException       if plugin that provided the class does not extend the class
	 */
	public static <T extends Plugin> T getPlugin( Class<T> clazz )
	{
		Objs.notNull( clazz, "Null class cannot have a plugin" );
		if ( !Plugin.class.isAssignableFrom( clazz ) )
			throw new IllegalArgumentException( clazz + " does not extend " + Plugin.class );
		final ClassLoader cl = clazz.getClassLoader();
		if ( !( cl instanceof PluginClassLoader ) )
			throw new IllegalArgumentException( clazz + " is not initialized by " + PluginClassLoader.class );
		Plugin plugin = ( ( PluginClassLoader ) cl ).plugin;
		if ( plugin == null )
			throw new IllegalStateException( "Cannot get plugin for " + clazz + " from a static initializer" );
		return clazz.cast( plugin );
	}

	/**
	 * This method provides fast access to the plugin that has provided the given class.
	 *
	 * @throws IllegalArgumentException if the class is not provided by a Plugin
	 * @throws IllegalArgumentException if class is null
	 * @throws IllegalStateException    if called from the static initializer for given Plugin
	 */
	public static Plugin getProvidingPlugin( Class<?> clazz )
	{
		Objs.notNull( clazz, "Null class cannot have a plugin" );
		final ClassLoader cl = clazz.getClassLoader();
		if ( !( cl instanceof PluginClassLoader ) )
			throw new IllegalArgumentException( clazz + " is not provided by " + PluginClassLoader.class );
		Plugin plugin = ( ( PluginClassLoader ) cl ).plugin;
		if ( plugin == null )
			throw new IllegalStateException( "Cannot get plugin for " + clazz + " from a static initializer" );
		return plugin;
	}

	private ClassLoader classLoader = null;
	private Path configPath = null;
	private Path dataPath = null;
	private boolean isEnabled = false;
	private PluginLoader loader = null;
	private PluginMeta meta = null;
	private boolean naggable = true;
	private Parcel newConfig = null;
	private Path pluginPath = null;

	/*
	 * protected Plugin( final PluginLoader loader, final PluginDescriptionFile description, final File dataPath, final File pluginPath ) { final ClassLoader classLoader = this.getClass().getClassLoader(); if ( classLoader instanceof PluginClassLoader ) {
	 * throw new IllegalStateException( "Cannot use initialization constructor at runtime" ); } init( loader, description, dataPath, pluginPath, classLoader ); }
	 */

	public Plugin()
	{
		PluginClassLoader.initialize( this );
	}

	/**
	 * Returns the ClassLoader which holds this plugin
	 *
	 * @return ClassLoader holding this plugin
	 */
	protected final ClassLoader getClassLoader()
	{
		return classLoader;
	}

	@Override
	public Parcel getConfig()
	{
		if ( newConfig == null )
			try
			{
				reloadConfig();
			}
			catch ( IOException e )
			{
				// Ignore
			}
		return newConfig;
	}

	public ConfigMap getConfigNode()
	{
		return ConfigRegistry.getChildOrCreate( "plugins." + getSimpleName() );
	}

	@Override
	public Path getConfigPath()
	{
		return configPath;
	}

	/**
	 * Returns the folder that the plugin data's files are located in. The folder may not yet exist.
	 *
	 * @return The folder.
	 */
	@Override
	public Path getDataPath()
	{
		return dataPath;
	}

	/**
	 * Returns the file which contains this plugin
	 *
	 * @return File containing this plugin
	 */
	protected final Path getFile()
	{
		return pluginPath;
	}

	public final Logger getLogger()
	{
		return LogBuilder.get( getClass() );
	}

	/**
	 * Returns the plugin.yaml pluginPath containing the details for this plugin
	 *
	 * @return Contents of the plugin.yaml pluginPath
	 */
	@Override
	public final PluginMeta getMeta()
	{
		return meta;
	}

	/**
	 * Gets the associated PluginLoader responsible for this plugin
	 *
	 * @return PluginLoader that controls this plugin
	 */
	@Override
	public final PluginLoader getPluginLoader()
	{
		return loader;
	}

	public VendorMeta getPluginMeta()
	{
		return meta;
	}

	@Override
	public final InputStream getResource( @Nonnull String localName )
	{
		Objs.notEmpty( localName );

		try
		{
			URL url = getClassLoader().getResource( localName );

			if ( url == null )
				return null;

			URLConnection connection = url.openConnection();
			connection.setUseCaches( false );
			return connection.getInputStream();
		}
		catch ( IOException ex )
		{
			return null;
		}
	}

	public String getSimpleName()
	{
		return Strs.toCamelCase( getName() );
	}

	final void init( PluginLoader loader, PluginMeta meta, Path dataPath, Path pluginPath, ClassLoader classLoader )
	{
		this.loader = loader;
		this.pluginPath = this.pluginPath;
		this.meta = meta;
		this.dataPath = dataPath;
		this.classLoader = classLoader;
		this.configPath = Paths.get( "config.yml" ).resolve( dataPath );
	}

	/**
	 * Returns a value indicating whether or not this plugin is currently enabled
	 *
	 * @return true if this plugin is enabled, otherwise false
	 */
	@Override
	public final boolean isEnabled()
	{
		return isEnabled;
	}

	/**
	 * Sets the enabled state of this plugin
	 *
	 * @param enabled true if enabled, otherwise false
	 */
	protected final void setEnabled( final boolean enabled ) throws PluginException.Error
	{
		if ( isEnabled != enabled )
		{
			isEnabled = enabled;

			try
			{
				if ( enabled )
					onEnable();
				else
					onDisable();
			}
			catch ( Throwable e )
			{
				isEnabled = false;
				throw e;
			}
		}
	}

	@Override
	public final boolean isNaggable()
	{
		return naggable;
	}

	@Override
	public final void setNaggable( boolean canNag )
	{
		naggable = canNag;
	}

	public void publishConfig()
	{
		ConfigMap node = getConfigNode().destroyChildAndCreate( "conf" );



	}

	@Override
	public void reloadConfig() throws IOException
	{
		newConfig = ParcelLoader.decodeYaml( configPath );

		InputStream defConfigStream = getResource( "config.yaml" );

		if ( defConfigStream == null )
			defConfigStream = getResource( "config.yml" );

		if ( defConfigStream != null )
		{
			Parcel defConfig = ParcelLoader.decodeYaml( defConfigStream );
			// newConfig.setDefaults( defConfig );
		}
	}

	@Override
	public void saveConfig()
	{
		try
		{
			OutputStream out = Files.newOutputStream( configPath );
			out.write( Strs.decodeDefault( ParcelLoader.encodeYaml( getConfig() ) ) );
			IO.closeQuietly( out );
		}
		catch ( IOException ex )
		{
			getLogger().severe( "Could not save config to " + configPath, ex );
		}
	}

	@Override
	public void saveDefaultConfig()
	{
		if ( !Files.isRegularFile( configPath ) )
			try
			{
				saveResource( "config.yaml", false );
			}
			catch ( IllegalArgumentException e )
			{
				saveResource( "config.yml", false );
			}
	}

	@Override
	public void saveResource( String resourcePath, boolean replace )
	{
		if ( resourcePath == null || resourcePath.equals( "" ) )
			throw new IllegalArgumentException( "ResourcePath cannot be null or empty" );

		resourcePath = resourcePath.replace( '\\', '/' );
		InputStream in = getResource( resourcePath );
		if ( in == null )
			throw new IllegalArgumentException( "The embedded resource '" + resourcePath + "' cannot be found in " + pluginPath );

		Path outPath = Paths.get( resourcePath ).resolve( dataPath );

		try
		{
			IO.forceCreateDirectory( outPath.getParent() );

			if ( !Files.isRegularFile( outPath ) || replace )
			{
				IO.deleteIfExists( outPath );
				OutputStream out = Files.newOutputStream( outPath );
				byte[] buf = new byte[1024];
				int len;
				while ( ( len = in.read( buf ) ) > 0 )
					out.write( buf, 0, len );
				out.close();
				in.close();
			}
			else
				getLogger().warning( "Could not save " + resourcePath + " to " + IO.relPath( outPath ) + " because it already exists." );
		}
		catch ( IOException ex )
		{
			getLogger().severe( "Could not save " + resourcePath + " to " + IO.relPath( outPath ), ex );
		}
	}

	@Override
	public String toString()
	{
		return meta.getDisplayName();
	}
}
