package io.amelia.config;

import com.sun.istack.internal.NotNull;
import io.amelia.lang.ConfigException;
import io.amelia.support.LibIO;
import io.amelia.support.Objs;
import io.amelia.support.StackerWithValue;
import io.amelia.support.Strs;
import io.amelia.util.OptionalBoolean;

import java.awt.Color;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.stream.Collectors;

public final class ConfigNode extends StackerWithValue<ConfigNode, Object>
{
	protected ConfigNode()
	{
		super( ConfigNode::new, "" );
	}

	protected ConfigNode( String key )
	{
		super( ConfigNode::new, key );
	}

	protected ConfigNode( ConfigNode parent, String key )
	{
		super( ConfigNode::new, parent, key );
	}

	protected ConfigNode( ConfigNode parent, String key, Object value )
	{
		super( ConfigNode::new, parent, key, value );
	}

	public OptionalBoolean getBoolean()
	{
		return getBoolean( "" );
	}

	public OptionalBoolean getBoolean( String key )
	{
		return OptionalBoolean.ofNullable( getValue( key ).map( Objs::castToBoolean ).orElse( null ) );
	}

	public Optional<Color> getColor()
	{
		return getColor( "" );
	}

	public Optional<Color> getColor( String key )
	{
		return getValue( key ).filter( v -> v instanceof Color ).map( v -> ( Color ) v );
	}

	public OptionalDouble getDouble()
	{
		return getDouble( "" );
	}

	public OptionalDouble getDouble( String key )
	{
		return OptionalDouble.of( getValue( key ).map( Objs::castToDouble ).orElse( -1D ) );
	}

	public <T extends Enum<T>> Optional<T> getEnum( Class<T> enumClass )
	{
		return getEnum( "", enumClass );
	}

	public <T extends Enum<T>> Optional<T> getEnum( String key, Class<T> enumClass )
	{
		return getString( key ).map( e -> Enum.valueOf( enumClass, e ) );
	}

	public OptionalInt getInteger()
	{
		return getInteger( "" );
	}

	public OptionalInt getInteger( String key )
	{
		return OptionalInt.of( getValue( key ).map( Objs::castToInt ).orElse( -1 ) );
	}

	public <T> Optional<List<T>> getList()
	{
		return getList( "" );
	}

	public <T> Optional<List<T>> getList( String key )
	{
		return getValue( key ).filter( v -> v instanceof List ).map( v -> ( List<T> ) v );
	}

	public OptionalLong getLong()
	{
		return getLong( "" );
	}

	public OptionalLong getLong( String key )
	{
		return OptionalLong.of( getValue( key ).map( Objs::castToLong ).orElse( -1L ) );
	}

	public Optional<String> getString()
	{
		return getString( "" );
	}

	public Optional<String> getString( String key )
	{
		return Optional.ofNullable( getValue( key ).map( Objs::castToString ).orElse( null ) );
	}

	public <T> Optional<Class<T>> getStringAsClass()
	{
		return getStringAsClass( "" );
	}

	public <T> Optional<Class<T>> getStringAsClass( @NotNull String key )
	{
		return getStringAsClass( key, null );
	}

	@SuppressWarnings( "unchecked" )
	public <T> Optional<Class<T>> getStringAsClass( @NotNull String key, Class<T> expectedClass )
	{
		return getString( key ).map( str -> ( Class<T> ) Objs.getClassByName( str ) ).filter( cls -> expectedClass == null || expectedClass.isAssignableFrom( cls ) );
	}

	public Optional<File> getStringAsFile( File rel )
	{
		return getStringAsFile( "", rel );
	}

	public Optional<File> getStringAsFile( String key, File rel )
	{
		return getString( key ).map( s -> LibIO.buildFile( rel, s ) );
	}

	public Optional<File> getStringAsFile( String key )
	{
		return getString( key ).map( LibIO::buildFile );
	}

	public Optional<File> getStringAsFile()
	{
		return getStringAsFile( "" );
	}

	public Optional<List<String>> getStringAsList()
	{
		return getStringAsList( "", "|" );
	}

	public Optional<List<String>> getStringAsList( String key )
	{
		return getStringAsList( key, "|" );
	}

	public Optional<List<String>> getStringAsList( String key, String delimiter )
	{
		return getString( key ).map( s -> Strs.split( s, delimiter ).collect( Collectors.toList() ) );
	}

	public boolean isColor()
	{
		return isColor( "" );
	}

	public boolean isColor( String key )
	{
		return getValue( key ).map( v -> v instanceof Color ).orElse( false );
	}

	public boolean isEmpty()
	{
		return isEmpty( "" );
	}

	public boolean isEmpty( String key )
	{
		return getValue( key ).map( Objs::isEmpty ).orElse( true );
	}

	public boolean isList( String key )
	{
		return getValue( key ).map( o -> o instanceof List ).orElse( false );
	}

	public boolean isNull()
	{
		return isNull( "" );
	}

	public boolean isNull( String key )
	{
		return getValue( key ).map( Objs::isNull ).orElse( true );
	}

	public boolean isSet()
	{
		return !isNull();
	}

	public boolean isSet( String key )
	{
		return !isNull( key );
	}

	public boolean isTrue()
	{
		return isTrue( "" );
	}

	public boolean isTrue( String key )
	{
		return getValue( key ).map( Objs::isTrue ).orElse( false );
	}

	@Override
	public void throwExceptionError( String message ) throws ConfigException.Error
	{
		throw new ConfigException.Error( this, message );
	}

	@Override
	public void throwExceptionIgnorable( String message ) throws ConfigException.Ignorable
	{
		throw new ConfigException.Ignorable( this, message );
	}
}
