package com.marchnetworks.command.common.scheduling.task;

public class TaskSerial extends Task
{
	protected String executorId;

	public TaskSerial( Runnable task, String executorId )
	{
		super( task );
		this.executorId = executorId;
	}

	public String getExecutorId()
	{
		return executorId;
	}

	public void setExecutorId( String executorId )
	{
		this.executorId = executorId;
	}
}
