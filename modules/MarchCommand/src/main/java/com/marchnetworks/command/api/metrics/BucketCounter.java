package com.marchnetworks.command.api.metrics;

import com.marchnetworks.command.common.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public class BucketCounter extends Metric
{
	private Map<String, Long> counters = new ConcurrentSkipListMap();
	private int itemsPerLine;

	public BucketCounter()
	{
	}

	public BucketCounter( String name, int itemsPerLine )
	{
		super( name );
		this.itemsPerLine = itemsPerLine;
	}

	public void addValue( String bucket, long value )
	{
		Long current = ( Long ) counters.get( bucket );
		if ( current == null )
		{
			current = Long.valueOf( value );
		}
		else
		{
			current = Long.valueOf( current.longValue() + value );
		}
		counters.put( bucket, current );
	}

	public String getValueString()
	{
		String result = "counts: ";
		if ( itemsPerLine == 0 )
		{
			result = result + CollectionUtils.mapToStringList( counters, 0 );
		}
		return result;
	}

	public List<String> getAdditionalInfo()
	{
		if ( itemsPerLine > 0 )
		{
			return CollectionUtils.mapToStringList( counters, itemsPerLine );
		}
		return null;
	}
}
