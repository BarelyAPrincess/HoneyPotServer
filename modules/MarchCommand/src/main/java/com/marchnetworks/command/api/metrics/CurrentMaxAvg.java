package com.marchnetworks.command.api.metrics;

public class CurrentMaxAvg extends SingleValueMetric implements Comparable<CurrentMaxAvg>
{
	private long current;

	private long max;

	private long numSamples;

	private double avg;

	public CurrentMaxAvg()
	{
	}

	public CurrentMaxAvg( String name, long value )
	{
		super( name );
		current = value;
		max = value;
		avg = value;
		numSamples = 1L;
	}

	public void addValue( long value )
	{
		current = value;

		if ( value > max )
		{
			max = value;
		}

		numSamples += 1L;
		avg = ( ( avg * ( numSamples - 1L ) + value ) / numSamples );
	}

	public String getValueString()
	{
		return "current: " + current + ", max: " + max + ", avg: " + getAvgString() + ", total: " + numSamples;
	}

	public int compareTo( CurrentMaxAvg other )
	{
		return Double.compare( avg, other.getAvg() );
	}

	public long getCurrent()
	{
		return current;
	}

	public void setCurrent( long current )
	{
		this.current = current;
	}

	public long getMax()
	{
		return max;
	}

	public void setMax( long max )
	{
		this.max = max;
	}

	public double getAvg()
	{
		return avg;
	}

	public String getAvgString()
	{
		return String.format( "%.02f", new Object[] {Double.valueOf( avg )} );
	}

	public long getTotal()
	{
		return numSamples;
	}
}

