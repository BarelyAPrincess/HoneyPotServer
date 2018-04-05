/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.permission;

import io.amelia.support.LibEncrypt;
import io.amelia.support.Strs;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Used to differentiate between each {@link Permission} reference union
 */
public class References implements Iterable<String>
{
	private static <I, R> Function<I, R> castingIdentity()
	{
		return i -> ( R ) i;
	}

	public static References format( String... refs )
	{
		return new References().add( refs );
	}

	public static ReferencesCollector collector()
	{
		return new ReferencesCollector();
	}

	protected final Set<String> refs = new TreeSet<>();

	References()
	{

	}

	/**
	 * Merges two References together
	 *
	 * @param refs The Reference to be merged with this one
	 */
	public References add( References refs )
	{
		this.refs.addAll( refs.refs );
		return this;
	}

	public References add( String... refs )
	{
		if ( refs == null || refs.length == 0 )
			add( "" );
		else
			for ( String ref : refs )
				if ( ref == null )
					add( "" );
				else if ( ref.contains( "|" ) )
					add( ref.split( "|" ) );
				else if ( ref.contains( "," ) )
					add( ref.split( "," ) );
				else
					this.refs.add( Strs.removeInvalidChars( ref.toLowerCase() ) );
		return this;
	}

	public References addAll( Collection<? extends References> refs )
	{
		this.refs.addAll( refs.stream().flatMap( r -> r.refs.stream() ).collect( Collectors.toList() ) );
		return this;
	}

	public String hash()
	{
		return LibEncrypt.md5( join() );
	}

	public boolean isEmpty()
	{
		return refs.isEmpty() || ( refs.size() == 1 && refs.contains( "" ) );
	}

	@Override
	public Iterator<String> iterator()
	{
		return refs.iterator();
	}

	public String join()
	{
		return Strs.join( refs, "," );
	}

	public boolean match( References refs )
	{
		// Null means all
		if ( refs == null )
			return true;
		for ( String ref : refs.refs )
			if ( this.refs.contains( ref ) )
				return true;
		// If we failed to find any of the specified references then we try for the default empty one
		return this.refs.contains( "" );
	}

	public References remove( References refs )
	{
		this.refs.removeAll( refs.refs );
		return this;
	}

	public References remove( String... refs )
	{
		for ( String ref : refs )
			this.refs.remove( ref.toLowerCase() );
		return this;
	}

	@Override
	public String toString()
	{
		return "References{" + join() + "}";
	}

	private static class ReferencesCollector implements Collector<References, References, References>
	{
		@Override
		public Supplier<References> supplier()
		{
			return References::new;
		}

		@Override
		public BiConsumer<References, References> accumulator()
		{
			return References::add;
		}

		@Override
		public BinaryOperator<References> combiner()
		{
			return ( left, right ) ->
			{
				left.add( right );
				return left;
			};
		}

		@Override
		public Function<References, References> finisher()
		{
			return castingIdentity();
		}

		@Override
		public Set<Collector.Characteristics> characteristics()
		{
			return Collections.unmodifiableSet( EnumSet.of( Characteristics.IDENTITY_FINISH ) );
		}
	}
}
