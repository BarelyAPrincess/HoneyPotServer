package com.marchnetworks.command.api.execution.trigger;

import com.marchnetworks.command.common.execution.trigger.ExecutionTrigger;

public abstract interface ExecutionTriggerCoreService
{
	public abstract void schedule( ExecutionTrigger paramExecutionTrigger, boolean paramBoolean, String paramString, Class<?> paramClass ) throws ExecutionTriggerServiceException;

	public abstract void unscheduleJob( String paramString1, String paramString2 ) throws ExecutionTriggerServiceException;
}
