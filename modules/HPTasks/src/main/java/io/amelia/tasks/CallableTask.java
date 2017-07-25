package io.amelia.tasks;

@FunctionalInterface
public interface CallableTask
{
	void call() throws Exception;
}
