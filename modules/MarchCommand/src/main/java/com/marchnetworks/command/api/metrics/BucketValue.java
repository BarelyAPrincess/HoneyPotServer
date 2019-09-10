package com.marchnetworks.command.api.metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;

public class BucketValue extends Metric
{
	private Map<String, Long> counters = new ConcurrentSkipListMap();

	public BucketValue()
	{
	}

	public BucketValue( String name )
	{
		super( name );
	}

	public void addValue( String bucket, long value )
	{
		counters.put( bucket, Long.valueOf( value ) );
	}

	public String getValueString()
	{
		return null;
	}

	public List<List<String>> getAdditionalTable()
	{
		if ( counters.isEmpty() )
		{
			return null;
		}

		List<List<String>> result = new ArrayList();
		result.add( MetricsDisplayUtils.getNameValueHeaders() );
		Set<Entry<String, Long>> set = counters.entrySet();
		for ( Entry<String, Long> entry : set )
		{
			Long value = ( Long ) entry.getValue();
			List<String> row = Arrays.asList( new String[] {( String ) entry.getKey(), String.valueOf( value )} );
			result.add( row );
		}
		return result;
	}
}

