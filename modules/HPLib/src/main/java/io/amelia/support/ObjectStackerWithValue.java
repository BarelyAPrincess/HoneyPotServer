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

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings( "unchecked" )
public abstract class ObjectStackerWithValue<B extends ObjectStackerWithValue<B, T>, T> extends ObjectStackerBase<B>
{
	protected T value;

	protected ObjectStackerWithValue( String key )
	{
		this( null, key, null );
	}

	protected ObjectStackerWithValue( B parent, String key )
	{
		this( parent, key, null );
	}

	protected ObjectStackerWithValue( B parent, String key, T value )
	{
		super( parent, key );
		this.value = value;
	}

	public void clear()
	{
		super.clear();
		value = null;
	}

	public void delete()
	{
		super.delete();
		value = null;
	}

	public final Stream<T> flatValues()
	{
		disposeCheck();
		Stream<T> stream = children.stream().flatMap( ObjectStackerWithValue::flatValues );
		return Optional.ofNullable( value ).map( t -> Stream.concat( Stream.of( t ), stream ) ).orElse( stream );
	}

	public final Optional<T> getValue( String nodes )
	{
		return getValue( nodes, Optional.empty() );
	}

	public final Optional<T> getValue( String nodes, Optional<T> def )
	{
		disposeCheck();
		ObjectStackerWithValue<B, T> child = getChild( nodes );
		return child == null ? def : child.getValue();
	}

	public final Optional<T> getValue()
	{
		disposeCheck();
		return Optional.ofNullable( value );
	}

	public final void setValue( T value )
	{
		disposeCheck();
		if ( hasFlag( Flag.READ_ONLY ) )
			throwExceptionIgnorable( getNamespace() + " is READ_ONLY" );
		this.value = value;
	}

	private final boolean hasValue()
	{
		return value != null;
	}

	public final void setValue( Namespace ns, T value )
	{
		disposeCheck();
		ObjectStackerWithValue<B, T> child = getChild( ns, true );
		if ( child.hasFlag( Flag.NO_OVERRIDE ) && child.hasValue() )
			throwExceptionIgnorable( getNamespace() + " has NO_OVERRIDE flag" );
		child.setValue( value );
	}

	public final void setValue( String nodes, T value )
	{
		setValue( Namespace.parseString( nodes ), value );
	}

	public final Map<String, T> values()
	{
		disposeCheck();
		return children.stream().filter( ObjectStackerWithValue::hasValue ).collect( Collectors.toMap( ObjectStackerWithValue::key, c -> c.getValue().get() ) );
	}
}
