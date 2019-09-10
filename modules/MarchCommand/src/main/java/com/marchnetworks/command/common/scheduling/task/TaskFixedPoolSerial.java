package com.marchnetworks.command.common.scheduling.task;

public class TaskFixedPoolSerial extends TaskSerial
{
	public TaskFixedPoolSerial( Runnable task, String executorId )
	{
		super( task, executorId );
	}
}
