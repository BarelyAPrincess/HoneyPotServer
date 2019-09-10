package com.marchnetworks.command.api.metrics;

import com.marchnetworks.command.common.DateUtils;

import java.util.List;

public class MetricSnapshot
{
	private long time;
	private List<Metric> metrics;

	public MetricSnapshot()
	{
	}

	public MetricSnapshot( long time, List<Metric> metrics )
	{
		this.time = time;
		this.metrics = metrics;
	}

	public void onSerialization()
	{
		if ( metrics == null )
		{
			return;
		}

		for ( Metric metric : metrics )
		{
			metric.onSerialization();
		}
	}

	public String getTimeString()
	{
		return DateUtils.getDateStringFromMillis( time, "dd-MMM-yyyy" );
	}

	public long getTime()
	{
		return time;
	}

	public void setTime( long time )
	{
		this.time = time;
	}

	public List<Metric> getMetrics()
	{
		return metrics;
	}

	public void setMetrics( List<Metric> metrics )
	{
		this.metrics = metrics;
	}
}
