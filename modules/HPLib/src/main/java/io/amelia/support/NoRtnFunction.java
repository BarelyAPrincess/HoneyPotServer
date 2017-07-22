package io.amelia.support;

/**
 * Represents a function that accepts one argument and produces no result.
 * <p>
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #apply(Object)}.
 *
 * @param <T> the type of the input to the function
 */
@FunctionalInterface
public interface NoRtnFunction<T>
{
	/**
	 * Applies this function to the given argument.
	 *
	 * @param t the function argument
	 */
	void apply( T t );
}
