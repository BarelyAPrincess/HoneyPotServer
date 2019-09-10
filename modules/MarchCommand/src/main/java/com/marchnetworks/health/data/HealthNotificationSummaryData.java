package com.marchnetworks.health.data;

public class HealthNotificationSummaryData
{
	private String deviceName;

	private int totalIssues;

	private int driveIssue;

	private int unitIssue;

	private int networkIssue;

	private int videoIssue;

	private int powerIssue;

	private int peripheralIssue;

	private long firstOccurence;

	private long lastOccurence;

	public HealthNotificationSummaryData()
	{
		totalIssues = 0;
		driveIssue = 0;
		unitIssue = 0;
		networkIssue = 0;
		videoIssue = 0;
		powerIssue = 0;
		peripheralIssue = 0;
		firstOccurence = 0L;
		lastOccurence = 0L;
	}

	public void setDeviceName( String deviceName )
	{
		this.deviceName = deviceName;
	}

	public String getDeviceName()
	{
		return deviceName;
	}

	public void setTotalIssues( int totalIssues )
	{
		this.totalIssues = totalIssues;
	}

	public int getTotalIssues()
	{
		return totalIssues;
	}

	public int incrementTotalIssues()
	{
		return ++totalIssues;
	}

	public void setDriveIssue( int driveIssue )
	{
		this.driveIssue = driveIssue;
	}

	public int getDriveIssues()
	{
		return driveIssue;
	}

	public int incrementDriveIssues()
	{
		return ++driveIssue;
	}

	public void setUnitIssues( int unitIssue )
	{
		this.unitIssue = unitIssue;
	}

	public int getUnitIssues()
	{
		return unitIssue;
	}

	public int incrementUnitIssues()
	{
		return ++unitIssue;
	}

	public void setNetworkIssue( int networkssue )
	{
		networkIssue = networkssue;
	}

	public int getNetworkIssue()
	{
		return networkIssue;
	}

	public int incrementNetworkIssue()
	{
		return ++networkIssue;
	}

	public void setVideoIssue( int videoIssue )
	{
		this.videoIssue = videoIssue;
	}

	public int getVideoIssue()
	{
		return videoIssue;
	}

	public int incrementVideoIssue()
	{
		return ++videoIssue;
	}

	public void setPowerIssue( int powerIssue )
	{
		this.powerIssue = powerIssue;
	}

	public int getPowerIssue()
	{
		return powerIssue;
	}

	public int incrementPowerIssue()
	{
		return ++powerIssue;
	}

	public void setPeripheralIssue( int peripheralIssue )
	{
		this.peripheralIssue = peripheralIssue;
	}

	public int getPeripheralIssue()
	{
		return peripheralIssue;
	}

	public int incrementPeripheralIssue()
	{
		return ++peripheralIssue;
	}

	public void setFirstOccurence( long firstOccurence )
	{
		this.firstOccurence = firstOccurence;
	}

	public long getFirstOccurence()
	{
		return firstOccurence;
	}

	public void setLastOccurence( long lastOccurence )
	{
		this.lastOccurence = lastOccurence;
	}

	public long getLastOccurence()
	{
		return lastOccurence;
	}
}
