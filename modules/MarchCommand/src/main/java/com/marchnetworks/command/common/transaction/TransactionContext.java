package com.marchnetworks.command.common.transaction;

import com.marchnetworks.command.common.scheduling.task.Task;

import java.util.ArrayList;
import java.util.List;

public class TransactionContext<T>
{
	private List<T> eventsToSend = new ArrayList();
	private List<Task> tasksToExecute = new ArrayList();
	private int numberOfRetries = 0;
	private int transactionLevel = 0;

	private RuntimeException lastException;

	private int exceptionCounter;

	public void startTransaction()
	{
		transactionLevel += 1;
	}

	public void endTransaction()
	{
		transactionLevel -= 1;
	}

	public boolean isTransactionStarted()
	{
		return transactionLevel > 0;
	}

	public void addEvent( T event )
	{
		eventsToSend.add( event );
	}

	public List<T> getEvents()
	{
		return eventsToSend;
	}

	public void addTask( Task task )
	{
		tasksToExecute.add( task );
	}

	public List<Task> getTasksToExecute()
	{
		return tasksToExecute;
	}

	public void clear()
	{
		eventsToSend.clear();
		tasksToExecute.clear();
	}

	public boolean isRootLevelTransaction()
	{
		return transactionLevel == 1;
	}

	public int getTransactionLevel()
	{
		return transactionLevel;
	}

	public int getNumberOfRetries()
	{
		return numberOfRetries;
	}

	public void incrementNumberOfRetries()
	{
		numberOfRetries += 1;
	}

	public void manageLastException( RuntimeException exception )
	{
		if ( lastException == null )
		{
			lastException = exception;
			exceptionCounter = 0;
		}
		else if ( lastException.getClass().equals( exception.getClass() ) )
		{
			exceptionCounter += 1;
		}
		else
		{
			lastException = exception;
			exceptionCounter = 0;
		}
	}

	public boolean shouldLog()
	{
		return exceptionCounter == 1;
	}
}
