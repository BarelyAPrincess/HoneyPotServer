package com.marchnetworks.command.common.scheduling.task;

public class TaskFixedPool extends Task
{
	private String executorId;

	private int poolSize;

	private int keepAliveTime;

	public TaskFixedPool( Runnable task, String executorId, int poolSize, int keepAliveTime )
	{
		super( task );
		this.executorId = executorId;
		this.poolSize = poolSize;
		this.keepAliveTime = keepAliveTime;
	}

	public String getExecutorId()
	{
		return executorId;
	}

	public void setExecutorId( String executorId )
	{
		this.executorId = executorId;
	}

	public int getPoolSize()
	{
		return poolSize;
	}

	public void setPoolSize( int poolSize )
	{
		this.poolSize = poolSize;
	}

	public int getKeepAliveTime()
	{
		return keepAliveTime;
	}

	public void setKeepAliveTime( int keepAliveTime )
	{
		this.keepAliveTime = keepAliveTime;
	}
}
