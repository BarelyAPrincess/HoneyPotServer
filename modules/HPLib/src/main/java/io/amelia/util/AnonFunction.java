package io.amelia.util;

/**
 * Represents a function that accepts one argument and produces no result.
 * <p>
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #apply()}.
 */
@FunctionalInterface
public interface AnonFunction
{
	/**
	 * Applies this function to the given argument.
	 */
	void apply();
}
