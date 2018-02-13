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
public interface QuadFunctionWithException<T1, T2, T3, T4, R, E extends Exception>
{
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
	 */
	default <V> QuadFunctionWithException<T1, T2, T3, T4, V, E> andThen( FunctionWithException<? super R, ? extends V, E> after ) throws E
	{
		Objects.requireNonNull( after );
		return ( T1 t1, T2 t2, T3 t3, T4 t4 ) -> after.apply( apply( t1, t2, t3, t4 ) );
	}

	/**
	 * Applies this function to the given arguments.
	 *
	 * @param t1 the first function argument
	 * @param t2 the second function argument
	 * @param t3 the third function argument
	 * @param t4 the fourth function argument
	 *
	 * @return the function result
	 */
	R apply( T1 t1, T2 t2, T3 t3, T4 t4 ) throws E;
}