package io.amelia.support;

@FunctionalInterface
public interface Callback<E extends Exception>
{
	void call() throws E;
}
