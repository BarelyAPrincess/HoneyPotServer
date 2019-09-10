package com.marchnetworks.command.api.extractor.data;

public class TransactionJob extends Job
{
	private String siteId;

	private String terminalId;

	private Long textChannelId;

	private String customerId;

	private Long deviceResourceId;

	public TransactionJob( String siteId, String terminalId, Long deviceResourceId )
	{
		this.siteId = siteId;
		this.terminalId = terminalId;
		this.deviceResourceId = deviceResourceId;
	}

	public TransactionJob( String siteId, String terminalId, Long textChannelId, String customerId )
	{
		this.siteId = siteId;
		this.terminalId = terminalId;
		this.textChannelId = textChannelId;
		this.customerId = customerId;
	}

	public String getSiteId()
	{
		return siteId;
	}

	public void setSiteId( String siteId )
	{
		this.siteId = siteId;
	}

	public String getTerminalId()
	{
		return terminalId;
	}

	public void setTerminalId( String terminalId )
	{
		this.terminalId = terminalId;
	}

	public Long getTextChannelId()
	{
		return textChannelId;
	}

	public void setTextChannelId( Long textChannelId )
	{
		this.textChannelId = textChannelId;
	}

	public String getCustomerId()
	{
		return customerId;
	}

	public void setCustomerId( String customerId )
	{
		this.customerId = customerId;
	}

	public Long getDeviceResourceId()
	{
		return deviceResourceId;
	}

	public void setDeviceResourceId( Long deviceResourceId )
	{
		this.deviceResourceId = deviceResourceId;
	}
}
