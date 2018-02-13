/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.looper;

import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.support.Objs;

public final class LooperFactory<L extends AbstractLooper>
{
	private final Supplier<AbstractLooper> supplier;
	private volatile NavigableSet<AbstractLooper> loopers = new TreeSet<>();

	public LooperFactory( @Nonnull Supplier<AbstractLooper> supplier )
	{
		this.supplier = supplier;
	}

	public boolean hasLooper( L looper )
	{
		return loopers.contains( looper );
	}

	/**
	 * Destroys the Looper associated with the calling Thread.
	 *
	 * @return Was a Looper found and successfully destroyed.
	 */
	public boolean destroy()
	{
		AbstractLooper looper = peek();
		if ( looper != null )
		{
			looper.quitUnsafe();
			return true;
		}
		else
			return false;
	}

	/**
	 * Returns the Looper associated with the Thread calling this method and that has passed the predicate provided.
	 *
	 * @param supplier  The Supplier used for initiating a new instance of Looper if none was found or it fails the Predicate.
	 * @param predicate The predicate used to evaluate the associated Looper.
	 *
	 * @return The associated Looper that also passed the provided Predicate, otherwise a new instance returned by the Supplier if none was found or it failed the predicate.
	 */
	AbstractLooper obtain( @Nonnull Supplier<AbstractLooper> supplier, @Nullable Predicate<AbstractLooper> predicate )
	{
		Objs.notNull( supplier );

		AbstractLooper looper = peek();
		if ( looper == null )
		{
			looper = supplier.get();
			loopers.add( looper );
		}
		else if ( predicate != null && !predicate.test( looper ) )
		{
			// Looper failed the predicate, so it needs to be replaced.
			loopers.remove( looper );
			looper = supplier.get();
			loopers.add( looper );
		}
		return looper;
	}

	/**
	 * Returns the Looper associated with the Thread calling this method and that has passed the predicate provided.
	 *
	 * @param predicate The predicate used to evaluate the associated Looper.
	 *
	 * @return The associated Looper that also passed the provided Predicate, otherwise new if none was found or it failed the predicate.
	 */
	AbstractLooper obtain( @Nullable Predicate<AbstractLooper> predicate )
	{
		return obtain( supplier, predicate );
	}

	/**
	 * Returns the Looper associated with the Thread calling this method.
	 *
	 * @return The associated Looper, otherwise a new Looper if none was found.
	 */
	public AbstractLooper obtain()
	{
		return obtain( null );
	}

	/**
	 * Filter all current loopers using the predicate provided.
	 *
	 * @param predicate The predicate used to evaluate each Looper. Looper may or may not be in use elsewhere.
	 *
	 * @return Returns a stream of loopers that matched the predicate provided.
	 */
	Stream<AbstractLooper> peek( @Nonnull Predicate<AbstractLooper> predicate )
	{
		return loopers.stream().filter( predicate );
	}

	/**
	 * Returns the Looper associated with the Thread calling this method.
	 *
	 * @return The associated Looper, otherwise {@code null} if none was found.
	 */
	AbstractLooper peek()
	{
		return peek( AbstractLooper::isHeldByCurrentThread ).findFirst().orElse( null );
	}

	void remove( L looper )
	{
		loopers.remove( looper );
	}
}
