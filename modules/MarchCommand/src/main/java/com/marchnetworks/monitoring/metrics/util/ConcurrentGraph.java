package com.marchnetworks.monitoring.metrics.util;

import java.util.ArrayList;
import java.util.List;

public class ConcurrentGraph
{
	private List<String> dates = new ArrayList();
	private List<Long> totals = new ArrayList();
	private List<Long> maxes = new ArrayList();

	public List<String> getDates()
	{
		return dates;
	}

	public List<Long> getTotals()
	{
		return totals;
	}

	public List<Long> getMaxes()
	{
		return maxes;
	}

	public void addDate( String date )
	{
		dates.add( date );
	}

	public void addMax( Long max )
	{
		maxes.add( max );
	}

	public void addTotal( Long total )
	{
		totals.add( total );
	}
}

