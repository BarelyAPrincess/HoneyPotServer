package com.marchnetworks.command.api.metrics;

public abstract class SingleValueMetric extends Metric
{
	public SingleValueMetric()
	{
	}

	public SingleValueMetric( String name )
	{
		super( name );
	}

	public abstract void addValue( long paramLong );
}
