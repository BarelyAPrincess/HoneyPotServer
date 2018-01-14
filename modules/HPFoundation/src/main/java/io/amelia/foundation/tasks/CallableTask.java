package io.amelia.foundation.tasks;

@FunctionalInterface
public interface CallableTask
{
	void call() throws Exception;
}
