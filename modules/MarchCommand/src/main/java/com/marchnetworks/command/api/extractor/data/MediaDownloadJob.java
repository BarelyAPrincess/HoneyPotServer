package com.marchnetworks.command.api.extractor.data;

import com.marchnetworks.command.common.extractor.data.Channel;
import com.marchnetworks.command.common.extractor.data.CompletionState;
import com.marchnetworks.command.common.extractor.data.State;

import java.util.ArrayList;
import java.util.List;

public class MediaDownloadJob extends Job
{
	private Long deviceId;
	private List<Channel> channels;
	private Long startTime;
	private Long endTime;
	private Boolean restart;
	private State state;
	private CompletionState completionState;

	public Long getDeviceId()
	{
		return deviceId;
	}

	public void setDeviceId( Long deviceId )
	{
		this.deviceId = deviceId;
	}

	public List<Channel> getChannels()
	{
		return channels;
	}

	public Long getStartTime()
	{
		return startTime;
	}

	public void setStartTime( Long startTime )
	{
		this.startTime = startTime;
	}

	public Long getEndTime()
	{
		return endTime;
	}

	public void setEndTime( Long endTime )
	{
		this.endTime = endTime;
	}

	public State getState()
	{
		return state;
	}

	public void setState( State state )
	{
		this.state = state;
	}

	public CompletionState getCompletionState()
	{
		return completionState;
	}

	public void setCompletionState( CompletionState completionState )
	{
		this.completionState = completionState;
	}

	public Boolean getRestart()
	{
		return restart;
	}

	public void setRestart( Boolean restart )
	{
		this.restart = restart;
	}

	public void addChannel( String channelId )
	{
		if ( channels == null )
		{
			channels = new ArrayList( 1 );
		}
		Channel channel = new Channel();
		channel.setId( channelId );
		channels.add( channel );
	}

	public void addChannel( String channelId, String sectorId )
	{
		if ( channels == null )
		{
			channels = new ArrayList( 1 );
		}
		Channel channel = new Channel();
		channel.setId( channelId );
		channel.setSectorId( sectorId );
		channels.add( channel );
	}
}
