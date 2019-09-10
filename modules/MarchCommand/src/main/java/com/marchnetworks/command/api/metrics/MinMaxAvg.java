package com.marchnetworks.command.api.metrics;

public class MinMaxAvg extends SingleValueMetric implements Comparable<MinMaxAvg>
{
	private long min;

	private long max;

	private long numSamples;

	private double avg;

	public MinMaxAvg()
	{
	}

	public MinMaxAvg( String name, long value )
	{
		super( name );
		min = value;
		max = value;
		avg = value;
		numSamples = 1L;
	}

	public void addValue( long value )
	{
		if ( value < min )
		{
			min = value;
		}
		if ( value > max )
		{
			max = value;
		}

		numSamples += 1L;
		avg = ( ( avg * ( numSamples - 1L ) + value ) / numSamples );
	}

	public String getValueString()
	{
		return "min: " + min + ", max: " + max + ", avg: " + getAvgString() + ", total: " + numSamples + ", sum:" + getSum();
	}

	public int compareTo( MinMaxAvg other )
	{
		return Long.compare( getSum(), other.getSum() );
	}

	public long getMin()
	{
		return min;
	}

	public void setMin( long min )
	{
		this.min = min;
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

	public long getSum()
	{
		return ( long ) ( avg * numSamples );
	}
}
