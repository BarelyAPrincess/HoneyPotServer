package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "HashResult", propOrder = {"channelId", "channelState", "hash"} )
public class HashResult
{
	@XmlElement( required = true )
	protected String channelId;
	@XmlElement( required = true )
	protected ChannelState channelState;
	@XmlElement( required = true )
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
