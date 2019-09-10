package com.marchnetworks.command.common.scheduling;

public abstract interface NonConcurrentRunnable extends Runnable
{
	public abstract String getTaskId();
}
