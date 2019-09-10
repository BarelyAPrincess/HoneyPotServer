package com.marchnetworks.command.api.metrics.input;

public class CounterInput extends MetricInput
{
	private long value;

	public CounterInput( String name )
	{
		super( name );
		value = 1L;
	}

	public CounterInput( String name, long value )
	{
		super( name );
		this.value = value;
	}

	public long getValue()
	{
		return value;
	}
}
