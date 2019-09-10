package com.marchnetworks.health.data;

import java.util.Collection;

public class AlertConfigData
{
	private long version;
	private Collection<AlertThresholdData> thresholds;

	public AlertConfigData()
	{
	}

	public AlertConfigData( long version, Collection<AlertThresholdData> thresholds )
	{
		this.version = version;
		this.thresholds = thresholds;
	}

	public long getVersion()
	{
		return version;
	}

	public Collection<AlertThresholdData> getThresholds()
	{
		return thresholds;
	}

	public void setVersion( long version )
	{
		this.version = version;
	}

	public void setThresholds( Collection<AlertThresholdData> thresholds )
	{
		this.thresholds = thresholds;
	}
}
