package com.marchnetworks.management.instrumentation;

import com.marchnetworks.command.common.scheduling.TaskScheduler;

public abstract class BaseDeviceScheduler
{
	protected TaskScheduler taskScheduler;
	protected DeviceRegistry deviceRegistry;

	public TaskScheduler getTaskScheduler()
	{
		return taskScheduler;
	}

	public DeviceRegistry getDeviceRegistry()
	{
		return deviceRegistry;
	}

	public void setTaskScheduler( TaskScheduler taskScheduler )
	{
		this.taskScheduler = taskScheduler;
	}

	public void setDeviceRegistry( DeviceRegistry deviceRegistry )
	{
		this.deviceRegistry = deviceRegistry;
	}
}

