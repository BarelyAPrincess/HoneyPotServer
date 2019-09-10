package com.marchnetworks.execution.trigger;

import com.marchnetworks.command.api.execution.trigger.ExecutionTriggerServiceException;
import com.marchnetworks.command.common.execution.trigger.ExecutionTrigger;

public interface ExecutionTriggerService
{
	void schedule( ExecutionTrigger paramExecutionTrigger, boolean paramBoolean, String paramString, Class<?> paramClass ) throws ExecutionTriggerServiceException;

	void unscheduleJob( String paramString1, String paramString2 ) throws ExecutionTriggerServiceException;
}
