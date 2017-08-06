package io.amelia.foundation.binding;

import io.amelia.lang.ObjectStackerException;
import io.amelia.support.Lists;
import io.amelia.support.ObjectStackerBase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

@SuppressWarnings( "unchecked" )
public final class BindingBase extends ObjectStackerBase<BindingBase>
{
	private final Map<ObjectListenerEvent, List<BiConsumer<BindingBase, List<Object>>>> objectListeners = new ConcurrentHashMap<>();

	private final Set<Object> objects = new HashSet<>();

	protected BindingBase( String key )
	{
		super( key );
	}

	protected BindingBase( BindingBase parent, String key )
	{
		super( parent, key );
	}

	public BindingBase addObjectListener( ObjectListenerEvent event, BiConsumer<BindingBase, List<Object>> listener )
	{
		objectListeners.compute( event, ( k, v ) -> v == null ? new ArrayList<>() : v ).add( listener );
		return this;
	}

	public <T> Stream<T> compute( Class<T> aClass, Function<T, T> function )
	{
		synchronized ( objects )
		{
			return objects.stream().filter( o -> o.getClass() == aClass ).map( o ->
			{
				T t = function.apply( ( T ) o );
				if ( t != null && !objects.contains( t ) )
				{
					objects.remove( o );
					objects.add( t );
				}
				return t;
			} ).filter( Objects::nonNull );
		}
	}

	public <T> Stream<T> compute( Function<T, T> function )
	{
		synchronized ( objects )
		{
			return objects.stream().map( o ->
			{
				T t = function.apply( ( T ) o );
				if ( t != null && !objects.contains( t ) )
				{
					objects.remove( o );
					objects.add( t );
				}
				return t;
			} ).filter( Objects::nonNull );
		}
	}

	@Override
	protected BindingBase createChild( String key )
	{
		return new BindingBase( this, key );
	}

	@Override
	protected void throwExceptionError( String message ) throws BindingException.Error
	{
		throw new BindingException.Error( this, message );
	}

	@Override
	protected void throwExceptionIgnorable( String message ) throws ObjectStackerException.Ignorable
	{
		throw new BindingException.Ignorable( this, message );
	}

	public <T> Stream<T> fetch( int depth, Class<T> aClass )
	{
		Stream<T> stream = objects.stream().filter( o -> o.getClass() == aClass ).map( o -> ( T ) o );
		if ( depth > 1 )
			for ( BindingBase child : children )
				stream = Stream.concat( stream, child.fetch( depth - 1, aClass ) );
		return stream;
	}

	public <T, U> Stream<U> fetch( int depth, Function<T, U> function )
	{
		Stream<U> stream = objects.stream().map( o -> function.apply( ( T ) o ) ).filter( Objects::nonNull );
		if ( depth > 1 )
			for ( BindingBase child : children )
				stream = Stream.concat( stream, child.fetch( depth - 1, function ) );
		return stream;
	}

	public <T, U> Stream<U> fetch( Function<T, U> function )
	{
		return fetch( 1, function );
	}

	public <T> Stream<T> fetch( Class<T> aClass )
	{
		return fetch( 1, aClass );
	}

	public <T> Stream<T> fetch( int depth )
	{
		Stream<T> stream = objects.stream().map( o -> ( T ) o );
		if ( depth > 1 )
			for ( BindingBase child : children )
				stream = Stream.concat( stream, child.fetch( depth - 1 ) );
		return stream;
	}

	public <T> Stream<T> fetch()
	{
		return fetch( 1 );
	}

	public void remove( Object obj )
	{
		remove( Lists.newArrayList( obj ) );
	}

	public void remove( List<Object> objs )
	{
		signalObjectListeners( ObjectListenerEvent.REMOVE, this, objs );
		signalObjectListeners( ObjectListenerEvent.ALL, this, objs );

		synchronized ( objects )
		{
			objects.remove( objs );
		}
	}

	protected void signalObjectListeners( ObjectListenerEvent event, BindingBase ref, List<Object> objs )
	{
		if ( hasParent() )
			parent.signalObjectListeners( event, ref, objs );
		for ( BiConsumer<BindingBase, List<Object>> listener : objectListeners.compute( event, ( k, v ) -> v == null ? new ArrayList<>() : v ) )
			listener.accept( ref, objs );
	}

	public void store( Object obj )
	{
		store( Lists.newArrayList( obj ) );
	}

	public void store( List<Object> objs )
	{
		signalObjectListeners( ObjectListenerEvent.STORE, this, objs );
		signalObjectListeners( ObjectListenerEvent.ALL, this, objs );

		synchronized ( objects )
		{
			objects.addAll( objs );
		}
	}
}
