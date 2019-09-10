package com.marchnetworks.common.scheduling.asynchronous;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public abstract class AsynchronousBaseCommand implements Runnable
{
	private boolean eventsComplete = true;
	private boolean isCancelled = false;
	private String owner = null;
	private CompletionService<Object> results = null;
	private boolean resultsComplete = false;
	private Timeout resultsTimeout = null;
	private int retrievedResults = 0;
	private int submittedResults = 0;
	private boolean taskComplete = false;
	private UUID uuid = null;

	public AsynchronousBaseCommand( ExecutorService executorService )
	{
		results = new ExecutorCompletionService( executorService );
		setUUID( UUID.randomUUID() );
		resultsTimeout = new Timeout( uuid, 60 );
	}

	public void addResult( Object result, boolean... eventsCompletion ) throws AsynchronousException
	{
		if ( isCancelled() )
		{
			throw AsynchronousException.interruptedException( "The command has been cancelled." );
		}
		if ( eventsCompletion.length > 0 )
		{
			eventsComplete = eventsCompletion[0];
		}
		if ( result == null )
		{
			return;
		}
		CallableResult callableResult = new CallableResult( result );

		results.submit( callableResult );

		submittedResults += 1;
	}

	public void cancel()
	{
		setTimeout( 0 );
		isCancelled = true;
	}

	private void checkResultsCompletion()
	{
		if ( ( taskComplete ) && ( submittedResults == retrievedResults ) && ( eventsComplete ) )
		{
			resultsComplete = true;
		}
	}

	public int getNumRetrieved()
	{
		return retrievedResults;
	}

	public int getNumSubmitted()
	{
		return submittedResults;
	}

	public String getOwner()
	{
		return owner;
	}

	public synchronized List<Object> getResults()
	{
		LinkedList<Object> resultList = new LinkedList();
		Object result = null;
		Future<Object> future = null;

		while ( ( future = results.poll() ) != null )
		{
			try
			{
				result = future.get();

				resultList.add( result );

				retrievedResults += 1;
			}
			catch ( InterruptedException e )
			{
				return null;
			}
			catch ( ExecutionException e )
			{
				return null;
			}
		}

		checkResultsCompletion();
		return resultList;
	}

	public Timeout getTimeout()
	{
		return resultsTimeout;
	}

	public abstract AsynchronousCommandsList getType();

	public UUID getUUID()
	{
		return uuid;
	}

	public boolean isCancelled()
	{
		if ( Thread.interrupted() )
		{
			cancel();
		}

		return isCancelled;
	}

	public boolean isComplete()
	{
		return resultsComplete;
	}

	public boolean isSingleton()
	{
		return false;
	}

	public void setOwner( String owner )
	{
		this.owner = owner;
	}

	public void setTaskComplete( boolean completionStatus )
	{
		taskComplete = completionStatus;
	}

	public void setTimeout( int timeoutInSeconds )
	{
		if ( timeoutInSeconds >= 0 )
		{
			resultsTimeout.setTimeout( timeoutInSeconds );
		}
	}

	public void setTimeout( Timeout exactTimeout )
	{
		resultsTimeout.setTimeout( exactTimeout );
	}

	public void setUUID( UUID uuid )
	{
		this.uuid = uuid;
	}

	private class CallableResult implements Callable<Object>
	{
		private Object result = null;

		public CallableResult( Object result )
		{
			this.result = result;
		}

		public Object call() throws Exception
		{
			return result;
		}
	}

	public class Timeout
	{
		private Calendar timeout = null;
		private UUID uuid = null;

		public Timeout( UUID uuid, int timeoutParam )
		{
			this.uuid = uuid;
			setTimeout( timeoutParam );
		}

		public boolean equals( Object o )
		{
			if ( ( o instanceof Timeout ) )
			{
				Timeout timeout = ( Timeout ) o;
				return timeout.getUUID().equals( uuid );
			}
			return false;
		}

		public Calendar getTimeout()
		{
			return timeout;
		}

		public UUID getUUID()
		{
			return uuid;
		}

		public void setTimeout( Timeout exactTimeout )
		{
			timeout = exactTimeout.getTimeout();
		}

		public void setTimeout( int timeoutParam )
		{
			timeout = Calendar.getInstance( TimeZone.getTimeZone( "UTC" ) );
			if ( timeoutParam > 0 )
			{
				timeout.add( 13, timeoutParam );
			}
			else
			{
				timeout.add( 13, 0 );
			}
		}
	}
}
