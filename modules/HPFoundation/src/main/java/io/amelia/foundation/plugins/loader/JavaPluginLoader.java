/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation.plugins.loader;

import com.google.common.collect.ImmutableList;

import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import io.amelia.foundation.events.AbstractEvent;
import io.amelia.foundation.events.EventException;
import io.amelia.foundation.events.EventHandler;
import io.amelia.foundation.events.Events;
import io.amelia.foundation.events.RegisteredListener;
import io.amelia.foundation.events.PluginDisableEvent;
import io.amelia.foundation.events.PluginEnableEvent;
import io.amelia.foundation.plugins.PluginMeta;
import io.amelia.foundation.plugins.Plugins;
import io.amelia.lang.DeprecatedDetail;
import io.amelia.lang.PluginDependencyUnknownException;
import io.amelia.lang.PluginException;
import io.amelia.lang.PluginInvalidException;
import io.amelia.lang.PluginMetaException;
import io.amelia.lang.ReportingLevel;
import io.amelia.support.IO;
import io.amelia.support.Objs;
import io.amelia.data.parcel.ParcelLoader;

/**
 * Represents a Java plugin loader, allowing plugins in the form of .jar
 */
public final class JavaPluginLoader implements PluginLoader
{
	private final Map<String, Class<?>> classes = new HashMap<String, Class<?>>();
	private final Pattern[] fileFilters = new Pattern[] {Pattern.compile( "\\.jar$" )};
	private final Map<String, PluginClassLoader> loaders = new LinkedHashMap<String, PluginClassLoader>();

	/**
	 * Any class can be scanned for event methods.
	 */
	public Map<Class<? extends AbstractEvent>, Set<RegisteredListener>> createRegisteredListeners( Object listener, final Plugin plugin )
	{
		Objs.notNull( plugin, "Plugin can not be null" );
		Objs.notNull( listener, "Listener can not be null" );

		boolean useTimings = false; //EventDispatcher.i().useTimings();
		Map<Class<? extends AbstractEvent>, Set<RegisteredListener>> ret = new HashMap<Class<? extends AbstractEvent>, Set<RegisteredListener>>();
		Set<Method> methods;
		try
		{
			Method[] publicMethods = listener.getClass().getMethods();
			methods = new HashSet<Method>( publicMethods.length, Float.MAX_VALUE );
			for ( Method method : publicMethods )
				methods.add( method );
			for ( Method method : listener.getClass().getDeclaredMethods() )
				methods.add( method );
		}
		catch ( NoClassDefFoundError e )
		{
			Plugins.L.severe( "Plugin " + plugin.getMeta().getDisplayName() + " has failed to register events for " + listener.getClass() + " because " + e.getMessage() + " does not exist." );
			return ret;
		}

		for ( final Method method : methods )
		{
			final EventHandler eh = method.getAnnotation( EventHandler.class );
			if ( eh == null )
				continue;
			final Class<?> checkClass;
			if ( method.getParameterTypes().length != 1 || !AbstractEvent.class.isAssignableFrom( checkClass = method.getParameterTypes()[0] ) )
			{
				Plugins.L.severe( plugin.getMeta().getDisplayName() + " attempted to register an invalid EventHandler method signature \"" + method.toGenericString() + "\" in " + listener.getClass() );
				continue;
			}
			final Class<? extends AbstractEvent> eventClass = checkClass.asSubclass( AbstractEvent.class );
			method.setAccessible( true );
			Set<RegisteredListener> eventSet = ret.get( eventClass );
			if ( eventSet == null )
			{
				eventSet = new HashSet<RegisteredListener>();
				ret.put( eventClass, eventSet );
			}

			if ( ReportingLevel.E_DEPRECATED.isEnabled() )
				for ( Class<?> clazz = eventClass; AbstractEvent.class.isAssignableFrom( clazz ); clazz = clazz.getSuperclass() )
				{
					if ( clazz.isAnnotationPresent( DeprecatedDetail.class ) )
					{
						DeprecatedDetail deprecated = clazz.getAnnotation( DeprecatedDetail.class );
						Plugins.L.warning( String.format( "The plugin '%s' has registered a EventListener for %s on method '%s', but the event is Deprecated because '%s'; please notify the authors %s.", plugin.getMeta().getDisplayName(), clazz.getName(), method.toGenericString(), deprecated.reason(), Arrays.toString( plugin.getMeta().getAuthors().toArray() ) ) );
						break;
					}

					if ( clazz.isAnnotationPresent( Deprecated.class ) )
					{
						Plugins.L.warning( String.format( "The plugin '%s' has registered a EventListener for %s on method '%s', but the event is Deprecated! Please notify the authors %s.", plugin.getMeta().getDisplayName(), clazz.getName(), method.toGenericString(), Arrays.toString( plugin.getMeta().getAuthors().toArray() ) ) );
						break;
					}
				}

			RegisteredListener registeredListener = new RegisteredListener<>( plugin, eh.priority(), event -> {
				try
				{
					if ( !eventClass.isAssignableFrom( event.getClass() ) )
						return;
					method.invoke( listener, event );
				}
				catch ( InvocationTargetException ex )
				{
					throw new EventException.Error( ex.getCause() );
				}
				catch ( Throwable t )
				{
					throw new EventException.Error( t );
				}
			} );
			registeredListener.setUseTimings( useTimings );
			eventSet.add( registeredListener );
		}
		return ret;
	}

	@Override
	@SuppressWarnings( "resource" )
	public void disablePlugin( Plugin plugin )
	{
		if ( plugin.isEnabled() )
		{
			String message = String.format( "Disabling %s", plugin.getMeta().getDisplayName() );
			Plugins.L.info( message );

			Events.callEvent( new PluginDisableEvent( plugin ) );

			Plugin jPlugin = plugin;
			ClassLoader cloader = jPlugin.getClassLoader();

			try
			{
				jPlugin.setEnabled( false );
			}
			catch ( Throwable ex )
			{
				Plugins.L.log( Level.SEVERE, "Error occurred while disabling " + plugin.getMeta().getDisplayName() + " (Is it up to date?)", ex );
			}

			loaders.remove( jPlugin.getMeta().getName() );

			if ( cloader instanceof PluginClassLoader )
			{
				PluginClassLoader loader = ( PluginClassLoader ) cloader;
				Set<String> names = loader.getClasses();

				for ( String name : names )
					removeClass( name );
			}
		}
	}

	@Override
	public void enablePlugin( final Plugin plugin )
	{
		if ( !plugin.isEnabled() )
		{
			Plugins.L.info( "Enabling " + plugin.getMeta().getDisplayName() );

			Plugin jPlugin = plugin;

			String pluginName = jPlugin.getMeta().getName();

			if ( !loaders.containsKey( pluginName ) )
				loaders.put( pluginName, ( PluginClassLoader ) jPlugin.getClassLoader() );

			try
			{
				jPlugin.setEnabled( true );
			}
			catch ( PluginException.Unconfigured ex )
			{
				// Manually thrown by plugins to convey when they are unconfigured
				Plugins.L.severe( String.format( "The plugin %s has reported that it's unconfigured, the plugin has been disabled until this is resolved.", plugin.getMeta().getDisplayName() ), ex );
			}
			catch ( PluginException.Error ex )
			{
				// Manually thrown by plugins to convey an issue
				Plugins.L.severe( String.format( "The plugin %s has thrown the internal PluginException, the plugin has been disabled until this is resolved.", plugin.getMeta().getDisplayName() ), ex );
			}
			catch ( Throwable ex )
			{
				// Thrown for unexpected internal plugin problems
				Plugins.L.severe( String.format( "Error occurred while enabling %s (Is it up to date?)", plugin.getMeta().getDisplayName() ), ex );
			}

			// Perhaps abort here, rather than continue going, but as it stands,
			// an abort is not possible the way it's currently written
			Events.callEvent( new PluginEnableEvent( plugin ) );
		}
	}

	Class<?> getClassByName( final String name )
	{
		Class<?> cachedClass = classes.get( name );

		if ( cachedClass != null )
			return cachedClass;
		else
			for ( String current : loaders.keySet() )
			{
				PluginClassLoader loader = loaders.get( current );

				try
				{
					cachedClass = loader.findClass( name, false );
				}
				catch ( ClassNotFoundException cnfe )
				{
					// Ignore
				}
				if ( cachedClass != null )
					return cachedClass;
			}
		return null;
	}

	@SuppressWarnings( "unused" )
	private File getDataFolder( File file )
	{
		File dataFolder = null;

		String filename = file.getName();
		int index = file.getName().lastIndexOf( "." );

		if ( index != -1 )
		{
			String name = filename.substring( 0, index );

			dataFolder = new File( file.getParentFile(), name );
		}
		else
			dataFolder = new File( file.getParentFile(), filename + "_" );

		return dataFolder;
	}

	@Override
	public Pattern[] getPluginFileFilters()
	{
		return fileFilters.clone();
	}

	@Override
	public PluginMeta getPluginMeta( Path path ) throws PluginMetaException
	{
		Objs.notNull( path, "File cannot be null" );

		JarFile jar = null;
		InputStream stream = null;

		try
		{
			jar = new JarFile( path.toFile() );
			JarEntry entry = jar.getJarEntry( "plugin.yaml" );

			if ( entry == null )
				entry = jar.getJarEntry( "plugin.yml" );

			if ( entry == null )
				throw new PluginMetaException( new FileNotFoundException( "Jar does not contain plugin.yaml" ) );

			stream = jar.getInputStream( entry );

			// TODO Implement additional plugin meta types.
			return new PluginMeta( stream, ParcelLoader.Type.YAML );

		}
		catch ( YAMLException | IOException ex )
		{
			throw new PluginMetaException( ex );
		}
		finally
		{
			if ( jar != null )
				IO.closeQuietly( jar );
			if ( stream != null )
				IO.closeQuietly( stream );
		}
	}

	@Override
	public Plugin loadPlugin( @Nonnull Path pluginPath ) throws PluginInvalidException
	{
		if ( !Files.isRegularFile( pluginPath ) )
			throw new PluginInvalidException( new FileNotFoundException( pluginPath.toString() + " does not exist or is not a regular file" ) );

		PluginMeta pluginMeta;
		try
		{
			pluginMeta = getPluginMeta( pluginPath );
		}
		catch ( PluginMetaException ex )
		{
			throw new PluginInvalidException( ex );
		}

		Path dataPath = Paths.get( pluginMeta.getName().replaceAll( "\\W", "" ) ).resolve( pluginPath.getParent() );

		List<String> depend = pluginMeta.getDepend();
		if ( depend == null )
			depend = ImmutableList.of();

		for ( String pluginName : depend )
		{
			if ( loaders == null )
				throw new PluginDependencyUnknownException( pluginName );
			PluginClassLoader current = loaders.get( pluginName );

			if ( current == null )
				throw new PluginDependencyUnknownException( pluginName );
		}

		PluginClassLoader loader;
		try
		{
			loader = new PluginClassLoader( this, getClass().getClassLoader(), pluginMeta, dataPath, pluginPath );
		}
		catch ( PluginInvalidException ex )
		{
			throw ex;
		}
		catch ( Throwable ex )
		{
			throw new PluginInvalidException( ex );
		}

		loaders.put( pluginMeta.getName(), loader );

		if ( pluginMeta.getNatives().size() > 0 )
			try
			{
				IO.extractNatives( pluginMeta.getNatives(), pluginPath, dataPath );
			}
			catch ( IOException e )
			{
				Plugins.L.severe( "We had a problem trying to extract native libraries from plugin file '" + pluginPath + "':", e );
			}

		// Attempts to extract bundled library files
		IO.extractLibraries( pluginPath, dataPath );

		return loader.plugin;
	}

	private void removeClass( String name )
	{
		Class<?> clazz = classes.remove( name );

		try
		{
			/*if ( clazz != null && ConfigurationSerializable.class.isAssignableFrom( clazz ) )
			{
				Class<? extends ConfigurationSerializable> serializable = clazz.asSubclass( ConfigurationSerializable.class );
				ConfigurationSerialization.unregisterClass( serializable );
			}*/
		}
		catch ( NullPointerException ex )
		{
			// Boggle!
			// (Native methods throwing NPEs is not fun when you can't stop it before-hand)
		}
	}

	void setClass( final String name, final Class<?> clazz )
	{
		if ( !classes.containsKey( name ) )
		{
			classes.put( name, clazz );

			/*if ( ConfigurationSerializable.class.isAssignableFrom( clazz ) )
			{
				Class<? extends ConfigurationSerializable> serializable = clazz.asSubclass( ConfigurationSerializable.class );
				ConfigurationSerialization.registerClass( serializable );
			}*/
		}
	}
}
