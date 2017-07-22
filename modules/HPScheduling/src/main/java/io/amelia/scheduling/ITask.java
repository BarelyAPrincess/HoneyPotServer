/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.scheduling;


import io.amelia.foundation.Registrar;

/**
 * Represents a task being executed by the scheduler
 */

public interface ITask
{
	
	/**
	 * Returns the taskId for the task.
	 * 
	 * @return Task id number
	 */
	int getTaskId();
	
	/**
	 * Returns the TaskCreator that owns this task.
	 * 
	 * @return The TaskCreator that owns the task
	 */
	Registrar getOwner();
	
	/**
	 * Returns true if the Task is a sync task.
	 * 
	 * @return true if the task is run by main thread
	 */
	boolean isSync();
	
	/**
	 * Will attempt to cancel this task.
	 */
	void cancel();
}
