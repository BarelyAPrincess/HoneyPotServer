package io.amelia.foundation;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.annotation.Nonnull;

import io.amelia.lang.ConfigException;
import io.amelia.support.IO;
import io.amelia.support.Objs;
import io.amelia.support.Strs;
import io.amelia.support.data.ParcelLoader;

public class ConfigRegistry
{
	public static final ConfigMap config = new ConfigMap();

	/*
	 * We set default config values here for end-user reference, they're then saved to the config file upon load (if unset).
	 */
	static
	{
		config.setValue( "app.developmentMode", false );
	}

	public static void clearCache( @Nonnull File path, @Nonnull long keepHistory )
	{
		Objs.notNull( path );
		Objs.notNull( keepHistory );

		if ( path.isDirectory() )
		{
			for ( File f : path.listFiles() )
				if ( f.isFile() && f.lastModified() < System.currentTimeMillis() - keepHistory * 24 * 60 * 60 )
					f.delete();
				else if ( f.isDirectory() )
					clearCache( f, keepHistory );
		}
	}

	public static void clearCache( @Nonnull long keepHistory )
	{
		clearCache( App.getPath( App.PATH_CACHE ), keepHistory );
	}

	private static void decodeMap( Map<String, Object> mapEncoded, ConfigMap root )
	{
		for ( Map.Entry<String, Object> entry : mapEncoded.entrySet() )
		{
			if ( entry.getKey().equals( "__value" ) )
				root.setValue( entry.getValue() );
			else
			{
				ConfigMap child = root.getChildOrCreate( entry.getKey() );

				if ( entry.getValue() instanceof Map )
					decodeMap( ( Map<String, Object> ) entry.getValue(), child );
				else
					child.loadNewValue( entry.getValue() );
			}
		}
	}

	public static ConfigMap getChild( String key )
	{
		return config.getChild( key );
	}

	public static ConfigMap getChildOrCreate( String key )
	{
		return config.getChildOrCreate( key );
	}

	public static void init( Env env ) throws ConfigException.Error
	{
		App.setAppPath( IO.buildFile( true, env.getString( "app-dir" ) ) );
		// for ( String key : new String[] {"webroot", "config", "plugins", "updates", "database", "storage", "sessions", "cache", "logs"} )
		// setPath( key, Strs.split( env.getString( "dir-" + key ), "/" ).toArray( String[]::new ) );

		env.getStringsMap().filter( e -> e.getKey().startsWith( "dir-" ) ).forEach( e -> App.setPath( e.getKey().substring( 4 ), Strs.split( e.getValue(), "/" ).toArray( String[]::new ) ) );

		loadConfig();

		ConfigMap envNode = config.getChild( "env" );
		for ( Map.Entry<String, Object> entry : env.map().entrySet() )
			envNode.setValue( entry.getKey(), entry.getValue() );
		envNode.addFlag( ConfigMap.Flag.READ_ONLY, ConfigMap.Flag.NO_SAVE );
	}

	private static void loadConfig( @Nonnull File configPath, @Nonnull String nestingPrefix ) throws ConfigException.Error
	{
		if ( !configPath.isDirectory() )
			throw new ConfigException.Error( config, "Provided configPath is not a directory." );

		for ( File file : configPath.listFiles() )
		{
			String nesting = Strs.join( new String[] {nestingPrefix, IO.dirname( file )}, "." );

			try
			{
				if ( file.isDirectory() )
					loadConfig( file, nesting );
				else
					parseConfig( nesting, file );

			}
			catch ( Exception e )
			{
				throw new ConfigException.Error( config, "Failed to load configuration file " + IO.relPath( file ), e );
			}
		}
	}

	private static void loadConfig() throws ConfigException.Error
	{
		loadConfig( App.getPath( App.PATH_CONFIG, true ), "" );
	}

	private static void parseConfig( @Nonnull String nesting, @Nonnull File file ) throws ConfigException.Error
	{
		if ( !file.isFile() )
			return;

		Map<String, Object> map;
		ConfigMap child = config.getChildOrCreate( nesting );
		String name = file.getName().toLowerCase();

		try
		{
			if ( name.endsWith( ".yaml" ) || name.endsWith( ".yml" ) )
				map = ParcelLoader.decodeYamlToMap( file );
			else if ( name.endsWith( ".json" ) )
				map = ParcelLoader.decodeJsonToMap( file );
			else if ( name.endsWith( ".list" ) )
				map = ParcelLoader.decodeListToMap( file );
			else if ( name.endsWith( ".properties" ) )
				map = ParcelLoader.decodePropToMap( file );
			else
				throw new ConfigException.Ignorable( null, "Could not parse file " + IO.relPath( file ) );

			// TODO Add more supported types, .e.g., `.groovy` using the ScriptingFactory.
		}
		catch ( IOException e )
		{
			throw new ConfigException.Error( child, e );
		}

		ParcelLoader.decodeMap( map, child );
	}

	public static void save()
	{
		// TODO Save
	}

	public static void setObject( String key, Object value )
	{
		if ( value instanceof ConfigMap )
			config.setChild( key, ( ConfigMap ) value, false );
		else
			config.setValue( key, value );
	}

	private static void vendorConfig() throws IOException
	{
		// WIP Copies config from resources and plugins to config directories.

		File configPath = App.getPath( App.PATH_CONFIG, true );

		IO.extractResourceDirectory( "config", configPath, ConfigRegistry.class );
	}

	public static boolean warnOnOverload()
	{
		return config.isTrue( "general.warnOnOverload" );
	}

	private ConfigRegistry()
	{
		// Static Access
	}
}
