package com.marchnetworks.command.api.metrics.input;

public class ConcurrentActionInput extends MetricInput
{
	private long maxConcurrent;

	public ConcurrentActionInput( String name, long maxConcurrent )
	{
		super( name );
		this.maxConcurrent = maxConcurrent;
	}

	public long getMaxConcurrent()
	{
		return maxConcurrent;
	}
}
