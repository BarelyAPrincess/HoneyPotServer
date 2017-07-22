package io.amelia.foundation.binding;

import io.amelia.lang.ObjectStackerException;
import io.amelia.support.ObjectStackerBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

public final class BindingBase extends ObjectStackerBase<BindingBase, Void>
{
	private final List<Object> objects = new ArrayList<>();

	protected BindingBase( String key )
	{
		super( key );
	}

	protected BindingBase( BindingBase parent, String key )
	{
		super( parent, key );
		addFlag( Flag.NO_VALUE );
	}

	public <T> Stream<T> compute( Function<T, T> function )
	{
		synchronized ( objects )
		{
			return objects.stream().map( o ->
			{
				T t = function.apply( ( T ) o );
				if ( !objects.contains( t ) )
				{
					objects.remove( o );
					objects.add( t );
				}
				return t;
			} );
		}
	}

	@Override
	public ObjectStackerBase<BindingBase, Void> createChild( String key )
	{
		return new BindingBase( this, key );
	}

	@Override
	public void throwExceptionError( String message ) throws BindingException.Error
	{
		throw new BindingException.Error( this, message );
	}

	@Override
	public void throwExceptionIgnorable( String message ) throws ObjectStackerException.Ignorable
	{
		throw new BindingException.Ignorable( this, message );
	}

	public void remove( Object obj )
	{
		synchronized ( objects )
		{
			objects.remove( obj );
		}
	}

	public <T, U> Stream<U> fetch( Function<T, U> function )
	{
		return objects.stream().map( o -> function.apply( ( T ) o ) ).filter( Objects::nonNull );
	}

	public <T> Stream<T> fetch( Class<T> aClass )
	{
		return objects.stream().filter( o -> o.getClass() == aClass ).map( o -> ( T ) o );
	}

	public <T> Stream<T> fetch()
	{
		return objects.stream().map( o -> ( T ) o );
	}

	public void store( Object obj )
	{
		synchronized ( objects )
		{
			objects.add( obj );
		}
	}

	public void store( List<Object> objs )
	{
		synchronized ( objects )
		{
			objects.addAll( objs );
		}
	}
}
