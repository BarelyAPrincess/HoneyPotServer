package com.marchnetworks.command.api.metrics;

import com.marchnetworks.command.common.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public class BucketMinMaxAvg extends Metric
{
	private Map<String, MinMaxAvg> averages = new ConcurrentSkipListMap();

	public BucketMinMaxAvg()
	{
	}

	public BucketMinMaxAvg( String name, String bucket, long value )
	{
		super( name );
		MinMaxAvg minMaxAvg = new MinMaxAvg( "", value );
		averages.put( bucket, minMaxAvg );
	}

	public void addValue( String bucket, long value )
	{
		MinMaxAvg minMaxAvg = ( MinMaxAvg ) averages.get( bucket );
		if ( minMaxAvg == null )
		{
			minMaxAvg = new MinMaxAvg( "", value );
			averages.put( bucket, minMaxAvg );
		}
		minMaxAvg.addValue( value );
	}

	public String getValueString()
	{
		return null;
	}

	public List<List<String>> getAdditionalTable()
	{
		if ( averages.isEmpty() )
		{
			return null;
		}

		Map<String, MinMaxAvg> sortedAverages = CollectionUtils.sortByValue( averages, 0, true );

		List<List<String>> result = new ArrayList();

		result.add( MetricsDisplayUtils.getMinMaxAvgHeaders() );
		result.addAll( MetricsDisplayUtils.getMinMaxAvgRows( sortedAverages ) );
		return result;
	}

	public Map<String, MinMaxAvg> getAverages()
	{
		return averages;
	}
}

