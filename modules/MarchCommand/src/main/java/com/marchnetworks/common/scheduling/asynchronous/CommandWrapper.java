package com.marchnetworks.common.scheduling.asynchronous;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class CommandWrapper
{
	private Future<?> commandFuture = null;

	private AsynchronousBaseCommand command = null;

	private ExecutorService executorService = null;

	public CommandWrapper( AsynchronousBaseCommand command, ExecutorService executorService )
	{
		this.command = command;
		this.executorService = executorService;
	}

	public void execute()
	{
		commandFuture = executorService.submit( command );
	}

	public boolean cancel()
	{
		command.cancel();

		return commandFuture.cancel( true );
	}

	public boolean isDone()
	{
		return commandFuture.isDone();
	}

	public boolean isCancelled()
	{
		return commandFuture.isCancelled();
	}

	public boolean isComplete()
	{
		return command.isComplete();
	}

	public List<Object> getResults()
	{
		return command.getResults();
	}

	public AsynchronousBaseCommand getCommand()
	{
		return command;
	}

	public boolean isSingleton()
	{
		return command.isSingleton();
	}

	public boolean addResult( Object result, boolean isLastEvent )
	{
		try
		{
			command.addResult( result, new boolean[] {isLastEvent} );
			return true;
		}
		catch ( AsynchronousException e )
		{
		}
		return false;
	}

	public String getOwner()
	{
		return command.getOwner();
	}
}
