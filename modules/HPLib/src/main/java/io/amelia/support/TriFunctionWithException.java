package io.amelia.support;

import java.util.Objects;

@FunctionalInterface
public interface TriFunctionWithException<T, U, W, R, E extends Exception>
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
	default <V> TriFunctionWithException<T, U, W, V, E> andThen( FunctionWithException<? super R, ? extends V, E> after ) throws E
	{
		Objects.requireNonNull( after );
		return ( T t, U u, W w ) -> after.apply( apply( t, u, w ) );
	}

	/**
	 * Applies this function to the given arguments.
	 *
	 * @param t the first function argument
	 * @param u the second function argument
	 * @param w the third function argument
	 *
	 * @return the function result
	 */
	R apply( T t, U u, W w ) throws E;
}