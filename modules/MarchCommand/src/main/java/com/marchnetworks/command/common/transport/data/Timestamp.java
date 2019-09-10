package com.marchnetworks.command.common.transport.data;

public class Timestamp
{
	protected long ticks;

	public static Timestamp getInstance( long tm, long timestampInMicros )
	{
		Timestamp instance = new Timestamp();
		instance.setTicks( timestampInMicros );

		return instance;
	}

	public long getTicks()
	{
		return ticks;
	}

	public void setTicks( long value )
	{
		ticks = value;
	}
}
