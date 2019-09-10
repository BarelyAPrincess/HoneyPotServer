package com.marchnetworks.command.api.metrics.input;

public class MinMaxAvgInput extends MetricInput
{
	private long value;

	public MinMaxAvgInput( String name, long vlaue )
	{
		super( name );
		value = vlaue;
	}

	public long getValue()
	{
		return value;
	}
}
