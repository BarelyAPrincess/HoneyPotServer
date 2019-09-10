package com.marchnetworks.common.diagnostics.memory;

public class MemoryPoolThreshold
{
	private double threshold;

	private boolean triggered;

	public MemoryPoolThreshold( double threshold )
	{
		setThreshold( threshold );
		setTriggered( false );
	}

	public void setTriggered( boolean triggered )
	{
		this.triggered = triggered;
	}

	public boolean isTriggered()
	{
		return triggered;
	}

	public void setThreshold( double threshold )
	{
		this.threshold = threshold;
	}

	public double getThreshold()
	{
		return threshold;
	}
}

