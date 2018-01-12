package io.amelia.support;

import com.sun.istack.internal.NotNull;

import java.util.Objects;

/**
 * Represents a function that accepts one argument and produces a result.
 * Will throw {@link NullPointerException} if either the supplied argument or returned result are null.
 * <p>
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #apply(Object)}.
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 * @since 1.8
 */
@FunctionalInterface
public interface NotNullFunction<T, R>
{
	/**
	 * Returns a function that always returns its input argument.
	 *
	 * @param <T> the type of the input and output objects to the function
	 * @return a function that always returns its input argument
	 */
	static <T> NotNullFunction<T, T> identity()
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
	 * @see #compose(NotNullFunction)
	 */
	default <V> NotNullFunction<T, V> andThen( NotNullFunction<? super R, ? extends V> after )
	{
		Objects.requireNonNull( after );
		return ( T t ) -> after.apply( apply( t ) );
	}

	@NotNull
	default R apply( @NotNull T t )
	{
		return Objs.notNull( apply0( Objs.notNull( t ) ) );
	}

	/**
	 * Applies this function to the given argument.
	 *
	 * @param t the function argument
	 * @return the function result
	 */
	@NotNull
	R apply0( @NotNull T t );

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
	 * @see #andThen(NotNullFunction)
	 */
	default <V> NotNullFunction<V, R> compose( NotNullFunction<? super V, ? extends T> before )
	{
		Objects.requireNonNull( before );
		return ( V v ) -> apply( before.apply( v ) );
	}
}
