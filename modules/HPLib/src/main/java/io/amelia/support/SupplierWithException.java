package io.amelia.support;

@FunctionalInterface
public interface SupplierWithException<T, E extends Exception>
{
	/**
	 * Gets a result.
	 *
	 * @return a result
	 */
	T get() throws E;
}
