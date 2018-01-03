package io.amelia.looper;

@FunctionalInterface
public interface LooperTask<E extends Exception>
{
	/**
	 * Computes a result, or throws an exception if unable to do so.
	 *
	 * @throws E if unable to compute a result
	 */
	void execute() throws E;
}
