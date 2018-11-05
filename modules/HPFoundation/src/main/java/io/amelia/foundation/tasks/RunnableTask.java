/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation.tasks;

import io.amelia.foundation.RegistrarBase;

/**
 * This class is provided as an easy way to handle scheduling tasks.
 */
public abstract class RunnableTask implements CallableTask
{
	private int taskId = -1;

	/**
	 * Attempts to cancel this task.
	 *
	 * @throws IllegalStateException if task was not scheduled yet
	 */
	public synchronized void cancel() throws IllegalStateException
	{
		Tasks.cancelTask( getTaskId() );
	}

	private void checkState()
	{
		if ( taskId != -1 )
			throw new IllegalStateException( "Already scheduled as " + taskId );
	}

	/**
	 * Gets the task id for this runnable.
	 *
	 * @return the task id that this runnable was scheduled as
	 *
	 * @throws IllegalStateException if task was not scheduled yet
	 */
	public synchronized int getTaskId() throws IllegalStateException
	{
		final int id = taskId;
		if ( id == -1 )
			throw new IllegalStateException( "Not scheduled yet" );
		return id;
	}

	/**
	 * Schedules this in the Chiori scheduler to run on next tick.
	 *
	 * @param creator the reference to the plugin scheduling task
	 *
	 * @return a ChioriTask that contains the id number
	 *
	 * @throws IllegalArgumentException if plugin is null
	 * @throws IllegalStateException    if this was already scheduled
	 * @see Tasks#runTask(RegistrarBase, CallableTask)
	 */
	public synchronized Task runTask( RegistrarBase creator ) throws IllegalArgumentException, IllegalStateException
	{
		checkState();
		return setupId( Tasks.runTask( creator, this ) );
	}

	/**
	 * <b>Asynchronous tasks should never access any API in Main. Great care should be taken to assure the thread-safety
	 * of asynchronous tasks.</b> <br>
	 * <br>
	 * Schedules this in the Chiori scheduler to run asynchronously.
	 *
	 * @param creator the reference to the plugin scheduling task
	 *
	 * @return a ChioriTask that contains the id number
	 *
	 * @throws IllegalArgumentException if plugin is null
	 * @throws IllegalStateException    if this was already scheduled
	 * @see Tasks#runTaskAsynchronously(RegistrarBase, CallableTask)
	 */
	public synchronized Task runTaskAsynchronously( RegistrarBase creator ) throws IllegalArgumentException, IllegalStateException
	{
		checkState();
		return setupId( Tasks.runTaskAsynchronously( creator, this ) );
	}

	/**
	 * Schedules this to run after the specified number of server ticks.
	 *
	 * @param creator the reference to the plugin scheduling task
	 * @param delay   the ticks to wait before running the task
	 *
	 * @return a ChioriTask that contains the id number
	 *
	 * @throws IllegalArgumentException if plugin is null
	 * @throws IllegalStateException    if this was already scheduled
	 * @see Tasks#runTaskLater(RegistrarBase, long, CallableTask)
	 */
	public synchronized Task runTaskLater( RegistrarBase creator, long delay ) throws IllegalArgumentException, IllegalStateException
	{
		checkState();
		return setupId( Tasks.runTaskLater( creator, delay, this ) );
	}

	/**
	 * <b>Asynchronous tasks should never access any API in Main. Great care should be taken to assure the thread-safety
	 * of asynchronous tasks.</b> <br>
	 * <br>
	 * Schedules this to run asynchronously after the specified number of server ticks.
	 *
	 * @param creator the reference to the plugin scheduling task
	 * @param delay   the ticks to wait before running the task
	 *
	 * @return a ChioriTask that contains the id number
	 *
	 * @throws IllegalArgumentException if plugin is null
	 * @throws IllegalStateException    if this was already scheduled
	 * @see Tasks#runTaskLaterAsynchronously(RegistrarBase, long, CallableTask)
	 */
	public synchronized Task runTaskLaterAsynchronously( RegistrarBase creator, long delay ) throws IllegalArgumentException, IllegalStateException
	{
		checkState();
		return setupId( Tasks.runTaskLaterAsynchronously( creator, delay, this ) );
	}

	/**
	 * Schedules this to repeatedly run until cancelled, starting after the specified number of server ticks.
	 *
	 * @param creator the reference to the plugin scheduling task
	 * @param delay   the ticks to wait before running the task
	 * @param period  the ticks to wait between runs
	 *
	 * @return a ChioriTask that contains the id number
	 *
	 * @throws IllegalArgumentException if plugin is null
	 * @throws IllegalStateException    if this was already scheduled
	 * @see Tasks#runTaskTimer(RegistrarBase, long, long, CallableTask)
	 */
	public synchronized Task runTaskTimer( RegistrarBase creator, long delay, long period ) throws IllegalArgumentException, IllegalStateException
	{
		checkState();
		return setupId( Tasks.runTaskTimer( creator, delay, period, this ) );
	}

	/**
	 * <b>Asynchronous tasks should never access any API in Main. Great care should be taken to assure the thread-safety
	 * of asynchronous tasks.</b> <br>
	 * <br>
	 * Schedules this to repeatedly run asynchronously until cancelled, starting after the specified number of server
	 * ticks.
	 *
	 * @param creator the reference to the plugin scheduling task
	 * @param delay   the ticks to wait before running the task for the first time
	 * @param period  the ticks to wait between runs
	 *
	 * @return a ChioriTask that contains the id number
	 *
	 * @throws IllegalArgumentException if plugin is null
	 * @throws IllegalStateException    if this was already scheduled
	 * @see Tasks#runTaskTimerAsynchronously(RegistrarBase, long, long, CallableTask)
	 */
	public synchronized Task runTaskTimerAsynchronously( RegistrarBase creator, long delay, long period ) throws IllegalArgumentException, IllegalStateException
	{
		checkState();
		return setupId( Tasks.runTaskTimerAsynchronously( creator, delay, period, this ) );
	}

	private Task setupId( final Task task )
	{
		this.taskId = task.getTaskId();
		return task;
	}
}
