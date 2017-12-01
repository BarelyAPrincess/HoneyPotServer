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

import com.chiorichan.plugin.PluginInformation;
import io.amelia.lang.PluginInvalidException;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * A ClassLoader for plugins, to allow shared classes across multiple plugins
 *
 * @author Chiori Greene, a.k.a. Chiori-chan {@literal <me@chiorichan.com>}
 */
public final class PluginClassLoader extends URLClassLoader
{
	private static final Map<Class<?>, PluginClassLoader> loaders = new WeakHashMap<Class<?>, PluginClassLoader>();
	
	private final JavaPluginLoader loader;
	private final Map<String, Class<?>> classes = new HashMap<String, Class<?>>();
	private final PluginInformation description;
	private final File dataFolder;
	private final File file;
	final Plugin plugin;
	private boolean initialized = false;
	
	PluginClassLoader( final JavaPluginLoader loader, final ClassLoader parent, final PluginInformation description, final File dataFolder, final File file ) throws PluginInvalidException, MalformedURLException
	{
		super( new URL[] {file.toURI().toURL()}, parent );
		
		Validate.notNull( loader, "Loader cannot be null" );
		
		this.loader = loader;
		this.description = description;
		this.dataFolder = dataFolder;
		this.file = file;
		
		try
		{
			
			
			Class<?> jarClass;
			try
			{
				jarClass = Class.forName( description.getMain(), true, this );
			}
			catch ( ClassNotFoundException ex )
			{
				throw new PluginInvalidException( "Cannot find main class `" + description.getMain() + "'", ex );
			}
			
			Class<? extends Plugin> pluginClass;
			try
			{
				pluginClass = jarClass.asSubclass( Plugin.class );
			}
			catch ( ClassCastException ex )
			{
				throw new PluginInvalidException( "main class `" + description.getMain() + "' does not extend Plugin", ex );
			}
			
			loaders.put( jarClass, this );
			
			plugin = pluginClass.newInstance();
		}
		catch ( IllegalAccessException ex )
		{
			throw new PluginInvalidException( "No public constructor", ex );
		}
		catch ( InstantiationException ex )
		{
			throw new PluginInvalidException( "Abnormal plugin type", ex );
		}
	}
	
	static synchronized void initialize( Plugin javaPlugin )
	{
		Validate.notNull( javaPlugin, "Initializing plugin cannot be null" );
		
		PluginClassLoader loader = loaders.get( javaPlugin.getClass() );
		
		if ( loader == null )
			throw new IllegalStateException( "Plugin was not properly initialized: '" + javaPlugin.getClass().getName() + "'." );
		
		if ( loader.initialized )
			throw new IllegalArgumentException( "Plugin already initialized: '" + javaPlugin.getClass().getName() + "'." );
		
		javaPlugin.init( loader.loader, loader.description, loader.dataFolder, loader.file, loader );
		loader.initialized = true;
	}
	
	@Override
	protected Class<?> findClass( String name ) throws ClassNotFoundException
	{
		return findClass( name, true );
	}
	
	Class<?> findClass( String name, boolean checkGlobal ) throws ClassNotFoundException
	{
		if ( name.startsWith( "com.chiorichan." ) && !name.startsWith( "com.chiorichan.plugin." ) )
			throw new ClassNotFoundException( name );
		
		Class<?> result = classes.get( name );
		
		if ( result == null )
		{
			if ( checkGlobal )
				result = loader.getClassByName( name );
			
			if ( result == null )
			{
				result = super.findClass( name );
				
				if ( result != null )
					loader.setClass( name, result );
			}
			
			classes.put( name, result );
		}
		
		return result;
	}
	
	Set<String> getClasses()
	{
		return classes.keySet();
	}
	
	public Plugin getPlugin()
	{
		return plugin;
	}
	
	public PluginLoader getPluginLoader()
	{
		return loader;
	}
}
