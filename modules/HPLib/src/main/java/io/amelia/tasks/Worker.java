/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.tasks;

import io.amelia.foundation.RegistrarBase;

/**
 * Represents a worker thread for the scheduler. This gives information about the Thread object for the task, owner of
 * the task and the taskId. </p> Workers are used to execute async tasks.
 */
public interface Worker
{
	/**
	 * Returns the TaskCreator that owns this task.
	 *
	 * @return The TaskCreator that owns the task
	 */
	RegistrarBase getRegistrar();

	/**
	 * Returns the taskId for the task being executed by this worker.
	 *
	 * @return Task id number
	 */
	int getTaskId();

	/**
	 * Returns the thread for the worker.
	 *
	 * @return The Thread object for the worker
	 */
	Thread getThread();
}
