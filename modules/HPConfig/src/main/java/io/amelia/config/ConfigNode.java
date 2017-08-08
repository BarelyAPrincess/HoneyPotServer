package io.amelia.config;

import com.sun.istack.internal.NotNull;
import io.amelia.lang.ConfigException;
import io.amelia.support.Objs;
import io.amelia.support.StackerListener;
import io.amelia.support.StackerWithValue;
import io.amelia.support.Strs;
import io.amelia.util.OptionalBoolean;

import java.awt.Color;
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

	public OptionalBoolean getBoolean( String key )
	{
		return OptionalBoolean.ofNullable( getValue( key ).map( Objs::castToBoolean ).orElse( null ) );
	}

	public Optional<Color> getColor( String key )
	{
		return getValue().filter( v -> v instanceof Color ).map( v -> ( Color ) v );
	}

	public OptionalDouble getDouble( String key )
	{
		return OptionalDouble.of( getValue( key ).map( Objs::castToDouble ).orElse( -1D ) );
	}

	public <T extends Enum<T>> T getEnum( String enumKey, Class<T> enumClass )
	{
		return Objs.onPresent( getString( enumKey ), s -> Enum.valueOf( enumClass, s ) );
	}

	@Deprecated
	public OptionalInt getInt( String key )
	{
		return getInteger( key );
	}

	public OptionalInt getInteger( String key )
	{
		return OptionalInt.of( getValue( key ).map( Objs::castToInt ).orElse( -1 ) );
	}

	public <T> List<T> getList( String key )
	{
		return getValue( key ).filter( v -> v instanceof List ).map( v -> ( List<T> ) v ).orElse( null );
	}

	public OptionalLong getLong( String key )
	{
		return OptionalLong.of( getValue( key ).map( Objs::castToLong ).orElse( -1L ) );
	}

	public Optional<String> getString( String key )
	{
		return Optional.ofNullable( getValue( key ).map( Objs::castToString ).orElse( null ) );
	}

	public <T> Class<T> getStringAsClass( @NotNull String key )
	{
		return getStringAsClass( key, null );
	}

	@SuppressWarnings( "unchecked" )
	public <T> Class<T> getStringAsClass( @NotNull String key, Class<T> expectedClass )
	{
		Optional<String> sClass = getString( key );
		if ( !sClass.isPresent() )
			return null;
		Class<T> aClass = Objs.getClassByName( sClass.orElse( null ) );
		return expectedClass == null ? aClass : aClass == null || !expectedClass.isAssignableFrom( aClass ) ? null : aClass;
	}

	public List<String> getStringAsList( String key, String delimiter )
	{
		return getString( key ).map( s -> Strs.split( s, delimiter ).collect( Collectors.toList() ) ).orElse( null );
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
