package com.marchnetworks.command.api.metrics.input;

public class CurrentMaxAvgInput extends MetricInput
{
	private long value;

	public CurrentMaxAvgInput( String name, long vlaue )
	{
		super( name );
		value = vlaue;
	}

	public long getValue()
	{
		return value;
	}
}
