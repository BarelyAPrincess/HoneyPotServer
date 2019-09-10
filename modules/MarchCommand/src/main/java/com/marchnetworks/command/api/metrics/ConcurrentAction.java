package com.marchnetworks.command.api.metrics;

public class ConcurrentAction extends SingleValueMetric
{
	private long total;

	private long maxConcurrent;

	public ConcurrentAction()
	{
	}

	public ConcurrentAction( String name )
	{
		super( name );
	}

	public void addValue( long concurrent )
	{
		if ( concurrent > maxConcurrent )
		{
			maxConcurrent = concurrent;
		}
		total += 1L;
	}

	public String getValueString()
	{
		return "total: " + total + ", maxConcurrent: " + maxConcurrent;
	}

	public Long getTotal()
	{
		return Long.valueOf( total );
	}

	public void setTotal( Long total )
	{
		this.total = total.longValue();
	}

	public Long getMaxConcurrent()
	{
		return Long.valueOf( maxConcurrent );
	}

	public void setMaxConcurrent( Long maxConcurrent )
	{
		this.maxConcurrent = maxConcurrent.longValue();
	}
}

