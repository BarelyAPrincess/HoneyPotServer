package com.marchnetworks.server.communications.transport.datamodel;

public class HashResult
{
	protected String channelId;

	protected ChannelState channelState;

	protected String hash;

	public String getChannelId()
	{
		return channelId;
	}

	public void setChannelId( String value )
	{
		channelId = value;
	}

	public ChannelState getChannelState()
	{
		return channelState;
	}

	public void setChannelState( ChannelState value )
	{
		channelState = value;
	}

	public String getHash()
	{
		return hash;
	}

	public void setHash( String value )
	{
		hash = value;
	}
}

