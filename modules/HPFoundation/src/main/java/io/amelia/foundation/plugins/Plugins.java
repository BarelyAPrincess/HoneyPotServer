/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation.plugins;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.amelia.foundation.Foundation;
import io.amelia.foundation.Kernel;
import io.amelia.foundation.events.EventHandlers;
import io.amelia.foundation.events.EventPriority;
import io.amelia.foundation.events.Events;
import io.amelia.foundation.events.builtin.RunlevelEvent;
import io.amelia.foundation.plugins.loader.JavaPluginLoader;
import io.amelia.foundation.plugins.loader.Plugin;
import io.amelia.foundation.plugins.loader.PluginClassLoader;
import io.amelia.foundation.plugins.loader.PluginLoader;
import io.amelia.foundation.tasks.Tasks;
import io.amelia.injection.Libraries;
import io.amelia.injection.MavenReference;
import io.amelia.lang.PluginDependencyUnknownException;
import io.amelia.lang.PluginException;
import io.amelia.lang.PluginInvalidException;
import io.amelia.lang.PluginMetaException;
import io.amelia.lang.PluginNotFoundException;
import io.amelia.logcompat.LogBuilder;
import io.amelia.logcompat.Logger;
import io.amelia.support.IO;
import io.amelia.support.Objs;
import io.amelia.support.Runlevel;

public class Plugins
{
	public static final Logger L = LogBuilder.get( Plugins.class );

	private static final Map<Pattern, PluginLoader> fileAssociations = new HashMap<>();
	private static final ReentrantLock lock = new ReentrantLock();
	private static final Map<String, Plugin> lookupNames = new HashMap<>();
	private static final List<Plugin> plugins = new ArrayList<>();
	private static Set<String> loadedPlugins = new HashSet<>();

	static
	{
		// Loads plugins in order as PluginManager receives the notices from the Events
		Events.listen( Foundation.getApplication(), EventPriority.NORMAL, RunlevelEvent.class, event -> {
			Runlevel level = event.getRunLevel();

			Plugin[] plugins = getPlugins();

			for ( Plugin plugin : plugins )
				if ( !plugin.isEnabled() && plugin.getMeta().getLoad() == level )
					enablePlugin( plugin );
		} );
	}

	private static void checkUpdate( File file )
	{
		if ( !Kernel.getPath( Kernel.PATH_UPDATES ).isDirectory() )
			return;

		File updateFile = new File( Kernel.getPath( Kernel.PATH_UPDATES ), file.getName() );
		if ( updateFile.isFile() && IO.copy( updateFile, file ) )
			updateFile.delete();
	}

	public static void clearPlugins()
	{
		lock.lock();
		try
		{
			disablePlugins();
			plugins.clear();
			lookupNames.clear();
			EventHandlers.unregisterAll();
			fileAssociations.clear();
		}
		finally
		{
			lock.unlock();
		}
	}

	public static void disablePlugin( final Plugin plugin )
	{
		if ( plugin.isEnabled() )
		{
			try
			{
				plugin.getPluginLoader().disablePlugin( plugin );
			}
			catch ( NoClassDefFoundError ex )
			{
				// Ignore
			}
			catch ( Throwable ex )
			{
				L.log( Level.SEVERE, "Error occurred (in the plugin loader) while disabling " + plugin.getMeta().getDisplayName() + " (Is it up to date?)", ex );
			}

			try
			{
				Tasks.cancelTasks( plugin );
			}
			catch ( NoClassDefFoundError ex )
			{
				// Ignore
			}
			catch ( Throwable ex )
			{
				L.log( Level.SEVERE, "Error occurred (in the plugin loader) while cancelling tasks for " + plugin.getMeta().getDisplayName() + " (Is it up to date?)", ex );
			}

			try
			{
				// Loader.getServicesManager().unregisterAll( plugin );
			}
			catch ( NoClassDefFoundError ex )
			{
				// Ignore
			}
			catch ( Throwable ex )
			{
				L.log( Level.SEVERE, "Error occurred (in the plugin loader) while unregistering services for " + plugin.getMeta().getDisplayName() + " (Is it up to date?)", ex );
			}

			try
			{
				EventHandlers.unregisterAll( plugin );
			}
			catch ( NoClassDefFoundError ex )
			{
				// Ignore
			}
			catch ( Throwable ex )
			{
				L.log( Level.SEVERE, "Error occurred (in the plugin loader) while unregistering events for " + plugin.getMeta().getDisplayName() + " (Is it up to date?)", ex );
			}

			try
			{
				// TODO Implement here the unregistering of plugin loopers

				// Loader.getMessenger().unregisterIncomingPluginChannel( plugin );
				// Loader.getMessenger().unregisterOutgoingPluginChannel( plugin );
			}
			catch ( NoClassDefFoundError ex )
			{
				// Ignore
			}
			catch ( Throwable ex )
			{
				L.log( Level.SEVERE, "Error occurred (in the plugin loader) while unregistering plugin channels for " + plugin.getMeta().getDisplayName() + " (Is it up to date?)", ex );
			}
		}
	}

	public static void disablePlugins()
	{
		Plugin[] plugins = getPlugins();
		for ( int i = plugins.length - 1; i >= 0; i-- )
			disablePlugin( plugins[i] );
	}

	public static void enablePlugin( final Plugin plugin )
	{
		if ( !plugin.isEnabled() )
			try
			{
				plugin.getPluginLoader().enablePlugin( plugin );
			}
			catch ( Throwable ex )
			{
				L.log( Level.SEVERE, "Error occurred (in the plugin loader) while enabling " + plugin.getMeta().getDisplayName() + " (Check for Version Mismatch)", ex );
			}
	}

	public static Plugin getPluginByClass( Class<?> clz ) throws PluginNotFoundException
	{
		try
		{
			if ( clz.getClassLoader() instanceof PluginClassLoader )
				for ( Plugin plugin : getPlugins() )
					if ( plugin == ( ( PluginClassLoader ) clz.getClassLoader() ).getPlugin() )
						return plugin;
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}

		throw new PluginNotFoundException( "We could not find a plugin with the class '" + clz.getSimpleName() + "', maybe it's not loaded." );
	}

	public static Plugin getPluginByClassWithoutException( Class<?> clz )
	{
		try
		{
			return getPluginByClass( clz );
		}
		catch ( PluginNotFoundException e )
		{
			L.finest( e.getMessage() );
			return null;
		}
	}

	public static Plugin getPluginByClassname( String className ) throws PluginNotFoundException
	{
		try
		{
			for ( Plugin plugin1 : getPlugins() )
				if ( plugin1.getClass().getName().startsWith( className ) )
					return plugin1;
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}

		throw new PluginNotFoundException( "We could not find a plugin with the classname '" + className + "', maybe it's not loaded." );
	}

	public static Plugin getPluginByClassnameWithoutException( String className )
	{
		try
		{
			return getPluginByClassname( className );
		}
		catch ( PluginNotFoundException e )
		{
			L.finest( e.getMessage() );
			return null;
		}
	}

	public static Plugin getPluginByName( String pluginName ) throws PluginNotFoundException
	{
		try
		{
			for ( Plugin plugin1 : getPlugins() )
				if ( plugin1.getClass().getCanonicalName().equals( pluginName ) || plugin1.getName().equalsIgnoreCase( pluginName ) )
					return plugin1;
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}

		throw new PluginNotFoundException( "We could not find a plugin by the name '" + pluginName + "', maybe it's not loaded." );
	}

	public static Plugin getPluginByNameWithoutException( String pluginName )
	{
		try
		{
			return getPluginByName( pluginName );
		}
		catch ( PluginNotFoundException e )
		{
			L.finest( e.getMessage() );
			return null;
		}
	}

	public static synchronized Plugin[] getPlugins()
	{
		return plugins.toArray( new Plugin[0] );
	}

	/**
	 * Loads the plugin in the specified file
	 * <p>
	 * File must be valid according to the current enabled Plugin interfaces
	 * <p>
	 *
	 * @param file File containing the plugin to load
	 *
	 * @return The Plugin instance
	 *
	 * @throws PluginInvalidException           Thrown when the specified file is not a valid plugin
	 * @throws PluginDependencyUnknownException If a required dependency could not be found
	 */
	public static synchronized Plugin loadPlugin( File file ) throws PluginInvalidException, PluginDependencyUnknownException
	{
		Objs.notNull( file, "File cannot be null" );

		checkUpdate( file );

		Set<Pattern> filters = fileAssociations.keySet();
		Plugin result = null;

		for ( Pattern filter : filters )
		{
			String name = file.getName();
			Matcher match = filter.matcher( name );

			if ( match.find() )
			{
				PluginLoader loader = fileAssociations.get( filter );

				result = loader.loadPlugin( file );
			}
		}

		if ( result != null )
		{
			plugins.add( result );
			lookupNames.put( result.getMeta().getName(), result );
		}

		return result;
	}

	protected static void loadPlugin( Plugin plugin )
	{
		try
		{
			enablePlugin( plugin );
		}
		catch ( Throwable ex )
		{
			L.log( Level.SEVERE, ex.getMessage() + " loading " + plugin.getMeta().getDisplayName() + " (Is it up to date?)", ex );
		}
	}

	public static void loadPlugins() throws PluginException.Error
	{
		registerInterface( JavaPluginLoader.class );
		// registerInterface( GroovyPluginLoader.class );

		File pluginFolder = Kernel.getPath( Kernel.PATH_PLUGINS );
		if ( pluginFolder.exists() )
		{
			Plugin[] plugins = loadPlugins( pluginFolder );
			for ( Plugin plugin : plugins )
				try
				{
					String message = String.format( "Loading %s", plugin.getMeta().getDisplayName() );
					L.info( message );
					plugin.onLoad();
				}
				catch ( Throwable ex )
				{
					L.log( Level.SEVERE, ex.getMessage() + " initializing " + plugin.getMeta().getDisplayName() + " (Is it up to date?)", ex );
				}
		}
		else
			pluginFolder.mkdir();
	}

	/**
	 * Loads the plugins contained within the specified directory
	 * <p>
	 *
	 * @param directory Directory to check for plugins
	 *
	 * @return A list of all plugins loaded
	 */
	public static Plugin[] loadPlugins( File directory )
	{
		Objs.notNull( directory, "Directory cannot be null" );
		Objs.notFalse( directory.isDirectory(), "Directory must be a directory" );

		List<Plugin> result = new ArrayList<Plugin>();
		Set<Pattern> filters = fileAssociations.keySet();

		Map<String, File> plugins = new HashMap<String, File>();
		Map<String, Collection<MavenReference>> libraries = new HashMap<>();
		Map<String, Collection<String>> dependencies = new HashMap<>();
		Map<String, Collection<String>> softDependencies = new HashMap<>();

		// This is where it figures out all possible plugins
		for ( File file : directory.listFiles() )
		{
			PluginLoader loader = null;
			for ( Pattern filter : filters )
			{
				Matcher match = filter.matcher( file.getName() );
				if ( match.find() )
					loader = fileAssociations.get( filter );
			}

			if ( loader == null )
				continue;

			PluginMeta description = null;
			try
			{
				description = loader.getPluginMeta( file );
			}
			catch ( PluginMetaException ex )
			{
				L.log( Level.SEVERE, "Could not load '" + file.getPath() + "' in folder '" + directory.getPath() + "'", ex );
				continue;
			}

			plugins.put( description.getName(), file );

			Collection<String> softDependencySet = description.getSoftDepend();
			if ( softDependencySet != null )
				if ( softDependencies.containsKey( description.getName() ) )
					// Duplicates do not matter, they will be removed together if applicable
					softDependencies.get( description.getName() ).addAll( softDependencySet );
				else
					softDependencies.put( description.getName(), new LinkedList<>( softDependencySet ) );

			Collection<MavenReference> librariesSet = description.getLibraries();
			if ( librariesSet != null )
				libraries.put( description.getName(), new LinkedList<>( librariesSet ) );

			Collection<String> dependencySet = description.getDepend();
			if ( dependencySet != null )
				dependencies.put( description.getName(), new LinkedList<>( dependencySet ) );

			Collection<String> loadBeforeSet = description.getLoadBefore();
			if ( loadBeforeSet != null )
				for ( String loadBeforeTarget : loadBeforeSet )
					if ( softDependencies.containsKey( loadBeforeTarget ) )
						softDependencies.get( loadBeforeTarget ).add( description.getName() );
					else
					{
						// softDependencies is never iterated, so 'ghost' plugins aren't an issue
						Collection<String> shortSoftDependency = new LinkedList<>();
						shortSoftDependency.add( description.getName() );
						softDependencies.put( loadBeforeTarget, shortSoftDependency );
					}
		}

		while ( !plugins.isEmpty() )
		{
			boolean missingDependency = true;
			Iterator<String> pluginIterator = plugins.keySet().iterator();

			while ( pluginIterator.hasNext() )
			{
				String plugin = pluginIterator.next();

				if ( libraries.containsKey( plugin ) )
				{
					Iterator<MavenReference> librariesIterator = libraries.get( plugin ).iterator();

					while ( librariesIterator.hasNext() )
					{
						MavenReference library = librariesIterator.next();

						if ( Libraries.isLoaded( library ) )
							librariesIterator.remove();
						else if ( !Libraries.loadLibrary( library ) )
						{
							missingDependency = false;
							File file = plugins.get( plugin );
							pluginIterator.remove();
							libraries.remove( plugin );
							softDependencies.remove( plugin );
							dependencies.remove( plugin );

							L.severe( "Could not load '" + file.getPath() + "' in folder '" + directory.getPath() + "' due to issue with library '" + library + "'." );
							break;
						}
					}
				}

				if ( dependencies.containsKey( plugin ) )
				{
					Iterator<String> dependencyIterator = dependencies.get( plugin ).iterator();

					while ( dependencyIterator.hasNext() )
					{
						String dependency = dependencyIterator.next();

						// Dependency loaded
						if ( loadedPlugins.contains( dependency ) )
							dependencyIterator.remove();
						else if ( !plugins.containsKey( dependency ) )
						{
							missingDependency = false;
							File file = plugins.get( plugin );
							pluginIterator.remove();
							libraries.remove( plugin );
							softDependencies.remove( plugin );
							dependencies.remove( plugin );

							L.severe( "Could not load '" + file.getPath() + "' in folder '" + directory.getPath() + "'", new PluginDependencyUnknownException( dependency ) );
							break;
						}
					}

					if ( dependencies.containsKey( plugin ) && dependencies.get( plugin ).isEmpty() )
						dependencies.remove( plugin );
				}
				if ( softDependencies.containsKey( plugin ) )
				{
					// Soft depend is no longer around
					softDependencies.get( plugin ).removeIf( softDependency -> !plugins.containsKey( softDependency ) );

					if ( softDependencies.get( plugin ).isEmpty() )
						softDependencies.remove( plugin );
				}
				if ( !( dependencies.containsKey( plugin ) || softDependencies.containsKey( plugin ) ) && plugins.containsKey( plugin ) )
				{
					// We're clear to load, no more soft or hard dependencies left
					File file = plugins.get( plugin );
					pluginIterator.remove();
					missingDependency = false;

					try
					{
						result.add( loadPlugin( file ) );
						loadedPlugins.add( plugin );
						continue;
					}
					catch ( PluginInvalidException ex )
					{
						L.severe( "Could not load '" + file.getPath() + "' in folder '" + directory.getPath() + "'", ex );
					}
				}
			}

			if ( missingDependency )
			{
				// We now iterate over plugins until something loads
				// This loop will ignore soft dependencies
				pluginIterator = plugins.keySet().iterator();

				while ( pluginIterator.hasNext() )
				{
					String plugin = pluginIterator.next();

					if ( !dependencies.containsKey( plugin ) )
					{
						softDependencies.remove( plugin );
						missingDependency = false;
						File file = plugins.get( plugin );
						pluginIterator.remove();

						try
						{
							result.add( loadPlugin( file ) );
							loadedPlugins.add( plugin );
							break;
						}
						catch ( PluginInvalidException ex )
						{
							L.severe( "Could not load '" + file.getPath() + "' in folder '" + directory.getPath() + "'", ex );
						}
					}
				}
				// We have no plugins left without a depend
				if ( missingDependency )
				{
					softDependencies.clear();
					dependencies.clear();
					Iterator<File> failedPluginIterator = plugins.values().iterator();

					while ( failedPluginIterator.hasNext() )
					{
						File file = failedPluginIterator.next();
						failedPluginIterator.remove();
						L.severe( "Could not load '" + file.getPath() + "' in folder '" + directory.getPath() + "': circular dependency detected" );
					}
				}
			}
		}

		return result.toArray( new Plugin[result.size()] );
	}

	/**
	 * Registers the specified plugin loader
	 * <p>
	 *
	 * @param loader Class name of the PluginLoader to register
	 *
	 * @throws IllegalArgumentException Thrown when the given Class is not a valid PluginLoader
	 */
	public static void registerInterface( Class<? extends PluginLoader> loader ) throws IllegalArgumentException
	{
		PluginLoader instance;

		if ( PluginLoader.class.isAssignableFrom( loader ) )
		{
			Constructor<? extends PluginLoader> constructor;

			try
			{
				constructor = loader.getConstructor();
				instance = constructor.newInstance();
			}
			catch ( NoSuchMethodException ex )
			{
				/*
				 * try
				 * {
				 * constructor = loader.getConstructor( AppController.class );
				 * instance = constructor.newInstance( AppController.instance );
				 * }
				 * catch ( NoSuchMethodException ex1 )
				 * {
				 */
				String className = loader.getName();
				throw new IllegalArgumentException( String.format( "Class %s does not have a public %s(Server) constructor", className, className ), ex );
				/*
				 * }
				 * catch ( Exception ex1 )
				 * {
				 * throw new IllegalArgumentException( String.format( "Unexpected exception %s while attempting to construct a new instance of %s", ex.getClass().getProductName(), loader.getProductName() ), ex1 );
				 * }
				 */
			}
			catch ( Exception ex )
			{
				throw new IllegalArgumentException( String.format( "Unexpected exception %s while attempting to construct a new instance of %s", ex.getClass().getName(), loader.getName() ), ex );
			}
		}
		else
			throw new IllegalArgumentException( String.format( "Class %s does not implement interface PluginLoader", loader.getName() ) );

		Pattern[] patterns = instance.getPluginFileFilters();

		lock.lock();
		try
		{
			for ( Pattern pattern : patterns )
				fileAssociations.put( pattern, instance );
		}
		finally
		{
			lock.unlock();
		}
	}

	public static void shutdown()
	{
		clearPlugins();
	}

	public Plugins()
	{
		// Static Manager
	}
}
