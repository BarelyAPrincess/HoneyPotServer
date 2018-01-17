package io.amelia.foundation.plugins.loader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import io.amelia.foundation.plugins.PluginMeta;
import io.amelia.lang.PluginInvalidException;
import io.amelia.support.Objs;

/**
 * A ClassLoader for plugins, to allow shared classes across multiple plugins
 */
public final class PluginClassLoader extends URLClassLoader
{
	private static final Map<Class<?>, PluginClassLoader> loaders = new WeakHashMap<Class<?>, PluginClassLoader>();

	static synchronized void initialize( Plugin javaPlugin )
	{
		Objs.notNull( javaPlugin, "Initializing plugin cannot be null" );

		PluginClassLoader loader = loaders.get( javaPlugin.getClass() );

		if ( loader == null )
			throw new IllegalStateException( "Plugin was not properly initialized: '" + javaPlugin.getClass().getName() + "'." );

		if ( loader.initialized )
			throw new IllegalArgumentException( "Plugin already initialized: '" + javaPlugin.getClass().getName() + "'." );

		javaPlugin.init( loader.loader, loader.description, loader.dataFolder, loader.file, loader );
		loader.initialized = true;
	}

	final Plugin plugin;
	private final Map<String, Class<?>> classes = new HashMap<String, Class<?>>();
	private final File dataFolder;
	private final PluginMeta description;
	private final File file;
	private final JavaPluginLoader loader;
	private boolean initialized = false;

	PluginClassLoader( final JavaPluginLoader loader, final ClassLoader parent, final PluginMeta description, final File dataFolder, final File file ) throws PluginInvalidException, MalformedURLException
	{
		super( new URL[] {file.toURI().toURL()}, parent );

		Objs.notNull( loader, "Loader cannot be null" );

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
