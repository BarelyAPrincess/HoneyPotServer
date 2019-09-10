package com.marchnetworks.command.api.extractor.data;

import java.util.List;

public class ImageDownloadScheduleJob extends Job
{
	private Long siteId;
	private Long scheduleId;
	private String timezone;
	private Integer downloadFrequency;
	private List<Long> channelResourceIds;
	private boolean highResolution;

	public ImageDownloadScheduleJob( Long siteId, Long scheduleId, String timezone, Integer downloadFrequency, List<Long> channelIds, boolean highResolution )
	{
		this.siteId = siteId;
		this.scheduleId = scheduleId;
		this.timezone = timezone;
		this.downloadFrequency = downloadFrequency;
		channelResourceIds = channelIds;
		this.highResolution = highResolution;
	}

	public Long getSiteId()
	{
		return siteId;
	}

	public void setSiteId( Long siteId )
	{
		this.siteId = siteId;
	}

	public Long getScheduleId()
	{
		return scheduleId;
	}

	public void setScheduleId( Long scheduleId )
	{
		this.scheduleId = scheduleId;
	}

	public String getTimezone()
	{
		return timezone;
	}

	public void setTimezone( String timezone )
	{
		this.timezone = timezone;
	}

	public Integer getDownloadFrequency()
	{
		return downloadFrequency;
	}

	public void setDownloadFrequency( Integer downloadFrequency )
	{
		this.downloadFrequency = downloadFrequency;
	}

	public List<Long> getChannelResourceIds()
	{
		return channelResourceIds;
	}

	public void setChannelResourceIds( List<Long> channelResourceIds )
	{
		this.channelResourceIds = channelResourceIds;
	}

	public boolean isHighResolution()
	{
		return highResolution;
	}
}
