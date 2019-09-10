package com.marchnetworks.command.api.execution.trigger;

public class ExecutionTriggerServiceException extends Exception
{
	private static final long serialVersionUID = 1L;

	public ExecutionTriggerServiceException()
	{
	}

	public ExecutionTriggerServiceException( String errorMessage, Exception e )
	{
		super( errorMessage, e );
	}
}
