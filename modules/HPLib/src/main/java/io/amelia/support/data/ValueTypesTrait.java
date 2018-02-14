/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support.data;

import java.awt.Color;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import io.amelia.support.IO;
import io.amelia.support.Objs;
import io.amelia.support.OptionalBoolean;
import io.amelia.support.OptionalLongExt;
import io.amelia.support.Strs;

/**
 * Provides common methods for converting an unknown value to (and from) {@link Object} using the Java 8 {@link Optional} feature.
 * <p>
 * These types include:
 * Boolean
 * Color
 * Double
 * Enum
 * Integer
 * Long
 * String
 * File
 * List
 * Class
 */
public interface ValueTypesTrait
{
	default OptionalBoolean getBoolean( String key )
	{
		return OptionalBoolean.ofNullable( getValue( key ).map( Objs::castToBoolean ).orElse( null ) );
	}

	default Optional<Color> getColor()
	{
		return getColor( "" );
	}

	default Optional<Color> getColor( String key )
	{
		return getValue( key ).filter( v -> v instanceof Color ).map( v -> ( Color ) v );
	}

	default OptionalDouble getDouble()
	{
		return getDouble( "" );
	}

	default OptionalDouble getDouble( String key )
	{
		return OptionalDouble.of( getValue( key ).map( Objs::castToDouble ).orElse( -1D ) );
	}

	default <T extends Enum<T>> Optional<T> getEnum( Class<T> enumClass )
	{
		return getEnum( "", enumClass );
	}

	default <T extends Enum<T>> Optional<T> getEnum( String key, Class<T> enumClass )
	{
		return getString( key ).map( e -> Enum.valueOf( enumClass, e ) );
	}

	default OptionalInt getInteger()
	{
		return getInteger( "" );
	}

	default OptionalInt getInteger( String key )
	{
		return OptionalInt.of( getValue( key ).map( Objs::castToInt ).orElse( -1 ) );
	}

	default <T> Optional<List<T>> getList()
	{
		return getList( "" );
	}

	default <T> Optional<List<T>> getList( @Nonnull String key )
	{
		return getValue( key ).filter( v -> v instanceof List ).map( v -> ( List<T> ) v );
	}

	default <T> Optional<List<T>> getList( @Nonnull Class<T> expectedObjectClass )
	{
		return getList( "", expectedObjectClass );
	}

	default <T> Optional<List<T>> getList( @Nonnull String key, @Nonnull Class<T> expectedObjectClass )
	{
		return getValue( key ).filter( v -> v instanceof List ).map( v -> Objs.castList( ( List<?> ) v, expectedObjectClass ) );
	}

	default OptionalLongExt getLong()
	{
		return getLong( "" );
	}

	default OptionalLongExt getLong( @Nonnull String key )
	{
		return OptionalLongExt.of( getValue( key ).map( Objs::castToLong ).orElse( -1L ) );
	}

	default Optional<String> getString()
	{
		return getString( "" );
	}

	default Optional<String> getString( @Nonnull String key )
	{
		return Optional.ofNullable( getValue( key ).map( Objs::castToString ).orElse( null ) );
	}

	default <T> Optional<Class<T>> getStringAsClass()
	{
		return getStringAsClass( "" );
	}

	default <T> Optional<Class<T>> getStringAsClass( @Nonnull String key )
	{
		return getStringAsClass( key, null );
	}

	@SuppressWarnings( "unchecked" )
	default <T> Optional<Class<T>> getStringAsClass( @Nonnull String key, @Nonnull Class<T> expectedClass )
	{
		return getString( key ).map( str -> ( Class<T> ) Objs.getClassByName( str ) ).filter( cls -> expectedClass == null || expectedClass.isAssignableFrom( cls ) );
	}

	default Optional<File> getStringAsFile( File rel )
	{
		return getStringAsFile( "", rel );
	}

	default Optional<File> getStringAsFile( String key, File rel )
	{
		return getString( key ).map( s -> IO.buildFile( rel, s ) );
	}

	default Optional<File> getStringAsFile( String key )
	{
		return getString( key ).map( IO::buildFile );
	}

	default Optional<File> getStringAsFile()
	{
		return getStringAsFile( "" );
	}

	default Optional<List<String>> getStringAsList()
	{
		return getStringAsList( "", "|" );
	}

	default Optional<List<String>> getStringAsList( String key )
	{
		return getStringAsList( key, "|" );
	}

	default Optional<List<String>> getStringAsList( String key, String delimiter )
	{
		return getString( key ).map( s -> Strs.split( s, delimiter ).collect( Collectors.toList() ) );
	}

	Optional<?> getValue( String key );

	Optional<?> getValue();

	default boolean isColor()
	{
		return isColor( "" );
	}

	default boolean isColor( String key )
	{
		return getValue( key ).map( v -> v instanceof Color ).orElse( false );
	}

	default boolean isEmpty()
	{
		return isEmpty( "" );
	}

	default boolean isEmpty( String key )
	{
		return getValue( key ).map( Objs::isEmpty ).orElse( true );
	}

	default boolean isList( String key )
	{
		return getValue( key ).map( o -> o instanceof List ).orElse( false );
	}

	default boolean isNull()
	{
		return isNull( "" );
	}

	default boolean isNull( String key )
	{
		return getValue( key ).map( Objs::isNull ).orElse( true );
	}

	default boolean isSet()
	{
		return !isNull();
	}

	default boolean isSet( String key )
	{
		return !isNull( key );
	}

	default boolean isTrue()
	{
		return isTrue( "" );
	}

	default boolean isTrue( String key )
	{
		return getValue( key ).map( Objs::isTrue ).orElse( false );
	}

	default boolean isType( @Nonnull String key, @Nonnull Class<?> type )
	{
		Optional<?> result = getValue( key );
		return result.isPresent() && type.isAssignableFrom( result.get().getClass() );
	}
}
