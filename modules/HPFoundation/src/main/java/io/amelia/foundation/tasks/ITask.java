package io.amelia.foundation.tasks;

import io.amelia.foundation.RegistrarBase;

/**
 * Represents a task being executed by the scheduler
 */
public interface ITask
{

	/**
	 * Will attempt to cancel this task.
	 */
	void cancel();

	/**
	 * Returns the TaskCreator that owns this task.
	 *
	 * @return The TaskCreator that owns the task
	 */
	RegistrarBase getRegistrar();

	/**
	 * Returns the taskId for the task.
	 *
	 * @return Task id number
	 */
	int getTaskId();

	/**
	 * Returns true if the Task is a sync task.
	 *
	 * @return true if the task is run by main thread
	 */
	boolean isSync();
}