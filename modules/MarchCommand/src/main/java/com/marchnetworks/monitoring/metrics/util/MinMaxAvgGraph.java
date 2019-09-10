package com.marchnetworks.monitoring.metrics.util;

import java.util.ArrayList;
import java.util.List;

public class MinMaxAvgGraph
{
	private List<String> dates = new ArrayList();
	private List<Long> maxes = new ArrayList();
	private List<Double> averages = new ArrayList();

	public List<String> getDates()
	{
		return dates;
	}

	public List<Long> getMaxes()
	{
		return maxes;
	}

	public List<Double> getAverages()
	{
		return averages;
	}

	public void addDate( String date )
	{
		dates.add( date );
	}

	public void addMax( Long max )
	{
		maxes.add( max );
	}

	public void addAverage( Double average )
	{
		averages.add( average );
	}
}

