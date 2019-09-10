package com.marchnetworks.common.configuration;

public class ConfigSettings
{
	private static final int MAX_GLOBAL_BANDWIDTH = 1000;

	private static final int MAX_SIMULTANEOUS_UPDATES = 25;

	private int maxBandwidth;

	private boolean maxBandwidthEnabled = false;
	private int maxDeviceBandwidth;
	private boolean maxDeviceBandwidthEnabled = false;
	private int maxSimultaneousUpdates;
	private boolean maxSimultaneousUpdatesEnabled = false;

	public int getMaxBandwidth()
	{
		if ( maxBandwidthEnabled )
		{
			return maxBandwidth;
		}
		return 1000;
	}

	public void setMaxBandwidth( int maxBandwidth )
	{
		this.maxBandwidth = maxBandwidth;
	}

	public int getMaxDeviceBandwidth()
	{
		if ( maxDeviceBandwidthEnabled )
		{
			return maxDeviceBandwidth;
		}
		return 1000;
	}

	public void setMaxDeviceBandwidth( int maxDeviceBandwidth )
	{
		this.maxDeviceBandwidth = maxDeviceBandwidth;
	}

	public int getMaxSimultaneousUpdates()
	{
		if ( maxSimultaneousUpdatesEnabled )
		{
			return maxSimultaneousUpdates;
		}
		return 25;
	}

	public void setMaxSimultaneousUpdates( int maxSimultaneousUpdates )
	{
		this.maxSimultaneousUpdates = maxSimultaneousUpdates;
	}

	public boolean isMaxBandwidthEnabled()
	{
		return maxBandwidthEnabled;
	}

	public void setMaxBandwidthEnabled( boolean maxBandwidthEnabled )
	{
		this.maxBandwidthEnabled = maxBandwidthEnabled;
	}

	public boolean isMaxDeviceBandwidthEnabled()
	{
		return maxDeviceBandwidthEnabled;
	}

	public void setMaxDeviceBandwidthEnabled( boolean maxDeviceBandwidthEnabled )
	{
		this.maxDeviceBandwidthEnabled = maxDeviceBandwidthEnabled;
	}

	public boolean isMaxSimultaneousUpdatesEnabled()
	{
		return maxSimultaneousUpdatesEnabled;
	}

	public void setMaxSimultaneousUpdatesEnabled( boolean maxSimultaneousUpdatesEnabled )
	{
		this.maxSimultaneousUpdatesEnabled = maxSimultaneousUpdatesEnabled;
	}
}
