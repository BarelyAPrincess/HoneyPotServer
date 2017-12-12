/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support.data;

import com.sun.istack.internal.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.amelia.foundation.Kernel;
import io.amelia.support.Objs;
import io.amelia.support.Pair;

@SuppressWarnings( "unchecked" )
public abstract class StackerWithValue<B extends StackerWithValue<B, T>, T> extends StackerBase<B>
{
	public static final int LISTENER_VALUE_CHANGE = 0xfd;
	protected T value;

	protected StackerWithValue( BiFunction<B, String, B> creator, String key )
	{
		this( creator, null, key, null );
	}

	protected StackerWithValue( BiFunction<B, String, B> creator, B parent, String key )
	{
		this( creator, parent, key, null );
	}

	protected StackerWithValue( BiFunction<B, String, B> creator, B parent, String key, T value )
	{
		super( creator, parent, key );
		this.value = value;
	}

	public final int addValueListener( StackerListener.OnValueChange<B, T> function, StackerListener.Flags... flags )
	{
		return addListener( new StackerListener.Container( LISTENER_VALUE_CHANGE, flags )
		{
			@Override
			public void call( Object[] objs )
			{
				function.listen( ( B ) objs[0], ( T ) objs[1], ( T ) objs[2] );
			}
		} );
	}

	public <O> O asObject( Class<O> cls )
	{
		try
		{
			Constructor<?> constructor = cls.getConstructor( StackerBase.class );
			return ( O ) constructor.newInstance( this );
		}
		catch ( Exception ignore )
		{
		}

		try
		{
			Constructor<?> constructor = cls.getConstructor();
			Object instance = constructor.newInstance();

			for ( Field field : cls.getFields() )
			{
				B child = findChild( field.getName(), false );

				if ( child == null )
				{
					Kernel.L.warning( "Could not assign field " + field.getName() + " with type " + field.getType() + " within class " + cls.getSimpleName() + "." );
					continue;
				}

				field.setAccessible( true );

				Object obj = child.getValue().orElse( null );
				boolean assigned = true;

				if ( obj != null )
				{
					if ( obj.getClass().isAssignableFrom( field.getType() ) )
						field.set( instance, obj );
					else if ( String.class.isAssignableFrom( field.getType() ) )
						field.set( instance, Objs.castToString( value ) );
					else if ( Double.class.isAssignableFrom( field.getType() ) )
						field.set( instance, Objs.castToDouble( value ) );
					else if ( Integer.class.isAssignableFrom( field.getType() ) )
						field.set( instance, Objs.castToInt( value ) );
					else if ( Long.class.isAssignableFrom( field.getType() ) )
						field.set( instance, Objs.castToLong( value ) );
					else if ( Boolean.class.isAssignableFrom( field.getType() ) )
						field.set( instance, Objs.castToBoolean( value ) );
					else
						assigned = false;
				}

				if ( field.get( instance ) == null )
					assigned = false;

				if ( !assigned )
				{
					Object o = child.asObject( field.getType() );
					if ( o == null )
						Kernel.L.severe( "Could not cast field " + field.getName() + " with type " + field.getType() + " with value " + value.getClass().getSimpleName() + " within class" + cls.getSimpleName() + "." );
					else
						field.set( instance, o );
				}
			}

			//for ( Field field : cls.getFields() )
			//	if ( field.get( instance ) == null && !field.isSynthetic() )
			//		Kernel.L.warning( "The field " + field.getProductName() + " is unassigned for object " + cls );

			return ( O ) instance;
		}
		catch ( Exception ignore )
		{
		}

		return null;
	}

	public void destroy()
	{
		super.destroy();
		value = null;
	}

	public Stream<T> flatValues()
	{
		disposeCheck();
		Stream<T> stream = children.stream().flatMap( StackerWithValue::flatValues );
		return Optional.ofNullable( value ).map( t -> Stream.concat( Stream.of( t ), stream ) ).orElse( stream );
	}

	public <LT extends T> List<LT> getChildAsList( String key, Class<LT> type )
	{
		B child = getChild( key );
		if ( child == null )
			return null;
		return child.children.stream().map( c -> Objs.castTo( c.value, type ) ).filter( Objects::nonNull ).collect( Collectors.toList() );
	}

	public <LT extends T> List<LT> getChildAsList( String key )
	{
		B child = getChild( key );
		if ( child == null )
			return null;
		return child.children.stream().map( c -> ( LT ) c.value ).filter( Objects::nonNull ).collect( Collectors.toList() );
	}

	public <T> T getChildAsObject( @NotNull String key, Class<T> cls )
	{
		B child = findChild( key, false );
		if ( child == null )
			return null;
		return child.asObject( cls );
	}

	public <MT extends T> Map<String, MT> getChildrenAsMap()
	{
		return children.stream().collect( Collectors.toMap( StackerBase::getName, c -> ( MT ) c.value ) );
	}

	public <MT extends T> Map<String, MT> getChildrenAsMap( String key )
	{
		B child = getChild( key );
		if ( child == null )
			return null;
		return child.getChildrenAsMap();
	}

	public <MT extends T> Stream<Pair<String, MT>> getChildrenWithKeys()
	{
		return children.stream().map( c -> new Pair( c.getName(), c.value ) );
	}

	public T getValue( String key, Function<T, T> computeFunction )
	{
		B child = findChild( key, true );
		T val = computeFunction.apply( child.value );
		if ( val != child.value )
			child.setValue( val );
		return val;
	}

	public T getValue( String key, Supplier<T> supplier )
	{
		B child = findChild( key, true );
		if ( child.value == null )
			child.setValue( supplier.get() );
		return child.value;
	}

	public Optional<T> getValue( String key )
	{
		B child = findChild( key, false );
		return Optional.ofNullable( child == null ? null : child.value );
	}

	public Optional<T> getValue()
	{
		disposeCheck();
		return Optional.ofNullable( value );
	}

	public void setValue( T value )
	{
		disposeCheck();
		notFlag( Flag.READ_ONLY );
		if ( hasFlag( Flag.NO_OVERRIDE ) && hasValue() )
			throwExceptionIgnorable( getCurrentPath() + " has NO_OVERRIDE flag" );
		fireListener( LISTENER_VALUE_CHANGE, this.value, value );
		this.value = value;
	}

	public void getValueIfPresent( String key, Consumer<T> consumer )
	{
		B child = findChild( key, false );
		if ( child != null && child.value != null )
			consumer.accept( child.value );
	}

	public boolean hasValue()
	{
		return value != null;
	}

	@Override
	public void mergeChild( @NotNull B child )
	{
		super.mergeChild( child );
		value = child.value;
	}

	public Optional<T> pollValue( String key )
	{
		B child = findChild( key, false );
		if ( child == null )
			return Optional.empty();
		else
		{
			T value = child.value;
			child.value = null;
			return Optional.ofNullable( value );
		}
	}

	public void setValue( String key, T value )
	{
		getChildOrCreate( key ).setValue( value );
	}

	protected void setValueIgnoreFlags( String key, T value )
	{
		getChildOrCreate( key ).setValueIgnoreFlags( value );
	}

	protected void setValueIgnoreFlags( T value )
	{
		disposeCheck();
		fireListener( LISTENER_VALUE_CHANGE, this.value, value );
		this.value = value;
	}

	public Map<String, T> values()
	{
		disposeCheck();
		return children.stream().filter( StackerWithValue::hasValue ).collect( Collectors.toMap( StackerWithValue::getName, c -> c.getValue().get() ) );
	}
}
