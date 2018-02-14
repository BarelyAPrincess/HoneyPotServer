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
import java.util.Map;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import io.amelia.lang.ConfigException;
import io.amelia.support.IO;
import io.amelia.support.Objs;
import io.amelia.support.Strs;
import io.amelia.support.data.ParcelLoader;

public class ConfigRegistry
{
	public static final ConfigMap config = new ConfigMap();
	private static boolean isConfigLoaded;

	/*
	 * We set default config values here for end-user reference, they're then saved to the config file upon load (if unset).
	 */
	static
	{
		config.setValueIfAbsent( ConfigKeys.DEVELOPMENT_MODE, false );
	}

	public static void clearCache( @Nonnull File path, @Nonnegative long keepHistory )
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

	public static void clearCache( @Nonnegative long keepHistory )
	{
		clearCache( Kernel.getPath( Kernel.PATH_CACHE ), keepHistory );
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
		Kernel.setAppPath( IO.buildFile( false, env.getString( "app-dir" ).orElse( null ) ) );
		// for ( String key : new String[] {"webroot", "config", "plugins", "updates", "database", "storage", "sessions", "cache", "logs"} )
		// setPath( key, Strs.split( env.getString( "dir-" + key ), "/" ).toArray( String[]::new ) );

		env.getStringsMap().filter( e -> e.getKey().startsWith( "dir-" ) ).forEach( e -> Kernel.setPath( e.getKey().substring( 4 ), Strs.split( e.getValue(), "/" ).toArray( String[]::new ) ) );

		loadConfig();

		ConfigMap envNode = config.getChildOrCreate( "env" );
		for ( Map.Entry<String, Object> entry : env.map().entrySet() )
			envNode.setValue( entry.getKey().replace( '-', '_' ), entry.getValue() );
		envNode.addFlag( ConfigMap.Flag.READ_ONLY, ConfigMap.Flag.NO_SAVE );
	}

	public static boolean isConfigLoaded()
	{
		return isConfigLoaded;
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

		isConfigLoaded = true;
	}

	private static void loadConfig() throws ConfigException.Error
	{
		loadConfig( Kernel.getPath( Kernel.PATH_CONFIG, true ), "" );
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

		File configPath = Kernel.getPath( Kernel.PATH_CONFIG, true );

		IO.extractResourceDirectory( "config", configPath, ConfigRegistry.class );
	}

	private ConfigRegistry()
	{
		// Static Access
	}

	public static class ConfigKeys
	{
		public static final String APPLICATION_BASE = "app";
		public static final String WARN_ON_OVERLOAD = APPLICATION_BASE + ".warnOnOverload";
		public static final String DEVELOPMENT_MODE = APPLICATION_BASE + ".developmentMode";
		public static final String CONFIGURATION_BASE = "conf";

		private ConfigKeys()
		{
			// Static Access
		}
	}
}
