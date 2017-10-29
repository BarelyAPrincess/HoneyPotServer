/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation.plugins.loader;

import io.amelia.config.ConfigMap;
import io.amelia.config.ConfigRegistry;
import io.amelia.foundation.VendorMeta;
import io.amelia.foundation.plugins.PluginBase;
import io.amelia.foundation.plugins.PluginManager;
import io.amelia.foundation.plugins.PluginMeta;
import io.amelia.lang.PluginException;
import io.amelia.logcompat.Logger;
import io.amelia.support.Objs;
import io.amelia.support.Strs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public abstract class Plugin extends PluginBase
{
	/**
	 * This method provides fast access to the plugin that has {@link #getProvidingPlugin(Class) provided} the given plugin class, which is usually the plugin that implemented it.
	 * <p>
	 * An exception to this would be if plugin's jar that contained the class does not extend the class, where the intended plugin would have resided in a different jar / classloader.
	 *
	 * @param clazz the class desired
	 * @return the plugin that provides and implements said class
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
	private File configFile = null;
	private File dataFolder = null;
	private File file = null;
	private boolean isEnabled = false;
	private PluginLoader loader = null;
	private PluginMeta meta = null;
	private boolean naggable = true;
	private ConfigMap newConfig = null;

	/*
	 * protected Plugin( final PluginLoader loader, final PluginDescriptionFile description, final File dataFolder, final File file ) { final ClassLoader classLoader = this.getClass().getClassLoader(); if ( classLoader instanceof PluginClassLoader ) {
	 * throw new IllegalStateException( "Cannot use initialization constructor at runtime" ); } init( loader, description, dataFolder, file, classLoader ); }
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
	public ConfigMap getConfig()
	{
		if ( newConfig == null )
			reloadConfig();
		return newConfig;
	}

	@Override
	public File getConfigFile()
	{
		return configFile;
	}

	/**
	 * Returns the folder that the plugin data's files are located in. The folder may not yet exist.
	 *
	 * @return The folder.
	 */
	@Override
	public File getDataFolder()
	{
		return dataFolder;
	}

	/**
	 * Returns the plugin.yaml file containing the details for this plugin
	 *
	 * @return Contents of the plugin.yaml file
	 */
	@Override
	public final PluginMeta getDescription()
	{
		return meta;
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
	 * Gets the associated PluginLoader responsible for this plugin
	 *
	 * @return PluginLoader that controls this plugin
	 */
	@Override
	public final PluginLoader getPluginLoader()
	{
		return loader;
	}

	@Override
	public final InputStream getResource( String filename )
	{
		if ( filename == null )
			throw new IllegalArgumentException( "Filename cannot be null" );

		try
		{
			URL url = getClassLoader().getResource( filename );

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

	@Override
	public void reloadConfig()
	{
		newConfig = ConfigMap.loadConfiguration( configFile );

		InputStream defConfigStream = getResource( "config.yaml" );

		if ( defConfigStream == null )
			defConfigStream = getResource( "config.yml" );

		if ( defConfigStream != null )
		{
			ConfigMap defConfig = ConfigMap.loadConfiguration( defConfigStream );

			newConfig.setDefaults( defConfig );
		}
	}

	@Override
	public void saveConfig()
	{
		try
		{
			getConfig().save( configFile );
		}
		catch ( IOException ex )
		{
			getLogger().severe( "Could not save config to " + configFile, ex );
		}
	}

	@Override
	public void saveDefaultConfig()
	{
		if ( !configFile.exists() )
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
			throw new IllegalArgumentException( "The embedded resource '" + resourcePath + "' cannot be found in " + file );

		File outFile = new File( dataFolder, resourcePath );
		int lastIndex = resourcePath.lastIndexOf( '/' );
		File outDir = new File( dataFolder, resourcePath.substring( 0, lastIndex >= 0 ? lastIndex : 0 ) );

		if ( !outDir.exists() )
			outDir.mkdirs();

		try
		{
			if ( !outFile.exists() || replace )
			{
				OutputStream out = new FileOutputStream( outFile );
				byte[] buf = new byte[1024];
				int len;
				while ( ( len = in.read( buf ) ) > 0 )
					out.write( buf, 0, len );
				out.close();
				in.close();
			}
			else
				getLogger().warning( "Could not save " + outFile.getName() + " to " + outFile + " because " + outFile.getName() + " already exists." );
		}
		catch ( IOException ex )
		{
			getLogger().severe( "Could not save " + outFile.getName() + " to " + outFile, ex );
		}
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

	public ConfigMap getConfigNode()
	{
		return ConfigRegistry.getChildOrCreate( "plugins." + getSimpleName() );
	}

	/**
	 * Returns the file which contains this plugin
	 *
	 * @return File containing this plugin
	 */
	protected final File getFile()
	{
		return file;
	}

	public final Logger getLogger()
	{
		return PluginManager.L.getLogger( this );
	}

	public VendorMeta getPluginMeta()
	{
		return meta;
	}

	public String getSimpleName()
	{
		return Strs.toCamelCase( getName() );
	}

	final void init( PluginLoader loader, PluginMeta meta, File dataFolder, File file, ClassLoader classLoader )
	{
		this.loader = loader;
		this.file = file;
		this.meta = meta;
		this.dataFolder = dataFolder;
		this.classLoader = classLoader;
		this.configFile = new File( dataFolder, "config.yaml" );
	}

	public void publishConfig()
	{
		ConfigMap node = getConfigNode().destroyChildThenCreate( "conf" );




	}

	@Override
	public String toString()
	{
		return meta.getDisplayName();
	}
}
