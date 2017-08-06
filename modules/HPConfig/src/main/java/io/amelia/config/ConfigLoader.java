package io.amelia.config;

import io.amelia.lang.ConfigException;
import io.amelia.support.LibIO;
import io.amelia.support.Maps;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class ConfigLoader
{
	public static Map<String, Object> parseFile( File file ) throws IOException
	{
		if ( file.isDirectory() )
			return null;

		String name = file.getName().toLowerCase();

		if ( name.endsWith( ".yaml" ) || name.endsWith( ".yml" ) )
			return parseYaml( file );

		if ( name.endsWith( ".json" ) )
			return parseJson( file );

		if ( name.endsWith( ".list" ) )
			return Maps.builder().increment( LibIO.readFileToLines( file, "#" ) ).castTo( String.class, Object.class ).hashMap();

		if ( name.endsWith( ".properties" ) )
		{
			Properties prop = new Properties();
			prop.load( new FileReader( file ) );
			return Maps.builder( prop ).castTo( String.class, Object.class ).hashMap();
		}

		// TODO Add more supported types

		//if ( file.getName().endsWith( ".groovy" ) )
		// Future Use - Parse using scripting factory.

		throw new ConfigException.Ignorable( null, "Could not parse file " + LibIO.relPath( file ) );
	}

	public static Map<String, Object> parseJson( File file )
	{

	}

	private static Map<String, Object> parseJson( InputStream inputStream )
	{

	}

	public static Map<String, Object> parseStream( InputStream stream, StreamType type ) throws IOException
	{
		if ( type == StreamType.AUTO_DETECT )
			throw new ConfigException.Ignorable( null, "Unfortunately StreamType AUTO_DETECT is not implemented. Future use." );

		if ( type == StreamType.YAML )
			return parseYaml( stream );

		if ( type == StreamType.JSON )
			return parseJson( stream );

		if ( type == StreamType.LIST )
			return Maps.builder().increment( LibIO.readStreamToLines( stream, "#" ) ).castTo( String.class, Object.class ).hashMap();

		if ( type == StreamType.PROP )
		{
			Properties prop = new Properties();
			prop.load( stream );
			return Maps.builder( prop ).castTo( String.class, Object.class ).hashMap();
		}

		// TODO Add more supported types

		//if ( file.getName().endsWith( ".groovy" ) )
		// Future Use - Parse using scripting factory.

		throw new ConfigException.Ignorable( null, "Could not parse stream" );
	}

	private static Map<String, Object> parseYaml( InputStream inputStream )
	{

	}

	public static Map<String, Object> parseYaml( File file )
	{

	}

	public enum StreamType
	{
		/**
		 * .json
		 */
		JSON,
		/**
		 * .yaml (or .yml if you so prefer)
		 */
		YAML,
		/**
		 * .list
		 */
		LIST,
		/**
		 * .properties
		 */
		PROP,
		/**
		 * Not Implemented. Future Use.
		 */
		AUTO_DETECT
	}
}
