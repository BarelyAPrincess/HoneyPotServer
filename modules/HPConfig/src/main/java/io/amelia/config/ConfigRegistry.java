package io.amelia.config;

import com.sun.istack.internal.NotNull;
import io.amelia.env.Env;
import io.amelia.foundation.binding.AppBindings;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.ConfigException;
import io.amelia.support.Arrs;
import io.amelia.support.LibIO;
import io.amelia.support.Lists;
import io.amelia.support.Maps;
import io.amelia.support.Objs;
import io.amelia.support.Strs;
import io.amelia.util.OptionalBoolean;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class ConfigRegistry
{
	public static final String PATH_APP = "__app";
	public static final String PATH_WEBROOT = "__webroot";
	public static final String PATH_CONFIG = "__config";
	public static final String PATH_PLUGINS = "__plugins";
	public static final String PATH_UPDATES = "__updates";
	public static final String PATH_DATABASE = "__database";
	public static final String PATH_STORAGE = "__storage";
	public static final String PATH_SESSIONS = "__sessions";
	public static final String PATH_CACHE = "__cache";
	public static final String PATH_LOGS = "__logs";

	private static final Map<String, List<String>> appPaths = new ConcurrentHashMap<>();
	private static File appPath = LibIO.buildFile( true );
	private static File configFile = null;
	private static ConfigNode node = new ConfigNode();

	static
	{
		setPath( "webroot", PATH_APP, "webroot" );
		setPath( "config", PATH_APP, "config" );
		setPath( "plugins", PATH_APP, "plugins" );
		setPath( "updates", PATH_APP, "updates" );
		setPath( "database", PATH_APP, "database" );
		setPath( "storage", PATH_APP, "storage" );
		setPath( "sessions", PATH_STORAGE, "sessions" );
		setPath( "cache", PATH_STORAGE, "cache" );
		setPath( "logs", PATH_STORAGE, "logs" );
	}

	public static OptionalBoolean getBoolean( String key )
	{
		return node.getBoolean( key );
	}

	public static ConfigNode getChild( String key, boolean create )
	{
		return node.getChild( key, create );
	}

	public static ConfigNode getChild( String key )
	{
		return node.getChild( key );
	}

	public static OptionalDouble getDouble( String key )
	{
		return node.getDouble( key );
	}

	public static OptionalInt getInteger( String key )
	{
		return node.getInteger( key );
	}

	public static <T> List<T> getList( String key )
	{
		return node.getList( key );
	}

	public static OptionalLong getLong( String key )
	{
		return node.getLong( key );
	}

	public static File getPath( @NotNull String slug )
	{
		return getPath( new String[] {slug} );
	}

	public static File getPath( @NotNull String slug, boolean createPath )
	{
		return getPath( new String[] {slug}, createPath );
	}

	public static File getPath( @NotNull String[] slugs )
	{
		return getPath( slugs, false );
	}

	/**
	 * Builds a directory based on the provided slugs.
	 * Key based paths MUST start with double underscores.
	 * <p>
	 * The options are as follows:
	 * __app
	 * __webroot
	 * __config
	 * __plugins
	 * __updates
	 * __database
	 * __storage
	 * __sessions
	 * __cache
	 * __logs
	 * <p>
	 * Slugs not starting with double underscores will be treated as either a relative
	 * or absolute path depending on if it starts with a single forward slash.
	 * <p>
	 * Examples:
	 * __app -> /usr/share/honeypot
	 * __sessions -> /usr/share/honeypot/storage/sessions
	 * relative -> /usr/share/honeypot/relative
	 * /absolute -> /absolute
	 * <p>
	 *
	 * @param slugs      The path slugs
	 * @param createPath Should we try creating the directory if it doesn't exist?
	 * @return The absolute File
	 * @throws ApplicationException.Ignorable
	 */
	public static File getPath( @NotNull String[] slugs, boolean createPath )
	{
		Objs.notNull( slugs );

		if ( slugs.length == 0 )
			return getPath();

		if ( slugs[0].startsWith( "__" ) )
		{
			String key = slugs[0].substring( 2 );
			if ( key.equals( "app" ) )
				slugs[0] = getPath().toString();
			else if ( appPaths.containsKey( key ) )
				slugs = ( String[] ) Stream.concat( appPaths.get( key ).stream(), Arrays.stream( slugs ).skip( 1 ) ).toArray();
			else
				throw ApplicationException.ignorable( "Path " + key + " is not set!" );

			return getPath( slugs, createPath );
		}
		else if ( !slugs[0].startsWith( "/" ) )
			slugs = Arrs.prepend( slugs, getPath().toString() );

		File path = LibIO.buildFile( true, slugs );

		if ( createPath && !path.exists() )
			if ( !path.mkdirs() )
				throw ApplicationException.ignorable( "The path \"" + path.getAbsolutePath() + "\" does not exist and we failed to create it." );

		return path;
	}

	public static File getPath()
	{
		return appPath;
	}

	public static Optional<String> getString( String key )
	{
		return node.getString( key );
	}

	public static void init( Env env ) throws ConfigException.Error
	{
		appPath = LibIO.buildFile( true, env.getString( "app-dir" ) );
		for ( String key : new String[] {"webroot", "config", "plugins", "updates", "database", "storage", "sessions", "cache", "logs"} )
			setPath( key, Strs.split( env.getString( key + "-dir" ), "/" ).toArray( String[]::new ) );

		loadConfig();

		ConfigNode envNode = node.getChild( "env" );
		for ( Map.Entry<String, Object> entry : env.map().entrySet() )
			envNode.setValue( entry.getKey(), entry.getValue() );
		envNode.addFlag( ConfigNode.Flag.READ_ONLY, ConfigNode.Flag.NO_SAVE );

		AppBindings.init();
	}

	private static void loadConfig( @NotNull File configPath, @NotNull String nestingPrefix ) throws ConfigException.Error
	{
		if ( configPath == null || !configPath.isDirectory() )
			throw new ConfigException.Error( node, "Provided configPath is not a directory." );

		for ( File file : configPath.listFiles() )
		{
			String nesting = Strs.join( new String[] {nestingPrefix, LibIO.dirname( file )}, "." );

			try
			{
				if ( file.isDirectory() )
					loadConfig( file, nesting );
				else
				{
					String name = file.getName().toLowerCase();

					if ( name.endsWith( ".yaml" ) )
						node.setValue( nesting, ConfigLoader.parseYaml( file ) );

					if ( name.endsWith( ".json" ) )
						node.setValue( nesting, ConfigLoader.parseJson( file ) );

					if ( name.endsWith( ".list" ) )
						node.setValue( nesting, LibIO.readFileToLines( file, "#" ) );

					if ( name.endsWith( ".properties" ) )
					{
						Properties prop = new Properties();
						prop.load( new FileReader( file ) );
						node.setValue( nesting, Maps.builder( prop ).hashMap() );
					}

					// TODO Add more supported types

					//if ( file.getName().endsWith( ".groovy" ) )
					// Future Use - Parse using scripting factory.
				}
			}
			catch ( Exception e )
			{
				throw new ConfigException.Error( node, "Failed to load configuration file " + LibIO.relPath( file ), e );
			}
		}
	}

	private static void loadConfig() throws ConfigException.Error
	{
		loadConfig( getPath( ConfigRegistry.PATH_CONFIG, true ), "" );
	}

	public static void setObject( String key, Object value )
	{
		if ( value instanceof ConfigNode )
			node.setChild( key, ( ConfigNode ) value, false );
		else
			node.setValue( key, value );
	}

	public static void setPath( @NotNull String key, @NotNull String... paths )
	{
		key = key.toLowerCase();
		if ( "app".equals( key ) )
			throw new IllegalArgumentException( "App path is set using the setAppPath() method." );
		if ( !Paths.get( paths[0] ).isAbsolute() && !paths[0].startsWith( "__" ) )
			throw new IllegalArgumentException( "App paths must be absolute or reference another app path, i.e., __app. Paths: [" + Strs.join( paths ) + "]" );
		appPaths.put( key, Lists.newArrayList( paths ) );
	}

	private static void vendorConfig() throws IOException
	{
		// WIP Copies config from resources and plugins to config directories.

		File configPath = getPath( ConfigRegistry.PATH_CONFIG, true );

		LibIO.extractResourceDirectory( "config", configPath, ConfigRegistry.class );
	}

	public static boolean warnOnOverload()
	{
		return false;
	}

	/**
	 * Keep ConfigRegistry a pure static class
	 */
	private ConfigRegistry()
	{

	}
}
