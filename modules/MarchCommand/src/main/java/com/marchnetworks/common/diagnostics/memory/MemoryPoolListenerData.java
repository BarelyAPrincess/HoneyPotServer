package com.marchnetworks.common.diagnostics.memory;

public class MemoryPoolListenerData
{
	private MemoryPoolListener listener;

	private MemoryPool pool;

	double[] thresholds;

	public MemoryPoolListenerData( MemoryPoolListener listener, MemoryPool pool, double[] thresholds )
	{
		this.listener = listener;
		this.pool = pool;
		this.thresholds = thresholds;
	}

	public double getMinimumThreshold()
	{
		return thresholds[0];
	}

	public double getNextThreshold( double current )
	{
		for ( double value : thresholds )
		{
			if ( value > current )
				return value;
		}
		return 1.0D;
	}

	public boolean containsThreshold( double threshold )
	{
		for ( double value : thresholds )
		{
			if ( value == threshold )
				return true;
		}
		return false;
	}

	public MemoryPoolListener getListener()
	{
		return listener;
	}

	public MemoryPool getPool()
	{
		return pool;
	}
}
