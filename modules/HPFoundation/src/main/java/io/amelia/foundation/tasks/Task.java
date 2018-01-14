/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation.tasks;

import io.amelia.foundation.RegistrarBase;

public class Task implements ITask, Runnable
{
	private final RegistrarBase creator;
	private final int id;
	private final CallableTask task;
	private volatile Task next = null;
	private long nextRun;
	/**
	 * -1 means no repeating <br>
	 * -2 means cancel <br>
	 * -3 means processing for Future <br>
	 * -4 means done for Future <br>
	 * -5 means it's creator is disabled - wait <br>
	 * Never 0 <br>
	 * >0 means number of ticks to wait between each execution
	 */
	private volatile long period;

	Task()
	{
		this( null, null, -1, -1 );
	}

	Task( final CallableTask task )
	{
		this( null, task, -1, -1 );
	}

	Task( final RegistrarBase creator, final CallableTask task, final int id, final long period )
	{
		this.creator = creator;
		this.task = task;
		this.id = id;
		this.period = period;
	}

	@Override
	public void cancel()
	{
		Tasks.cancelTask( id );
	}

	@Override
	public final RegistrarBase getRegistrar()
	{
		return creator;
	}

	@Override
	public final int getTaskId()
	{
		return id;
	}

	@Override
	public boolean isSync()
	{
		return true;
	}

	/**
	 * This method properly sets the status to cancelled, synchronizing when required.
	 *
	 * @return false if it is a future task that has already begun execution, true otherwise
	 */
	boolean cancel0()
	{
		setPeriod( -2L );
		return true;
	}

	/**
	 * This method compares a runnable to the scheduled task to determine if it belongs to this Task
	 *
	 * @param r The runnable to compare
	 * @return Does the runnable compare to the scheduled task
	 */
	boolean compare( CallableTask r )
	{
		return r == task;
	}

	Task getNext()
	{
		return next;
	}

	void setNext( Task next )
	{
		this.next = next;
	}

	long getNextRun()
	{
		return nextRun;
	}

	void setNextRun( long nextRun )
	{
		this.nextRun = nextRun;
	}

	long getPeriod()
	{
		return period;
	}

	void setPeriod( long period )
	{
		this.period = period;
	}

	Class<? extends CallableTask> getTaskClass()
	{
		return task.getClass();
	}

	@Override
	public void run()
	{
		try
		{
			task.call();
		}
		catch ( Exception e )
		{
			e.printStackTrace();
			// TODO Handle exception
		}
	}
}
