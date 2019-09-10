package com.marchnetworks.command.api.metrics.input;

public class MaxValueInput extends MetricInput
{
	private long value;

	public MaxValueInput( String name, long value )
	{
		super( name );
		this.value = value;
	}

	public long getValue()
	{
		return value;
	}
}
