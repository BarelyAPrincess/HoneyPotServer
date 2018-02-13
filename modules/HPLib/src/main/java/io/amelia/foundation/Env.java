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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Stream;

import io.amelia.lang.UncaughtException;
import io.amelia.support.IO;
import io.amelia.support.Objs;
import io.amelia.support.OptionalBoolean;
import io.amelia.support.Pair;

public class Env
{
	private final Map<String, Object> env = new HashMap<>();
	private final File envFile;

	public Env( File envFile ) throws IOException
	{
		Objs.notNull( envFile );
		this.envFile = envFile;

		synchronized ( env )
		{
			Properties prop = new Properties();
			if ( envFile.exists() )
				prop.load( new FileInputStream( envFile ) );

			for ( String key : prop.stringPropertyNames() )
				env.put( key, prop.getProperty( key ) );
		}
	}

	public <T> T computeValue( String key, Supplier<T> valueSupplier, boolean updateEnvFile )
	{
		T value = computeValue( key, valueSupplier );

		if ( updateEnvFile )
			updateEnvFile( key, value );

		return value;
	}

	public <T> T computeValue( String key, Supplier<T> valueSupplier )
	{
		return ( T ) env.computeIfAbsent( key, k -> valueSupplier.get() );
	}

	public OptionalBoolean getBoolean( String key )
	{
		return Objs.isTrue( getObject( key ) );
	}

	public Optional<Object> getObject( String key )
	{
		return Optional.ofNullable( env.get( key ) );
	}

	public Stream<Object> getStream()
	{
		return env.values().stream();
	}

	public Stream<Pair<String, Object>> getStreamMap()
	{
		return env.entrySet().stream().map( e -> new Pair<>( e.getKey(), e.getValue() ) );
	}

	public Optional<String> getString( String key )
	{
		return getObject( key ).map( Objs::castToStringWithException );
	}

	public Stream<String> getStrings()
	{
		return env.values().stream().filter( v -> v instanceof String ).map( v -> ( String ) v );
	}

	public Stream<Pair<String, String>> getStringsMap()
	{
		return env.entrySet().stream().filter( e -> e.getValue() instanceof String ).map( e -> new Pair<>( e.getKey(), ( String ) e.getValue() ) );
	}

	public boolean hasKey( String key )
	{
		return env.containsKey( key );
	}

	public boolean isValueSet( String key )
	{
		return env.containsKey( key ) && !Objs.isNull( env.get( key ) );
	}

	public Map<String, Object> map()
	{
		return Collections.unmodifiableMap( env );
	}

	public Env set( String key, Object value, boolean updateEnvFile )
	{
		Objs.notNull( key );
		Objs.notNull( value );
		env.put( key, value );

		if ( updateEnvFile )
			updateEnvFile( key, value );

		return this;
	}

	private void updateEnvFile( String key, Object value )
	{
		try
		{
			Properties prop = new Properties();
			if ( envFile.exists() )
				prop.load( new FileInputStream( envFile ) );
			prop.setProperty( key, Objs.castToString( value ) );
			prop.store( new FileOutputStream( envFile ), "" );
		}
		catch ( IOException e )
		{
			if ( e instanceof FileNotFoundException && e.getMessage().contains( "Permission denied" ) )
				throw new UncaughtException( "We attempted to save the .env file and ran into a permissions issue for directory \"" + IO.relPath( envFile.getParentFile(), new File( "" ) ) + "\"", e );
			else
				throw new UncaughtException( e );
		}
	}
}
