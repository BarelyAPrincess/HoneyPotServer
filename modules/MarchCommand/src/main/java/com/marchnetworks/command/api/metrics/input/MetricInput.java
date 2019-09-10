package com.marchnetworks.command.api.metrics.input;

public abstract class MetricInput
{
	private String name;

	public MetricInput( String name )
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}
}
