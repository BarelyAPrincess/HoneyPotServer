package com.marchnetworks.common.scheduling.asynchronous;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public abstract class AsynchronousSingletonCommand extends AsynchronousBaseCommand
{
	private ExecutorService serviceForResults = null;

	private LinkedList<Object> cacheList = new LinkedList();

	private ConcurrentHashMap<UUID, AsynchronousBaseCommand> clientHandles = new ConcurrentHashMap();

	public AsynchronousSingletonCommand( ExecutorService executorService )
	{
		super( executorService );
		serviceForResults = executorService;
	}

	public List<Object> getResults()
	{
		getAndSetResults( null );

		return cacheList;
	}

	private synchronized List<Object> getAndSetResults( AsynchronousCachePlugin pluginToAdd )
	{
		if ( pluginToAdd != null )
		{
			clientHandles.put( pluginToAdd.getUUID(), pluginToAdd );
		}

		List<Object> previousCacheList = new LinkedList( cacheList );
		List<Object> singletonResults = super.getResults();
		Iterator<UUID> uuidIterator = clientHandles.keySet().iterator();
		UUID uuid = null;

		while ( uuidIterator.hasNext() )
		{
			uuid = ( UUID ) uuidIterator.next();
			AsynchronousBaseCommand handle = ( AsynchronousBaseCommand ) clientHandles.get( uuid );

			if ( handle.isCancelled() )
			{
				uuidIterator.remove();
			}
			else
			{
				for ( int i = 0; i < singletonResults.size(); i++ )
				{
					try
					{
						handle.addResult( singletonResults.get( i ), new boolean[0] );
					}
					catch ( AsynchronousException e )
					{
						uuidIterator.remove();
					}
				}
			}
		}
		cacheList.addAll( singletonResults );
		return previousCacheList;
	}

	public boolean isInterrupted()
	{
		return isCancelled();
	}

	public boolean isSingleton()
	{
		return true;
	}

	public AsynchronousBaseCommand getSingletonPlugin() throws AsynchronousException
	{
		AsynchronousCachePlugin command = new AsynchronousCachePlugin( serviceForResults, this );

		List<Object> cache = getAndSetResults( command );
		Iterator<Object> cacheIterator = cache.iterator();
		while ( cacheIterator.hasNext() )
		{
			command.addResult( cacheIterator.next(), new boolean[0] );
		}
		return command;
	}

	private class AsynchronousCachePlugin extends AsynchronousBaseCommand
	{
		private AsynchronousSingletonCommand cacheCommand = null;

		public AsynchronousCachePlugin( ExecutorService executorService, AsynchronousSingletonCommand cacheCommand ) throws AsynchronousException
		{
			super( executorService );

			this.cacheCommand = cacheCommand;
			setTimeout( getTimeout() );
		}

		public synchronized List<Object> getResults()
		{
			cacheCommand.getResults();

			return super.getResults();
		}

		public void run()
		{
			setTaskComplete( true );
		}

		public AsynchronousCommandsList getType()
		{
			return cacheCommand.getType();
		}

		public boolean isSingleton()
		{
			return false;
		}

		public boolean isComplete()
		{
			if ( ( cacheCommand.isComplete() ) && ( super.isComplete() ) && ( cacheCommand.getNumSubmitted() == getNumRetrieved() ) )
			{
				return true;
			}
			return false;
		}
	}
}
