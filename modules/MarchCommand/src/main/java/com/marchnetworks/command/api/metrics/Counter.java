package com.marchnetworks.command.api.metrics;

public class Counter extends SingleValueMetric
{
	private long counter;

	public Counter()
	{
	}

	public Counter( String name )
	{
		super( name );
	}

	public void addValue( long value )
	{
		counter += value;
	}

	public String getValueString()
	{
		return "count: " + counter;
	}

	public long getCounter()
	{
		return counter;
	}

	public void setCounter( long counter )
	{
		this.counter = counter;
	}
}

