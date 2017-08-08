/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

import com.sun.istack.internal.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

	public void clear()
	{
		super.clear();
		value = null;
	}

	public void destroy()
	{
		super.destroy();
		value = null;
	}

	@Override
	public void setChild( @NotNull B discardedChild )
	{
		super.setChild( discardedChild );
		notFlag( Flag.NO_OVERRIDE );
		value = discardedChild.value;
	}

	public final Stream<T> flatValues()
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

	public <MT extends T> Map<String, MT> getChildrenAsMap( String key )
	{
		B child = getChild( key );
		if ( child == null )
			return null;
		return child.children.stream().collect( Collectors.toMap( StackerBase::getName, c -> ( MT ) c.value ) );
	}

	public final Optional<T> getValue( String key )
	{
		B child = findChild( key, false );
		return Optional.ofNullable( child == null ? null : child.value );
	}

	public final Optional<T> getValue()
	{
		disposeCheck();
		return Optional.ofNullable( value );
	}

	public final void setValue( T value )
	{
		disposeCheck();
		notFlag( Flag.READ_ONLY );
		if ( hasFlag( Flag.NO_OVERRIDE ) && hasValue() )
			throwExceptionIgnorable( getCurrentPath() + " has NO_OVERRIDE flag" );
		fireListener( LISTENER_VALUE_CHANGE, this.value, value );
		this.value = value;
	}

	public final boolean hasValue()
	{
		return value != null;
	}

	public final void setValue( String key, T value )
	{
		disposeCheck();
		getChildOrCreate( key ).setValue( value );
	}

	public final Map<String, T> values()
	{
		disposeCheck();
		return children.stream().filter( StackerWithValue::hasValue ).collect( Collectors.toMap( StackerWithValue::getName, c -> c.getValue().get() ) );
	}
}
