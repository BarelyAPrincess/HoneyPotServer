package com.marchnetworks.command.common.extractor.data.media;

import com.marchnetworks.command.common.extractor.data.Channel;
import com.marchnetworks.command.common.extractor.data.RecorderJob;

import java.util.List;

public class MediaExtractionJob extends RecorderJob
{
	private List<Channel> Channels;
	private long StartTime;
	private long EndTime;
	private boolean Paused;
	private Progress Progress;

	public List<Channel> getChannels()
	{
		return Channels;
	}

	public void setChannels( List<Channel> channels )
	{
		Channels = channels;
	}

	public long getStartTime()
	{
		return StartTime;
	}

	public void setStartTime( long startTime )
	{
		StartTime = startTime;
	}

	public long getEndTime()
	{
		return EndTime;
	}

	public void setEndTime( long endTime )
	{
		EndTime = endTime;
	}

	public boolean isPaused()
	{
		return Paused;
	}

	public void setPaused( boolean paused )
	{
		Paused = paused;
	}

	public Progress getProgress()
	{
		return Progress;
	}

	public void setProgress( Progress progress )
	{
		Progress = progress;
	}
}
