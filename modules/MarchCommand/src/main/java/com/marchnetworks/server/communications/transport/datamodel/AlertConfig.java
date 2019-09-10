package com.marchnetworks.server.communications.transport.datamodel;

public class AlertConfig
{
	private String id;
	private AlertThreshold[] thresholds;

	public AlertConfig()
	{
	}

	public AlertConfig( String id, AlertThreshold[] thresholds )
	{
		this.id = id;
		this.thresholds = thresholds;
	}

	public String getId()
	{
		return id;
	}

	public void setId( String id )
	{
		this.id = id;
	}

	public AlertThreshold[] getThresholds()
	{
		return thresholds;
	}

	public void setThresholds( AlertThreshold[] thresholds )
	{
		this.thresholds = thresholds;
	}
}

