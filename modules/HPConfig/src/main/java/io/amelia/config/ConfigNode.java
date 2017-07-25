package io.amelia.config;

import com.sun.istack.internal.NotNull;
import io.amelia.lang.ConfigException;
import io.amelia.support.ObjectStackerWithValue;
import io.amelia.support.Objs;
import io.amelia.util.OptionalBoolean;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

public class ConfigNode extends ObjectStackerWithValue<ConfigNode, Object>
{
	protected ConfigNode()
	{
		super( "" );
	}

	protected ConfigNode( String key )
	{
		super( key );
	}

	protected ConfigNode( ConfigNode parent, String key )
	{
		super( parent, key );
	}

	protected ConfigNode( ConfigNode parent, String key, Object value )
	{
		super( parent, key, value );
	}

	@Override
	public ConfigNode createChild( String key )
	{
		return new ConfigNode( this, key );
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

	public OptionalBoolean getBoolean( String key )
	{
		return OptionalBoolean.ofNullable( Objs.castToBoolean( getValue( key ), null ) );
	}

	public OptionalDouble getDouble( String key )
	{
		return OptionalDouble.of( Objs.castToDouble( getValue( key ).orElse( -1D ) ) );
	}

	@Deprecated
	public OptionalInt getInt( String key )
	{
		return getInteger( key );
	}

	public OptionalInt getInteger( String key )
	{
		return OptionalInt.of( Objs.castToInt( getValue( key ).orElse( -1 ) ) );
	}

	public <T> List<T> getList( String key )
	{
		return null;
	}

	public OptionalLong getLong( String key )
	{
		return OptionalLong.of( Objs.castToLong( getValue( key ).orElse( -1L ) ) );
	}

	public Optional<String> getString( String key )
	{
		return Optional.ofNullable( Objs.castToString( getValue( key ).orElse( null ) ) );
	}

	public void setChild( @NotNull String key, @NotNull ConfigNode node, boolean preserve )
	{
		getChild( key, true ).setChild( node, preserve );
	}

	public void setChild( @NotNull ConfigNode node, boolean preserve )
	{
		ConfigNode config = getChild( node.key(), true );

		if ( !preserve )
			config.clear();

		config.addFlag( node.getFlags() );
		config.setValue( node.getValue() );

		node.getChildren().forEach( c -> config.setChild( c, preserve ) );
	}
}
