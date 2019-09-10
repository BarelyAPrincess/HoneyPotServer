package com.marchnetworks.command.api.metrics;

public class MaxValue extends SingleValueMetric
{
	private long max;

	public MaxValue()
	{
	}

	public MaxValue( String name )
	{
		super( name );
	}

	public void addValue( long value )
	{
		if ( value > max )
		{
			max = value;
		}
	}

	public String getValueString()
	{
		return "max: " + max;
	}

	public long getMaxValue()
	{
		return max;
	}

	public void setMaxValue( long maxValue )
	{
		max = maxValue;
	}
}
