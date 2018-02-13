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

@FunctionalInterface
public interface FunctionWithException<T, R, E extends Exception>
{
	/**
	 * Returns a function that always returns its input argument.
	 *
	 * @param <T> the type of the input and output objects to the function
	 * @return a function that always returns its input argument
	 */
	static <T, E extends Exception> FunctionWithException<T, T, E> identity()
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
	 * @return a composed function that first applies this function and then
	 * applies the {@code after} function
	 * @throws NullPointerException if after is null
	 * @see #compose(FunctionWithException)
	 */
	default <V> FunctionWithException<T, V, E> andThen( FunctionWithException<? super R, ? extends V, E> after )
	{
		Objects.requireNonNull( after );
		return ( T t ) -> after.apply( apply( t ) );
	}

	/**
	 * Applies this function to the given argument.
	 *
	 * @param t the function argument
	 * @return the function result
	 */
	R apply( T t );

	/**
	 * Returns a composed function that first applies the {@code before}
	 * function to its input, and then applies this function to the result.
	 * If evaluation of either function throws an exception, it is relayed to
	 * the caller of the composed function.
	 *
	 * @param <V>    the type of input to the {@code before} function, and to the
	 *               composed function
	 * @param before the function to apply before this function is applied
	 * @return a composed function that first applies the {@code before}
	 * function and then applies this function
	 * @throws NullPointerException if before is null
	 * @see #andThen(FunctionWithException)
	 */
	default <V> FunctionWithException<V, R, E> compose( FunctionWithException<? super V, ? extends T, E> before )
	{
		Objects.requireNonNull( before );
		return ( V v ) -> apply( before.apply( v ) );
	}
}