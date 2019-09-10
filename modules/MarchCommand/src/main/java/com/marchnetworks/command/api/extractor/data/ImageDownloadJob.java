package com.marchnetworks.command.api.extractor.data;

import java.util.List;

public class ImageDownloadJob extends Job
{
	private final Long channelResourceId;
	private final ImageDownloadTag tag;
	private final boolean manualDownload;
	private final List<Long> timestamps;

	public ImageDownloadJob( Long channelResourceId, ImageDownloadTag tag, boolean manualDownload, List<Long> timestamps )
	{
		this.channelResourceId = channelResourceId;
		this.tag = tag;
		this.manualDownload = manualDownload;
		this.timestamps = timestamps;
	}

	public Long getChannelResourceId()
	{
		return channelResourceId;
	}

	public List<Long> getTimestamps()
	{
		return timestamps;
	}

	public ImageDownloadTag getTag()
	{
		return tag;
	}

	public boolean isManualDownload()
	{
		return manualDownload;
	}
}
