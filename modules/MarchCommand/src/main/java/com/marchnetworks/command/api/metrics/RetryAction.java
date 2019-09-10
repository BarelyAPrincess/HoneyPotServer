package com.marchnetworks.command.api.metrics;

import com.marchnetworks.command.common.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class RetryAction extends Metric
{
	private long successes;
	private long failuresRetry;
	private long failures;
	private int maxSources;
	private Map<Long, Long> retries = new ConcurrentSkipListMap();

	private Map<String, MinMaxAvg> successSources = new ConcurrentHashMap();

	private Map<String, MinMaxAvg> failureSources = new ConcurrentHashMap();

	public RetryAction()
	{
	}

	public RetryAction( String name, int maxSources )
	{
		super( name );
		this.maxSources = maxSources;
	}

	public void onSerialization()
	{
		successSources = CollectionUtils.sortByValue( successSources, maxSources, true );
		failureSources = CollectionUtils.sortByValue( failureSources, maxSources, true );
	}

	public String getValueString()
	{
		return "successes: " + successes + ", failures: " + failures + ", retryFailures: " + failuresRetry + ", retries: " + CollectionUtils.mapToString( retries );
	}

	public List<String> getAdditionalInfo()
	{
		return null;
	}

	public List<List<String>> getAdditionalTable()
	{
		Map<String, MinMaxAvg> topSuccess = CollectionUtils.sortByValue( successSources, maxSources, true );
		Map<String, MinMaxAvg> topFailure = CollectionUtils.sortByValue( failureSources, maxSources, true );

		if ( ( topSuccess.isEmpty() ) && ( topFailure.isEmpty() ) )
		{
			return null;
		}

		List<List<String>> result = new ArrayList();

		if ( !topSuccess.isEmpty() )
		{
			result.add( MetricsDisplayUtils.getMinMaxAvgHeaders() );
			List<String> row = new ArrayList();
			Collections.addAll( row, new String[] {"<b>Top success</b>", "", "", "", "", ""} );

			result.add( row );
			result.addAll( MetricsDisplayUtils.getMinMaxAvgRows( topSuccess ) );
		}
		if ( !topFailure.isEmpty() )
		{
			List<String> row = new ArrayList();
			Collections.addAll( row, new String[] {"<b>Top failure</b>", "", "", "", "", ""} );
			result.add( row );
			result.addAll( MetricsDisplayUtils.getMinMaxAvgRows( topFailure ) );
		}
		return result;
	}

	public void addSuccess( String source, long value )
	{
		successes += 1L;
		MinMaxAvg minMaxAvg = ( MinMaxAvg ) successSources.get( source );
		if ( minMaxAvg == null )
		{
			minMaxAvg = new MinMaxAvg( "", value );
			successSources.put( source, minMaxAvg );
		}
		else
		{
			minMaxAvg.addValue( value );
		}
	}

	public void addFailure( String source, long value )
	{
		failures += 1L;
		MinMaxAvg minMaxAvg = ( MinMaxAvg ) failureSources.get( source );
		if ( minMaxAvg == null )
		{
			minMaxAvg = new MinMaxAvg( "", value );
			failureSources.put( source, minMaxAvg );
		}
		else
		{
			minMaxAvg.addValue( value );
		}
	}

	public void addRetry( long numRetry )
	{
		failuresRetry += 1L;
		CollectionUtils.incrementMapLongValue( Long.valueOf( numRetry ), retries );
	}

	public long getSuccesses()
	{
		return successes;
	}

	public void setSuccesses( long successes )
	{
		this.successes = successes;
	}

	public long getFailures()
	{
		return failures;
	}

	public void setFailures( long failures )
	{
		this.failures = failures;
	}

	public long getFailuresRetry()
	{
		return failuresRetry;
	}

	public void setFailuresRetry( long failures )
	{
		failuresRetry = failures;
	}

	public Map<Long, Long> getRetries()
	{
		return retries;
	}
}
