/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * Represents a function that accepts one argument and produces a result.
 * Will throw {@link NullPointerException} if either the supplied argument or returned result are null.
 * <p>
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #apply(Object)}.
 *
 * @param <InputType>  the type of the input to the function
 * @param <ResultType> the type of the result of the function
 *
 * @see java.util.function.Function
 */
@FunctionalInterface
public interface NonnullFunction<InputType, ResultType>
{
	/**
	 * Returns a function that always returns its input argument.
	 *
	 * @param <T> the type of the input and output objects to the function
	 *
	 * @return a function that always returns its input argument
	 */
	static <T> NonnullFunction<T, T> identity()
	{
		return t -> t;
	}

	/**
	 * Returns a composed function that first applies this function to
	 * its input, and then applies the {@code after} function to the result.
	 * If evaluation of either function throws an exception, it is relayed to
	 * the caller of the composed function.
	 *
	 * @param <V>   the type of output of the {@code after} function, and of the
	 *              composed function
	 * @param after the function to apply after this function is applied
	 *
	 * @return a composed function that first applies this function and then
	 * applies the {@code after} function
	 *
	 * @throws NullPointerException if after is null
	 * @see #compose(NonnullFunction)
	 */
	default <V> NonnullFunction<InputType, V> andThen( NonnullFunction<? super ResultType, ? extends V> after )
	{
		Objects.requireNonNull( after );
		return ( InputType t ) -> after.apply( apply( t ) );
	}

	@Nonnull
	default ResultType apply( @Nonnull InputType var )
	{
		return Objs.notNull( apply0( Objs.notNull( var ) ) );
	}

	/**
	 * Applies this function to the given argument.
	 *
	 * @param var the function argument
	 *
	 * @return the function result
	 */
	@Nonnull
	ResultType apply0( @Nonnull InputType var );

	/**
	 * Returns a composed function that first applies the {@code before}
	 * function to its input, and then applies this function to the result.
	 * If evaluation of either function throws an exception, it is relayed to
	 * the caller of the composed function.
	 *
	 * @param <V>    the type of input to the {@code before} function, and to the
	 *               composed function
	 * @param before the function to apply before this function is applied
	 *
	 * @return a composed function that first applies the {@code before}
	 * function and then applies this function
	 *
	 * @throws NullPointerException if before is null
	 * @see #andThen(NonnullFunction)
	 */
	default <V> NonnullFunction<V, ResultType> compose( NonnullFunction<? super V, ? extends InputType> before )
	{
		Objects.requireNonNull( before );
		return ( V v ) -> apply( before.apply( v ) );
	}
}
